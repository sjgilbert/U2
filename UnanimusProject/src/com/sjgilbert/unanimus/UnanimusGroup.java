package com.sjgilbert.unanimus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sjgilbert.unanimus.parsecache.ParseCache;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Model for a group_activity of users.
 */
@ParseClassName("UnanimusGroup")
public class UnanimusGroup extends ParseObject implements  IDependencyContainer {
    private FpaContainer fpaContainer;
    private GspaContainer gspaContainer;
    private PpaContainer ppaContainer;

    public UnanimusGroup() {
        super();

        if (! has(FriendPickerActivity.FPA)
                || ! has(GroupSettingsPickerActivity.GSPA)
                || ! has(PlacePickActivity.PPA))
            return;

        fpaContainer = (FpaContainer) get(FriendPickerActivity.FPA);
        gspaContainer = (GspaContainer) get(GroupSettingsPickerActivity.GSPA);
        ppaContainer = (PpaContainer) get(PlacePickActivity.PPA);
    }

    static ParseQuery<UnanimusGroup> getQuery() {
        return ParseQuery.getQuery(UnanimusGroup.class);
    }

    int getMaxRestaurants() {
        return 15;
    }

    @Nullable GspaContainer getGspaContainer() {
        return gspaContainer;
    }

    void setGspaContainer(Bundle bundle) throws NotSetException {
        this.gspaContainer = new GspaContainer();
        gspaContainer.setFromBundle(bundle);
    }

    @Nullable FpaContainer getFpaContainer() {
        return fpaContainer;
    }

    void setFpaContainer(Bundle bundle) throws NotSetException {
        this.fpaContainer = new FpaContainer();
        fpaContainer.setFromBundle(bundle);
    }

    @Nullable PpaContainer getPpaContainer() {
        return ppaContainer;
    }

    void setPpaContainer(Bundle bundle) throws NotSetException {
        this.ppaContainer = new PpaContainer();
        ppaContainer.setFromBundle(bundle);
    }

    @Deprecated
    public ArrayList<String> getRestaurants() {
        Object o_restaurants = get("restaurants");

        if (null == o_restaurants)
            throw new NullPointerException();

        if (!(o_restaurants instanceof ArrayList))
            throw new ClassCastException();

        ArrayList al_restaurants = (ArrayList) o_restaurants;

        ArrayList<String> al_p_restaurants = new ArrayList<>();
        for (Object o : al_restaurants)
            if (o instanceof String)
                al_p_restaurants.add((String) o);

        return al_p_restaurants;
    }

    @Deprecated
    public void addVoteArray(ArrayList<Integer> voteArray) {
        add("voteArrays", voteArray);
        saveInBackground();
    }

//    @Deprecated
//    private ArrayList<Integer> voteTally() {
//        ArrayList<Integer> voteSum = new ArrayList<>();
//        for (int x = 0; x < VotingActivity.NUMBER_OF_RESTAURANTS; x++) {
//            voteSum.add(0);
//        }
//        JSONArray array = getJSONArray("voteArrays");
//        for (int i = 0; i < array.length(); i++) {
//            ArrayList<Integer> oneUsersVotes;
//            try {
//                JSONArray vA = array.getJSONArray(i);
//                ArrayList<Integer> vAL = new ArrayList<>();
//                for (int k = 0; k < VotingActivity.NUMBER_OF_RESTAURANTS; k++) {
//                    vAL.add(vA.getInt(k));
//                }
//                oneUsersVotes = vAL;
//                for (int j = 0; j < getRestaurants().size(); j++) {
//                    voteSum.set(j, (voteSum.get(j) + oneUsersVotes.get(j)));
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//        return voteSum;
//    }

    @Deprecated
    private String getBestRestaurant(ArrayList<Integer> talliedVotes) {
        int winIndex = talliedVotes.get(0);
        for (int i = 0; i < talliedVotes.size(); i++) {
            int num = talliedVotes.get(i);
            if (num > winIndex) {
                winIndex = i;
            }
        }
        return getRestaurants().get(winIndex);
    }

//    @Deprecated
//    public void checkIfComplete() {
//        if ((getJSONArray("voteArrays").length() == getMembers().size())) {
//            String recommendation = getBestRestaurant(voteTally());
//            put("recommendation", recommendation);
//            saveInBackground();
//        } else {
//            Log.i(
//                    "Unanimus",
//                    "Voting not complete"
//            );
//        }
//    }

    @Deprecated
    public ArrayList<String> getMembers() {
        Object o_members = get("members");

        if (null == o_members)
            throw new NullPointerException();

        if (!(o_members instanceof ArrayList))
            throw new ClassCastException();

        ArrayList al_members = (ArrayList) o_members;

        ArrayList<String> al_p_members = new ArrayList<>();
        for (Object o : al_members)
            if (o instanceof String)
                al_p_members.add((String) o);

        return al_p_members;
    }

    @Override
    public Bundle getAsBundle() throws NotSetException {
        if (! isSet())
            throw new NotSetException();

        Bundle bundle = new Bundle();

        bundle.putBundle(GroupSettingsPickerActivity.GSPA, gspaContainer.getAsBundle());
        bundle.putBundle(FriendPickerActivity.FPA, fpaContainer.getAsBundle());
        bundle.putBundle(PlacePickActivity.PPA, ppaContainer.getAsBundle());

        bundle.putString(ParseCache.OBJECT_ID, getObjectId());

        return bundle;
    }

    @Override
    public void commit() throws NotSetException {
        gspaContainer.commit();
        fpaContainer.commit();
        ppaContainer.commit();

        put(GroupSettingsPickerActivity.GSPA, gspaContainer);
        put(FriendPickerActivity.FPA, fpaContainer);
        put(PlacePickActivity.PPA, ppaContainer);
    }

    @Override
    public void setDefault() throws NotSetException {
        gspaContainer = new GspaContainer();
        gspaContainer.setDefault();

        fpaContainer = new FpaContainer();
        fpaContainer.setDefault();

        ppaContainer = new PpaContainer();
        ppaContainer.setDefault();
    }

    @Override
    public void setFromBundle(Bundle bundle) throws NotSetException {
        setGspaContainer(bundle.getBundle(GroupSettingsPickerActivity.GSPA));
        setFpaContainer(bundle.getBundle(FriendPickerActivity.FPA));
        setPpaContainer(bundle.getBundle(PlacePickActivity.PPA));

        setObjectId(bundle.getString(ParseCache.OBJECT_ID));
        commit();
    }

    @Override
    public boolean isSet() {
        return (ppaContainer != null
                && fpaContainer != null
                && gspaContainer != null
        );
    }
}
