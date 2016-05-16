package in.aajhoga.com;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

/**
 * Created by aprakhar on 5/2/2016.
 */
public class SubscriptionHandler {
    Context mContext;

    public SubscriptionHandler(Context context) {
        mContext=context;
    }
    public void subscribeTopics(final String token) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                Log.d("WWE",token);
                GcmPubSub pubSub = GcmPubSub.getInstance(mContext);
                try {
                    pubSub.subscribe(token,"/topics/global",null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("WWE","registered");
            }
        }).start();

    }
    public void unSubscribeTopics(final String token) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                Log.d("WWE",token);
                GcmPubSub pubSub = GcmPubSub.getInstance(mContext);
                try {
                    pubSub.unsubscribe(token,"/topics/global");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("WWE","Unregistered");
            }
        }).start();
    }

}
