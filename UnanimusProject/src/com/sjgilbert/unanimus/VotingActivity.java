package com.sjgilbert.unanimus;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sjgilbert.unanimus.parsecache.ParseCache;
import com.sjgilbert.unanimus.unanimus_activity.UnanimusActivityTitle;

import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

/**
 * The activity for voting on restaurantIterator
 */
public class VotingActivity
        extends UnanimusActivityTitle
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static final int NUMBER_OF_RESTAURANTS = 15;

    private static final String VA = "va";

    private final BuildGoogleApiClientWorker googleApiClientWorker
            = new BuildGoogleApiClientWorker(this);

    private GoogleApiClient googleApiClient = null;


    private UnanimusGroup group;
    private String groupKey;

    private int i;
    private TextView counter;
    private ListIterator<String> restaurantIterator;

    public VotingActivity() {
        super(VA);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.voting_activity);
        try {
            setTitleBar(R.string.voting_activity_title, (ViewGroup) findViewById(R.id.voting_activity));
        } catch (ClassCastException e) {
            log(ELog.e, e.getMessage(), e);
        }

        Bundle extras = getIntent().getExtras();    //The GROUP_ID of the selected group_activity
        if (extras != null) {
            groupKey = extras.getString(GroupActivity.GROUP_ID);
        } else {
            Toast.makeText(VotingActivity.this, "NULL OBJ ID", Toast.LENGTH_LONG).show();
        }

        ParseQuery<ParseObject> query = ParseCache.parseCache.get(groupKey);
        if (query == null) {
            log(ELog.e, "messed up");
            finish();
        }

        assert query != null;

        try {
            group = (UnanimusGroup) query.getFirst();
        } catch (ClassCastException | ParseException e) {
            log(ELog.e, e.getMessage(), e);
            finish();
        }

        counter = (TextView) findViewById(R.id.va_voting_counter);
        restaurantIterator = group.getRestaurantIterator();

        Places.GeoDataApi.getPlaceById(googleApiClient, "ChIJg-eOlowq9ocRrjQq2PKvNlc")
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place myPlace = places.get(0);
                            Log.i(null, "Place found: " + myPlace.getName());
                        }
                        places.release();
                    }
                });



        final TextView restaurant = (TextView) findViewById(R.id.va_voting_restaurant_view);

        while (restaurantIterator.hasNext()) {

        }

        ParseQuery.clearAllCachedResults();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        if (group == null) setResult(RESULT_CANCELED);
        else setResult(RESULT_OK);

        super.finish();
    }

    private void setYesVote(int index) {
        group.vote(index, Vote.getUpVote(), null);
    }

    private void setNoVote(int index) {group.vote(index, Vote.getDownVote(), null);
    }

    private void incrementRestaurant() {
        i++;
        counter.setText(String.format("%d/15", i + 1));

        TextView restaurant = (TextView) findViewById(R.id.va_voting_restaurant_view);
//        restaurant.setText(restaurantIterator.get(i));
    }

    public void va_viewVoteNo(View view) {
//        setNoVote();
//                showVotes();
        if (i < NUMBER_OF_RESTAURANTS - 1) {
            incrementRestaurant();
        } else {
//                    group.checkIfComplete();
            finish();
        }
    }

    public void va_viewVoteYes(View view) {
//        setYesVote();
//                showVotes();
        if (i < NUMBER_OF_RESTAURANTS - 1) {
            incrementRestaurant();
        } else {
//                    group.checkIfComplete();
            finish();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        log(ELog.w, "Google Places Api client connection was suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log(ELog.w, connectionResult.toString());
    }

//    private void showVotes() {
//        Toast.makeText(
//                VotingActivity.this,
//                voteContainer.getVotes().toString(),
//                Toast.LENGTH_LONG
//        ).show();
//    }

    private static abstract class VotingActivityAsyncTask<T1, T2, T3>
            extends AsyncTask<T1, T2, T3> {
        private final VotingActivity votingActivity;

        public VotingActivityAsyncTask(VotingActivity votingActivity) {
            super();
            this.votingActivity = votingActivity;
        }
    }

    private static class BuildGoogleApiClientWorker
            extends VotingActivityAsyncTask<Object, Object, GoogleApiClient> {
        public BuildGoogleApiClientWorker(VotingActivity votingActivity) {
            super(votingActivity);
        }

        private static GoogleApiClient buildGoogleApiClient(
                Context context,
                GoogleApiClient.ConnectionCallbacks callbacks,
                GoogleApiClient.OnConnectionFailedListener connectionFailedListener
        ) {
            return new GoogleApiClient
                    .Builder(context)
                    .addConnectionCallbacks(callbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .addApi(LocationServices.API)
                    .build();
        }

        @Override
        protected GoogleApiClient doInBackground(Object... params) {
            return buildGoogleApiClient(
                    super.votingActivity,
                    super.votingActivity,
                    super.votingActivity
            );
        }

        @Override
        protected void onPostExecute(GoogleApiClient googleApiClient) {
            super.votingActivity.setGoogleApiClient(this);
        }

    }

    private void setGoogleApiClient(BuildGoogleApiClientWorker buildGoogleApiClientAsyncTask) {
        try {
            googleApiClient = buildGoogleApiClientAsyncTask.get();
        } catch (InterruptedException | ExecutionException e) {
            log(ELog.e, e.getMessage(), e);
            return;
        }
        googleApiClient.connect();
    }
}
