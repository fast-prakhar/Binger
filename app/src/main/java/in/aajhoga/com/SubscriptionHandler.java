package in.aajhoga.com;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

/**
 * Created by aprakhar on 5/2/2016.
 */
public class SubscriptionHandler {

    public SubscriptionHandler() {
        super();
    }
    public void subscribeTopics() throws IOException {
        FirebaseMessaging.getInstance().subscribeToTopic("global");
        /*
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
    */
    }
    public void unSubscribeTopics() throws IOException {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("global");
        /*
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
        }).start();*/
    }

}
