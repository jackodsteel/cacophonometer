package nz.org.cacophony.birdmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.util.Consumer;
import nz.org.cacophony.birdmonitor.views.ManageRecordingsFragment;


public class RecordingsHelper {

    /**
     * Creates a specialised BroadcastReceiver that is specific to the {@link ManageRecordingsFragment#MANAGE_RECORDINGS_ACTION} type.
     * This class will updateTvMessages on appropriate messages, and will also call the provided method on RECORDING_FINISHED.
     * @param updateTvMessage A function that takes a String, which should call a method to display the given message
     * @param onFinished Code to run upon receiving the {@link ManageRecordingsFragment.MessageType#RECORDING_FINISHED} message.
     * @return The BroadcastReceiver which then needs to be registered using the {@link MessageHelper#registerMessageHandler(MessageHelper.Action, BroadcastReceiver, Context)}
     */
    public static BroadcastReceiver createMessageHandler(Consumer<String> updateTvMessage, Runnable onFinished) {
        return MessageHelper.createReceiver((messageType, messageToDisplay) -> {
                switch (messageType) {
                    case RECORDING_DISABLED:
                    case NO_PERMISSION_TO_RECORD:
                    case UPLOADING_RECORDINGS:
                    case GETTING_READY_TO_RECORD:
                    case FAILED_RECORDINGS_NOT_UPLOADED:
                    case UPLOADING_FAILED_NOT_REGISTERED:
                    case RECORDING_STARTED:
                    case ALREADY_RECORDING:
                    case UPLOADING_FAILED:
                    case UPLOADING_FINISHED:
                    case RECORD_AND_UPLOAD_FAILED:
                        updateTvMessage.accept(messageToDisplay);
                        break;
                    case RECORDING_FINISHED:
                        updateTvMessage.accept(messageToDisplay);
                        onFinished.run();
                        break;
                }
        }, ManageRecordingsFragment.MessageType::valueOf);
    }
}
