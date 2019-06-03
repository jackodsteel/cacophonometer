package nz.org.cacophony.birdmonitor.views;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.*;
import nz.org.cacophony.birdmonitor.MessageHelper.Action;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GPSFragment extends Fragment {

    public enum GpsMessageType {
        GPS_UPDATE_SUCCESS,
        GPS_UPDATE_FAILED
    }

    public enum RootMessageType {
        ERROR_DO_NOT_HAVE_ROOT
    }

    public static final Action GPS_ACTION = new Action("GPS");

    public static final Action ROOT_ACTION = new Action("ROOT");

    private static final String TAG = "GPSFragment";

    private TextView tvMessages;
    private TextView tvSearching;
    private TextView latitudeDisplay;
    private TextView longitudeDisplay;

    private PermissionsHelper permissionsHelper;

    private final BroadcastReceiver rootMessageHandler = MessageHelper.createReceiver((messageType, ignored) -> onRootMessage(messageType), RootMessageType::valueOf);
    private final BroadcastReceiver gpsMessageHandler = MessageHelper.createReceiver(this::onGpsMessage, GpsMessageType::valueOf);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gps, container, false);

        setUserVisibleHint(false);
        tvMessages = view.findViewById(R.id.tvMessages);
        tvSearching = view.findViewById(R.id.tvSearching);
        latitudeDisplay = view.findViewById(R.id.tvLatitude);
        longitudeDisplay = view.findViewById(R.id.tvLongitude);

        Button btnGetGPSLocation = view.findViewById(R.id.btnGetGPSLocation);
        btnGetGPSLocation.setOnClickListener(v -> updateGPSLocationButtonPressed());

        return view;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            MessageHelper.registerMessageHandler(ROOT_ACTION, rootMessageHandler, getActivity());
            MessageHelper.registerMessageHandler(GPS_ACTION, gpsMessageHandler, getActivity());

            checkPermissions();

            updateGpsDisplay();

        } else {

            MessageHelper.unregisterMessageHandler(rootMessageHandler, getActivity());
            MessageHelper.unregisterMessageHandler(gpsMessageHandler, getActivity());
        }
    }

    private void updateGPSLocationButtonPressed() {
        // First check to see if Location service is available
        // https://stackoverflow.com/questions/25175522/how-to-enable-location-access-programmatically-in-android
        if (!canGetLocation()) {
            // Display dialog
            displayMessage();
            return;
        }

        TextView latitudeDisplay = getView().findViewById(R.id.tvLatitude);
        TextView longitudeDisplay = getView().findViewById(R.id.tvLongitude);
        latitudeDisplay.setText(getString(R.string.latitude));
        longitudeDisplay.setText(getString(R.string.longitude));

        tvSearching.setVisibility(View.VISIBLE);
        Util.updateGPSLocation(getActivity().getApplicationContext());
    }

    private void onGpsMessage(GpsMessageType messageType, String messageToDisplay) {
        if (getView() == null) {
            return;
        }
        switch (messageType) {
            case GPS_UPDATE_SUCCESS:
                updateGpsDisplay();
                break;
            case GPS_UPDATE_FAILED:
                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", messageToDisplay);
                tvSearching.setVisibility(View.GONE);
                break;
        }
    }

    private void onRootMessage(RootMessageType messageType) {
        if (getView() == null) {
            return;
        }
        if (messageType == RootMessageType.ERROR_DO_NOT_HAVE_ROOT) {
            String messageToDisplay = "It looks like you have incorrectly indicated in settings that this phone has been rooted";
            ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Oops", messageToDisplay);
        }
    }

    private void updateGpsDisplay() {
        try {

            Prefs prefs = new Prefs(getActivity());
            tvMessages.setText("");
            latitudeDisplay = getView().findViewById(R.id.tvLatitude);
            longitudeDisplay = getView().findViewById(R.id.tvLongitude);

            tvSearching.setVisibility(View.GONE);

            double lat = prefs.getLatitude();
            double lon = prefs.getLongitude();

            if (lat != 0 && lon != 0) {
                //http://www.coderzheaven.com/2012/10/14/numberformat-class-android-rounding-number-android-formatting-decimal-values-android/
                NumberFormat numberFormat = new DecimalFormat("#.000000");
                String latStr = numberFormat.format(lat);
                String lonStr = numberFormat.format(lon);

                latitudeDisplay.setText(getString(R.string.latitude) + ": " + latStr);
                longitudeDisplay.setText(getString(R.string.longitude) + ": " + lonStr);

            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
        }
    }

    private boolean canGetLocation() {

        LocationManager lm = null;
        boolean gps_enabled = false;

        if (lm == null)

            lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {

        }

        return gps_enabled;
    }

    private void displayMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("OK", (dialog, id) -> {
            Intent intent2 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent2);
        });

        builder.setMessage("Your phone\'s Location service is Off.  Press OK, to be taken to settings, and turn Location ON.  Then press your phone\'s back button to return here and press the UPDATE GPS LOCATION again.")
                .setTitle("Please turn on your phone\'s location service.");

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsHelper.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        permissionsHelper = new PermissionsHelper();
        permissionsHelper.checkAndRequestPermissions(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

}
