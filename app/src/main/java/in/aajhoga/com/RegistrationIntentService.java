package in.aajhoga.com;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by aprakhar on 4/20/2016.
 */
public class RegistrationIntentService extends IntentService {
    private static String TAG = "WWE";
    private static final String[] TOPICS = {"global"};

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public RegistrationIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            synchronized (TAG) {
                // Initially a network call, to retrieve the token, subsequent calls are local.
                final String defValue=null;
                SharedPreferences sp = this.getSharedPreferences("hello",MODE_PRIVATE);
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.i(TAG, "GCM Registration Token: " + token);

                SharedPreferences.Editor editor = sp.edit();
                editor.putString("TOKEN",token);
                editor.commit();

                new SubscriptionHandler(getApplicationContext()).subscribeTopics(token);
                if (new Utility().sendTokenToServer(token) == true){

                }

                sharedPreferences.edit().putBoolean(getString(R.string.pref_key_SENT_TOKEN_TO_SERVER), true).apply();

            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(getString(R.string.pref_key_SENT_TOKEN_TO_SERVER), false).apply();
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(getString(R.string.intent_name_REGISTRATION_COMPLETE)));
    }


}
