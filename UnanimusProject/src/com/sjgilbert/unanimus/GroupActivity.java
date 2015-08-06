package com.sjgilbert.unanimus;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sjgilbert.unanimus.unanimus_activity.UnanimusActivityTitle;

import java.util.ArrayList;

/**
 * The activity which displays a specific group_activity a user is a part of.  Should
 * eventually allow the user to indicate preferences/view recommendations.
 */
public class GroupActivity extends UnanimusActivityTitle {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_activity);
        try {
            setTitleBar(R.string.group_activity_title, (ViewGroup) findViewById(R.id.group_activity));
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();    //The groupID of the selected group_activity
        if (extras != null) {
            groupName = extras.getString("objID");
        }
        else {
            Toast.makeText(GroupActivity.this, "NULL OBJ ID", Toast.LENGTH_LONG).show();
        }

        //Setting the group_activity name at top
        TextView groupNameTextView = (TextView) findViewById(R.id.group_name);
        groupNameTextView.setText("GROUP ID: " + groupName);

        //Query for the group_activity's data
        ParseQuery<UnanimusGroup> query = ParseQuery.getQuery("UnanimusGroup");
        query.include("members");
        try{
            group = query.get(groupName);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        //Setting owner of group_activity
        TextView createdBy = (TextView) findViewById(R.id.group_created_by);
        createdBy.setText("Created by " + Profile.getCurrentProfile().getName());

        //Setting members of group_activity
        ArrayList<String> usernames = new ArrayList<>();
        for (ParseUser user : group.getMembers()) {
            usernames.add(user.getUsername());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.members_fragment, usernames);
        ListView membersList = (ListView) findViewById(R.id.members_list);
        membersList.setAdapter(adapter);

    }
    private String groupName;
    private UnanimusGroup group;
}
