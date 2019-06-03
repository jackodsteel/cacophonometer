package nz.org.cacophony.birdmonitor.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import nz.org.cacophony.birdmonitor.Prefs;
import nz.org.cacophony.birdmonitor.R;
import nz.org.cacophony.birdmonitor.Server;
import nz.org.cacophony.birdmonitor.Util;

import static nz.org.cacophony.birdmonitor.IdlingResourceForEspressoTesting.registerPhoneIdlingResource;
import static nz.org.cacophony.birdmonitor.MessageHelper.*;

public class RegisterFragment extends Fragment {

    public enum MessageType {
        REGISTER_SUCCESS,
        REGISTER_FAIL,
        REGISTER_ERROR_ALERT
    }

    public static final Action SERVER_REGISTER_ACTION = new Action("SERVER_REGISTER");

    private static final String TAG = "RegisterFragment";


    private Button btnRegister;
    private Button btnUnRegister;
    private TextView tvMessages;
    private EditText etGroupNameInput;
    private EditText etDeviceNameInput;
    private TextView tvTitleMessage;

    private final BroadcastReceiver messageHandler = createReceiver(this::onMessage, MessageType::valueOf);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        setUserVisibleHint(false);

        tvMessages = view.findViewById(R.id.tvMessagesRegister);
        etGroupNameInput = view.findViewById(R.id.etGroupNameInput);
        etDeviceNameInput = view.findViewById(R.id.etDeviceNameInput);

        btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> registerButtonPressed());

        btnUnRegister = view.findViewById(R.id.btnUnRegister);
        btnUnRegister.setOnClickListener(v -> unregisterButtonPressed());

        tvTitleMessage = view.findViewById(R.id.tvTitleMessage);

        return view;
    }


    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (getActivity() == null) {
            return;
        }
        if (visible) {
            registerMessageHandler(SERVER_REGISTER_ACTION, messageHandler, getActivity());

            if (Util.isPhoneRegistered(getActivity())) {
                ((SetupWizardActivity) getActivity()).setNumberOfPagesForRegisterd();
            }

            displayOrHideGUIObjects();


        } else {
            unregisterMessageHandler(messageHandler, getActivity());
        }
    }

    private void onMessage(MessageType messageType, String messageToDisplay) {
        if (getView() == null) {
            return;
        }
        switch (messageType) {
            case REGISTER_SUCCESS:
                tvTitleMessage.setText("Phone is registered");
                tvMessages.setText(messageToDisplay + " Swipe to next screen.");
                ((SetupWizardActivity) getActivity()).setNumberOfPagesForRegisterd();
                etGroupNameInput.setEnabled(false);
                etDeviceNameInput.setEnabled(false);
                break;
            case REGISTER_FAIL:
                tvMessages.setText(messageToDisplay);
                ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
                etGroupNameInput.setEnabled(true);
                etDeviceNameInput.setEnabled(true);
                break;
            default:
                ((SetupWizardActivity) getActivity()).displayOKDialogMessage("Error", messageToDisplay);
                break;
        }
    }

    void displayOrHideGUIObjects() {
        displayOrHideGUIObjects(false);
    }

    void displayOrHideGUIObjects(boolean callSetNumberOfPages) {

        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        String groupNameFromPrefs = prefs.getGroupName();
        String groupNameFromGroupsActivity = ((SetupWizardActivity) getActivity()).getGroup();
        String deviceNameFromPrefs = prefs.getDeviceName();

        tvTitleMessage.setText(getString(R.string.register_title_unregistered));
        tvMessages.setText("");

        if (groupNameFromPrefs == null) {
            // First time user has used this app
            // Phone is not registerd
            btnRegister.setVisibility(View.VISIBLE);
            btnUnRegister.setVisibility(View.INVISIBLE);
            if (callSetNumberOfPages) {
                ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
            }

            if (groupNameFromGroupsActivity == null) {
                // User did not enter a group on Groups screen
                // Display default register screen
                etGroupNameInput.setEnabled(true);
                etDeviceNameInput.setEnabled(true);

                etGroupNameInput.setText("");
                if (deviceNameFromPrefs != null) {
                    etDeviceNameInput.setText(deviceNameFromPrefs);
                } else {
                    etDeviceNameInput.setText("");
                }

            } else {
                // User DID enter a group on Groups screen
                // Display that group
                etGroupNameInput.setEnabled(true);
                etDeviceNameInput.setEnabled(true);

                etGroupNameInput.setText(groupNameFromGroupsActivity);
                if (deviceNameFromPrefs != null) {
                    etDeviceNameInput.setText(deviceNameFromPrefs);
                } else {
                    etDeviceNameInput.setText("");
                }
            }
        } else {
            // User has used this phone before and a group has been saved
            if (groupNameFromGroupsActivity == null) {
                // User did not enter a new group so display the group that they used last time
                etGroupNameInput.setEnabled(true);
                etDeviceNameInput.setEnabled(true);

                etGroupNameInput.setText(groupNameFromPrefs);

                if (deviceNameFromPrefs != null) {
                    // This means that the phone has already been registered so disable the input fields, hide the register button and display the unregister button.
                    if (callSetNumberOfPages) {
                        ((SetupWizardActivity) getActivity()).setNumberOfPagesForRegisterd();
                    }

                    etDeviceNameInput.setText(deviceNameFromPrefs);
                    tvTitleMessage.setText(getString(R.string.register_title_registered));

                    etGroupNameInput.setEnabled(false);
                    etDeviceNameInput.setEnabled(false);
                    btnRegister.setVisibility(View.INVISIBLE);
                    btnUnRegister.setVisibility(View.VISIBLE);

                } else {
                    // This means that the phone is not registered so make sure input fields are enabled and only register button is displayed
                    if (callSetNumberOfPages) {
                        ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
                    }
                    etGroupNameInput.setEnabled(true);
                    etDeviceNameInput.setEnabled(true);
                    btnRegister.setVisibility(View.VISIBLE);
                    btnUnRegister.setVisibility(View.INVISIBLE);

                    etDeviceNameInput.setText("");
                }

            } else { // groupNameFromPrefs and groupNameFromGroupsActivity are not null


                if (groupNameFromPrefs.equals(groupNameFromGroupsActivity)) {
                    // Phone is still registered and the user isn't trying to change groups
                    tvTitleMessage.setText(getString(R.string.register_title_registered)); // Might not need this, but being safe

                } else {
                    // User has entered a new group in the previous group screen
                    // Need to tell the user that they need to first unregister if they want to change groups
                    tvTitleMessage.setText("The phone is currently registered to group " + groupNameFromPrefs + ". You need to un-register before changing groups.");
                }

                if (callSetNumberOfPages) {
                    ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
                }
                etGroupNameInput.setEnabled(true);
                etDeviceNameInput.setEnabled(true);
                //etGroupNameInput.setText(groupNameFromPrefs);
                etGroupNameInput.setText(groupNameFromGroupsActivity);
                if (deviceNameFromPrefs != null) {
                    // May as well display device name that was used with the last group
                    etDeviceNameInput.setText(deviceNameFromPrefs);
                } else {
                    etDeviceNameInput.setText("");
                }

                // Which button to display depends on if it is currently registered
                // As it has got here we already know it has a group in preferences
                if (deviceNameFromPrefs != null) {
                    // Phone is registered so display unregisterd
                    btnRegister.setVisibility(View.INVISIBLE);
                    btnUnRegister.setVisibility(View.VISIBLE);
                } else {
                    // Phone is NOT registered so display register
                    btnRegister.setVisibility(View.VISIBLE);
                    btnUnRegister.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void registerButtonPressed() {

        Prefs prefs = new Prefs(getActivity().getApplicationContext());

        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            tvMessages.setText("The internet connection (in Advanced) has been set 'offline' - so this device can not be registered");
            return;
        }

        if (!Util.isNetworkConnected(getActivity().getApplicationContext())) {
            tvMessages.setText("The phone is not currently connected to the internet - please fix and try again");
            return;
        }

        if (prefs.getGroupName() != null) {
            tvMessages.setText("Already registered - press UNREGISTER first (if you really want to change group)");
            return;
        }
        // Check that the group name is valid, at least 4 characters.
        String group = etGroupNameInput.getText().toString();
        if (group.length() < 1) {
            tvMessages.setText("Please enter a group name of at least 4 characters (no spaces)");
            return;
        } else if (group.length() < 4) {
            tvMessages.setText(group + " is not a valid group name. Please use at least 4 characters (no spaces)");
            return;
        }

        // Check that the device name is valid, at least 4 characters.
        String deviceName = ((EditText) getView().findViewById(R.id.etDeviceNameInput)).getText().toString();
        if (deviceName.length() < 1) {
            tvMessages.setText("Please enter a device name of at least 4 characters (no spaces)");
            return;
        } else if (deviceName.length() < 4) {
            tvMessages.setText(deviceName + " is not a valid device name. Please use at least 4 characters (no spaces)");
            return;
        }

        // Don't allow . (dots) in the device name
        if (deviceName.contains(".")) {
            tvMessages.setText(deviceName + " is not a valid device name. Full stops . are not allowed.");
            return;
        }


        String groupNameFromPrefs = prefs.getGroupName();
        String deviceNameFromPrefs = prefs.getDeviceName();

        // Check to see if the user has actually changed anything from last time it was registerd
        if (groupNameFromPrefs != null && deviceNameFromPrefs != null) {
            if (groupNameFromPrefs.equals(group) && deviceNameFromPrefs.equals(deviceName)) {
                tvMessages.setText("Already registered with those group and device names.");
                return;
            }
        }

        register(group, deviceName, getActivity().getApplicationContext());

        tvMessages.setText("Attempting to register with server - please wait");

        // https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     *
     * @param group name of group to join.
     */
    private void register(final String group, final String deviceName, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
            return;
        }

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getActivity().getApplicationContext(), true)) {
            Log.e(TAG, "Failed to disable airplane mode");
            return;
        }
        registerPhoneIdlingResource.increment();
        new Thread(() -> Server.registerDevice(group, deviceName, context)).start();
    }


    public void unregisterButtonPressed() {
        Prefs prefs = new Prefs(getActivity().getApplicationContext());
        if (prefs.getGroupName() == null) {
            tvMessages.setText("Not currently registered - so can not unregister :-(");
            return;
        }
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton("Yes", (di, id) -> unregister())
                .setNegativeButton("No/Cancel", (di, id) -> { /*Exit the dialog*/ })
                .setMessage("Are you sure?")
                .setTitle("Un-register this phone")
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnPositive = dialog.getButton(Dialog.BUTTON_POSITIVE);
            btnPositive.setTextSize(24);
            int btnPositiveColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
            btnPositive.setTextColor(btnPositiveColor);

            Button btnNegative = dialog.getButton(Dialog.BUTTON_NEGATIVE);
            btnNegative.setTextSize(24);
            int btnNegativeColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.dialogButtonText, null);
            btnNegative.setTextColor(btnNegativeColor);

            //https://stackoverflow.com/questions/6562924/changing-font-size-into-an-alertdialog
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(22);
        });

        dialog.show();
    }

    private void unregister() {

        try {
            Util.unregisterPhone(getActivity().getApplicationContext());
            ((SetupWizardActivity) getActivity()).setNumberOfPagesForSignedInNotRegistered();
            etGroupNameInput.setText("");
            etDeviceNameInput.setText("");

            displayOrHideGUIObjects(true);
            tvMessages.setText("Success - Device is no longer registered");

        } catch (Exception ex) {
            Log.e(TAG, "Error Un-registering device.");
        }

    }

    private void disableFlightMode() {
        try {
            Util.disableFlightMode(getActivity().getApplicationContext());

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
            tvMessages.setText("Error disabling flight mode");
        }
    }


}
