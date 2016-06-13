package in.aajhoga.com;

import android.content.SharedPreferences;
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
        try {
            registerToken(refreshedToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Implement this method to send any registration to your app's servers.
    }

    private void registerToken(String refreshedToken) throws IOException {
        SharedPreferences.Editor editor = getSharedPreferences("hello",MODE_PRIVATE).edit();
        editor.putString("TOKEN",refreshedToken);
        editor.commit();

        new SubscriptionHandler().subscribeTopics();
        if (new Utility().sendTokenToServer(refreshedToken) == true){
            Log.d(LOG_TAG,"Token sent to server");
        }
        else {
            Log.d(LOG_TAG,"Token not send to server");
        }

    }


}
