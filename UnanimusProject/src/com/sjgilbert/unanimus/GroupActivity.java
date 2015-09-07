package com.sjgilbert.unanimus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.sjgilbert.unanimus.parsecache.ParseCache;
import com.sjgilbert.unanimus.unanimus_activity.UnanimusActivityTitle;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * The activity which displays a specific group_activity a user is a part of.  Should
 * eventually allow the user to indicate preferences/view recommendations.
 */
public class GroupActivity extends UnanimusActivityTitle {
    public static final String GROUP_ID = "GROUP_ID";
    private static final String TAG = "ga";
    private String groupName;
    private CgaContainer group;


    public GroupActivity() {
        super(TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_activity);
        try {
            setTitleBar(R.string.ga_title, (ViewGroup) findViewById(R.id.group_activity));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();    //The GROUP_ID of the selected group_activity
        if (extras != null) {
            groupName = extras.getString(ParseCache.OBJECT_ID);
        } else {
            throw new IllegalArgumentException();
        }

        assert groupName != null;
        //Setting the group_activity name at top
        TextView groupNameTextView = (TextView) findViewById(R.id.ga_name);
        groupNameTextView.setText("GROUP ID: " + groupName);

        //Query for the group_activity's data
        ParseQuery query = ParseQuery.getQuery(CgaContainer.class);
        query.whereEqualTo(ParseCache.OBJECT_ID, groupName);

        ParseCache.parseCache.put(groupName, (ParseQuery<ParseObject>) query);
        try {
            group = (CgaContainer) query.getFirst();
        } catch (ParseException e) {
            log(ELog.e, e.getMessage(), e);
            return;
        }

        try {
            group.load();
        } catch (ParseException e) {
            log(ELog.e, e.getMessage(), e);
            return;
        }

        UnanimusGroup.Builder builder = new UnanimusGroup.Builder(group);
        builder.getInBackground(new UnanimusGroup.Builder.Callback() {
            @Override
            public void done(UnanimusGroup unanimusGroup) {
                unanimusGroup.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) log(ELog.e, e.getMessage(), e);
                    }
                });

                CgaContainer cgaContainer = unanimusGroup.getCgaContainer();
                FpaContainer fpaContainer = cgaContainer.getFpaContainer();

                FpaContainer.UserIdPair[] userIdPairs = fpaContainer.getUserIdPairs();

                setData(userIdPairs);
            }
        });

        //Setting owner of group_activity
        TextView createdBy = (TextView) findViewById(R.id.ga_created_by);
        createdBy.setText("Created by " + Profile.getCurrentProfile().getName());
    }

    private void setData(final FpaContainer.UserIdPair[] userIdPairs) {
        final int length = userIdPairs.length;

        final ArrayList<String> usernames = new ArrayList<>(length);
        GraphRequest[] requests = new GraphRequest[length];
        for (int i = 0; i < length; i++) {
            String user = userIdPairs[i].facebookUserId;
            requests[i] = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    String.format("/%s", user),
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            if (response.getError() != null) {
                                log(ELog.e, response.getError().getErrorMessage());
                                return;
                            }

                            try {
                                usernames.add(response.getJSONObject().getString("name"));
                            } catch (JSONException e) {
                                log(ELog.e, e.getMessage(), e);
                            }
                        }
                    }
            );
        }

        GraphRequestBatch requestBatch = new GraphRequestBatch(requests);
        requestBatch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequestBatch) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(GroupActivity.this, R.layout.members_fragment, usernames);
                ListView membersList = (ListView) findViewById(R.id.ga_members_list);
                membersList.setAdapter(adapter);
            }
        });

        requestBatch.executeAsync();

    }
    @SuppressWarnings("unused")
    public void ga_viewStartVotingActivity(@SuppressWarnings("UnusedParameters") View view) {
        Intent intent = new Intent(GroupActivity.this, VotingActivity.class);
        intent.putExtra(GROUP_ID, groupName);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    public void ga_viewStartRecommendationActivity(@SuppressWarnings("UnusedParameters") View view) {
        if (group.get("recommendation") != null) {
            Intent intent = new Intent(GroupActivity.this, RecommendationActivity.class);
            intent.putExtra(GROUP_ID, groupName);
            startActivity(intent);
        }
    }
}
