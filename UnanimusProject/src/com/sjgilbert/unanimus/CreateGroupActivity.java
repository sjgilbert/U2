package com.sjgilbert.unanimus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseACL;
import com.parse.ParseUser;
import com.sjgilbert.unanimus.unanimus_activity.UnanimusActivityTitle;

import java.util.Locale;

import static com.sjgilbert.unanimus.FriendPickerActivity.FPA;
import static com.sjgilbert.unanimus.PlacePickActivity.PPA;

/**
 * Activity for creating group.  Calls 3 other activities for input to build group.
 */
public class CreateGroupActivity extends UnanimusActivityTitle {
    private static final String CGA = "cga";
    private final int FPA_REQUEST = 1;
    private final int PPA_REQUEST = 2;
    private final int GSPA_REQUEST = 3;

    private final UnanimusGroup unanimusGroup = new UnanimusGroup();

    public CreateGroupActivity() {
        super(CGA);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lauchNext();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log(
                ELog.i,
                String.format(
                        Locale.getDefault(),
                        "%s.  %s: %d  %s: %d",
                        "Received result",
                        "Request code",
                        requestCode,
                        "Result code",
                        resultCode
                )
        );
        if (RESULT_OK != resultCode) {
            log(
                    ELog.w,
                    "CreateGroupActivity got non-OK result from activity"
            );
            return;
        }

        switch (requestCode) {
            case GSPA_REQUEST:
                processGspaResult(data);
                break;
            case FPA_REQUEST:
                processFpaResult(data);
                break;
            case PPA_REQUEST:
                processPpaResult(data);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        lauchNext();
    }

    private void lauchNext() {
        if (! unanimusGroup.getGspaContainer().isSet()) {
            startGspaForResult();
        } else if (! unanimusGroup.getPpaContainer().isSet()) {
            startPpaForResult();
        } else if (! unanimusGroup.getFpaContainer().isSet()) {
            startFpaForResult();
        } else {
            finish();
        }
    }

    private void startGspaForResult() {
        startActivityForResult(
                new Intent(this, GroupSettingsPickerActivity.class),
                GSPA_REQUEST
        );
    }

    private void setGspaContainer(Bundle bundle) {
        unanimusGroup.setGspaContainer(bundle);
    }

    private void processGspaResult(Intent data) {
        final Bundle gspaBundle = data.getBundleExtra(GroupSettingsPickerActivity.GSPA);
        setGspaContainer(gspaBundle);
    }

    private void startFpaForResult() {
        startActivityForResult(
                new Intent(this, FriendPickerActivity.class),
                FPA_REQUEST
        );
    }

    private void setFpaContainer(Bundle bundle) {
        unanimusGroup.setFpaContainer(bundle);
    }

    private void processFpaResult(Intent data) {
        Bundle fpaBundle = data.getBundleExtra(FPA);
        setFpaContainer(fpaBundle);
    }

    private void startPpaForResult() {
        startActivityForResult(
                new Intent(this, PlacePickActivity.class),
                PPA_REQUEST
        );
    }

    private void setPpaContainer(Bundle bundle) {
        unanimusGroup.setPpaContainer(bundle);
    }

    private void processPpaResult(Intent data) {
        Bundle ppaBundle = data.getBundleExtra(PPA);
        setPpaContainer(ppaBundle);
    }

    static abstract class ADependencyContainer {
        Bundle getAsBundle() throws NotSetException {
            if (!isSet())
                throw new NotSetException();
            return null;
        }

        abstract void setDefault();

        @Deprecated
        abstract void setFromBundle(Bundle bundle);

        abstract boolean isSet();

        public static class NotSetException extends Exception {
            private NotSetException() {
                // Private instantiation
            }
        }
    }
}
