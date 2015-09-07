package com.sjgilbert.unanimus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sjgilbert.unanimus.parsecache.ParseCache;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sam on 9/1/15.
 */
@SuppressWarnings("WeakerAccess")
@ParseClassName("UnanimusGroup")
public class UnanimusGroup extends ParseObject {
    private static final String VOTE_CONTAINERS = "voteContainerIds";
    private static final String USER_IDS = "userIds";
    public static final String RESTAURANT_IDS = "restaurantIds";

    private Map<String, VotesList> userIdsVc;
    private List<String> restaurantIds;

    public CgaContainer getCgaContainer() {
        return cgaContainer;
    }

    private CgaContainer cgaContainer;

    public UnanimusGroup() {
        super();
    }

    void load() throws ParseException {
        if (!has(CreateGroupActivity.CGA)
                || !has(VOTE_CONTAINERS)
                || !has(USER_IDS)
                || !has(RESTAURANT_IDS))
            throw new IllegalStateException();

        this.cgaContainer = (CgaContainer) get(CreateGroupActivity.CGA);
        final List<VotesList> voteIds = getList(VOTE_CONTAINERS);
        final List<String> userIds = getList(USER_IDS);
        final List<String> parseRestaurantIds = getList(RESTAURANT_IDS);

        final int numUsers = userIds.size();

        if ((voteIds.size() != numUsers) || (userIds.size() != numUsers))
            throw new IllegalStateException(
                    "Received invalid data while attempting to initialize UnanimusGroup"
            );

        userIdsVc = new Hashtable<>(numUsers);

        for (int i = 0; numUsers > i; ++i) {
            final VotesList votesList;
            try {
                votesList = voteIds.get(i);
            } catch (NullPointerException e) {
                Log.i(UnanimusApplication.UNANIMUS, "Null voteList");
                Log.d(UnanimusApplication.UNANIMUS, e.getMessage(), e);
                continue;
            }

            votesList.load();

            userIdsVc.put(userIds.get(i), voteIds.get(i));
        }

        this.restaurantIds = new ImmutableList<>(parseRestaurantIds);
    }

    private UnanimusGroup(
            Map<String, VotesList> userIdsVc,
            List<String> restaurantIds,
            CgaContainer cgaContainer
    ) {
        this.userIdsVc = userIdsVc;
        this.restaurantIds = restaurantIds;
        this.cgaContainer = cgaContainer;

        commit();
    }

    ListIterator<String> getRestaurantIterator() {
        return restaurantIds.listIterator();
    }

    void vote(
            final int index,
            @NonNull final Vote vote,
            @Nullable final SaveCallback saveCallback
    ) {
        final VotesList votesList = userIdsVc.get(
                ParseUser.getCurrentUser().getObjectId()
        );

        votesList.set(index, vote);
        votesList.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.d(UnanimusApplication.UNANIMUS, e.getMessage(), e);
                    if (saveCallback != null)
                        saveCallback.done(e);

                    return;
                }

                Log.i(
                        UnanimusApplication.UNANIMUS,
                        String.format(
                                Locale.getDefault(),
                                "%s.  %s: %s",
                                "Successfully saved vote container",
                                ParseCache.OBJECT_ID,
                                votesList.getObjectId()
                        )
                );

                if (saveCallback != null)
                    saveCallback.done(null);
            }
        });
    }

    public Collection<String> getMembers() {
        return userIdsVc.keySet();
    }

    private void commit() {
        addAll(VOTE_CONTAINERS, userIdsVc.values());
        addAll(USER_IDS, userIdsVc.keySet());
        addAll(RESTAURANT_IDS, restaurantIds);

        put(CreateGroupActivity.CGA, cgaContainer);
    }

    public static class Builder {
        private static final String[] BOOT_STRAP = new String[]{
                "ChIJh2E4tQIq9ocRmxkXDVB0zZQ",  //blue door
                "ChIJSzSBIxAq9ocRU4zWpADCY2Y",  //neighborhood cafe
                "ChIJsbkY_hgq9ocRN8kZv60pQ6M",  //shish
                "ChIJrSAGxzwq9ocRMEzwa0u133g",  //st clair broiler
                "ChIJ93g6CRcq9ocRAWNTEjCS9Rk",  //st paul cheese shop
                "ChIJw_ls8zwq9ocR6VGmxMFu3mc",  //acme deli
                "ChIJSRkmGRkq9ocRT_kk4bSL3cQ",  //pad thai
                "ChIJgxIuKRkq9ocRwyw6ewF1Z2U",  //indochin
                "ChIJMW0S6RAq9ocRecs9YO5xjjM",  //buffalo wild wings
                "ChIJHQRGthoq9ocRIzv4-kbWuQQ",  //lulus deli
                "ChIJcdWmk4oq9ocRhV33nxJoU8s",  //chipotle
                "ChIJ894-6Ioq9ocRO1KIwauCOzE",  //cafe latte
                "ChIJnSuhY4sq9ocRuLdNgPgCHRA",  //brasa
                "ChIJAVDk_xgq9ocRkuAk36qyZKw",  //french meadow
                "ChIJQ5mqDBcq9ocRy0X2LCioShw"   //jamba juice
        };
        private final CgaContainer cgaContainer;

        public Builder(CgaContainer cgaContainer) {
            if (!cgaContainer.isSet())
                throw new IllegalArgumentException("CgaContainer is not set");

            this.cgaContainer = cgaContainer;
        }

        public void getInBackground(final Callback callback) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!cgaContainer.isSet())
                        throw new IllegalStateException();

                    final ParseUser parseUser = ParseUser.getCurrentUser();

                    final int maxRestaurants = cgaContainer.getMaxRestaurants();

                    final FpaContainer fpaContainer = cgaContainer.getFpaContainer();
                    final PpaContainer ppaContainer = cgaContainer.getPpaContainer();
                    final GspaContainer gspaContainer = cgaContainer.getGspaContainer();

                    final FpaContainer.UserIdPair[] userIdPairs = fpaContainer.getUserIdPairs();

                    final LatLng latLng = ppaContainer.getLatLng();

                    final Date date = gspaContainer.getDate();
                    final int radius = gspaContainer.getRadius();
                    final GspaContainer.EPriceLevel ePriceLevel = gspaContainer.getPriceLevel();

                    final Map<String, VotesList> userIdsVc;
                    final List<String> restaurantIds;

                    userIdsVc = new Hashtable<>(userIdPairs.length);
                    restaurantIds = new ImmutableList<>(maxRestaurants);

                    for (int i = 0; restaurantIds.size() > i; ++i)
                        restaurantIds.set(i, BOOT_STRAP[i]);

                    for (FpaContainer.UserIdPair userIdPair : userIdPairs) {
                        final String voterId = userIdPair.parseUserId;
                        final List<String> readers = new LinkedList<>();

                        for (FpaContainer.UserIdPair idPair : userIdPairs) {
                            final String id = idPair.parseUserId;
                            if (id.contentEquals(voterId)) continue;
                            readers.add(id);
                        }

                        final VotesList votesList = new VotesList(
                                maxRestaurants,
                                parseUser,
                                voterId,
                                readers
                        );

                        userIdsVc.put(voterId, votesList);
                    }

                    callback.done(new UnanimusGroup(userIdsVc, restaurantIds, cgaContainer));
                }
            }).run();

        }

        interface Callback {
            void done(UnanimusGroup unanimusGroup);
        }
    }
}
