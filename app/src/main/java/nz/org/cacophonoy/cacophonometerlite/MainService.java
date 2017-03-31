package nz.org.cacophonoy.cacophonometerlite;

import android.app.IntentService;


import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by User on 29-Mar-17.
 */

public class MainService extends IntentService {

    private static final String LOG_TAG = MainService.class.getName();

    public MainService(){
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RecordAndUpload.doRecord(getApplicationContext());
    }
}
