package in.aajhoga.com;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by aprakhar on 4/20/2016.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Log.d("id litener","ih");
        startService(new Intent(this, RegistrationIntentService.class));

    }
}
