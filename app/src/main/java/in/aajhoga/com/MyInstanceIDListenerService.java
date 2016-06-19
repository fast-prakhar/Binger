package in.aajhoga.com;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

/**
 * Created by aprakhar on 4/20/2016.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private String LOG_TAG = this.getClass().getSimpleName();

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(MyInstanceIDListenerService.class.getSimpleName(), "Refreshed token: " + refreshedToken);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG,"Permission already given moving forward with registerToken");
            try {
                registerToken(refreshedToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.d(LOG_TAG,"Permission not given");
        }

        // TODO: Implement this method to send any registration to your app's servers.
    }

    private void registerToken(String refreshedToken) throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences("hello",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TOKEN", refreshedToken);
        editor.apply();

        new SubscriptionHandler().subscribeTopics();
        if(sharedPreferences.getBoolean("TOKENSENT",false) == false) {
            for(int i = 0 ;i<5;i++){
                if (new Utility().sendTokenToServer(refreshedToken) == true) {
                    Log.d(LOG_TAG, "Token sent to server");
                    editor.putBoolean("TOKENSENT", true);
                    editor.apply();
                    break;
                }
            }
            if(sharedPreferences.getBoolean("TOKENSENT",false) == false){
                Log.d(LOG_TAG,"Token not sent after 5 retries");
            }

        }
        else {
            Log.d(LOG_TAG,"Token was already sent to server");
        }

    }


}
