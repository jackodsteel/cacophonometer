package nz.org.cacophony.birdmonitor.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.util.LinkifyCompat;
import androidx.fragment.app.Fragment;

import nz.org.cacophony.birdmonitor.MessageHelper;
import nz.org.cacophony.birdmonitor.PermissionsHelper;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.RecordAndUpload;
import nz.org.cacophony.birdmonitor.RecordingsHelper;
import nz.org.cacophony.birdmonitor.StartRecordingReceiver;

import static nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment.MANAGE_RECORDINGS_ACTION;

public class TestRecordFragment extends Fragment {

    private static final String TAG = "TestRecordFragment";

    private Button btnRecordNow;
    private TextView tvTitleMessage;
    private TextView tvMessages;
    private TextView tvServerLink;
    private final BroadcastReceiver messageHandler =
            RecordingsHelper.createMessageHandler(TAG, message -> tvMessages.setText(message), this::onRecordingFinished);
    private PermissionsHelper permissionsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_record, container, false);

        setUserVisibleHint(false);
        tvTitleMessage = view.findViewById(R.id.tvTitleMessage);
        tvMessages = view.findViewById(R.id.tvMessages);
        tvServerLink = view.findViewById(R.id.tvServerLink);

        Button btnNext = view.findViewById(R.id.btnFinished);
        btnNext.setOnClickListener(v -> ((SetupWizardActivity) getActivity()).nextPageView());

        btnRecordNow = view.findViewById(R.id.btnRecordNow);
        btnRecordNow.setOnClickListener(v -> recordNowButtonPressed());

        // Turn the words 'Cacophony Server' in the text view into a link
        // https://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-textview-clickable
        // and
        // https://android-developers.googleblog.com/2008/03/linkify-your-text.html

        // Note also used the following to set the color of the url link using the xml method
        // https://stackoverflow.com/questions/13520193/android-linkify-how-to-set-custom-link-color

        TextView tvServerLink = view.findViewById(R.id.tvServerLink);
        Prefs prefs = new Prefs(getActivity());
        String tvServerLinkText = tvServerLink.getText() + " " + prefs.getBrowseRecordingsServerUrl();
        tvServerLink.setText(tvServerLinkText);
        LinkifyCompat.addLinks(tvServerLink, Linkify.ALL);
        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {

        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            MessageHelper.registerMessageHandler(MANAGE_RECORDINGS_ACTION, messageHandler, getActivity());
            checkPermissions();
            displayOrHideGUIObjects();
        } else {
            MessageHelper.unregisterMessageHandler(messageHandler, getActivity());
        }
    }

    void displayOrHideGUIObjects() {
        tvMessages.setText("");

        if (RecordAndUpload.isRecording) {
            getView().findViewById(R.id.btnRecordNow).setEnabled(false);
            tvTitleMessage.setText(getString(R.string.record_in_progress));
            btnRecordNow.setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.btnRecordNow).setEnabled(true);
            tvTitleMessage.setText(getString(R.string.press_record_now));
            btnRecordNow.setVisibility(View.VISIBLE);
        }
    }

    public void recordNowButtonPressed() {
        recordNow();
    }

    public void recordNow() {
        btnRecordNow.setEnabled(false);

        Intent myIntent = new Intent(getActivity(), StartRecordingReceiver.class);
        myIntent.putExtra("callingCode", "recordNowButtonClicked"); // for debugging
        try {
            myIntent.putExtra(Prefs.INTENT_TYPE, Prefs.RECORD_NOW_ALARM);
            getActivity().sendBroadcast(myIntent);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    public void onRecordingFinished() {
        tvServerLink.setVisibility(View.VISIBLE);
        btnRecordNow.setEnabled(true);
        btnRecordNow.setVisibility(View.VISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE);
    }

}
