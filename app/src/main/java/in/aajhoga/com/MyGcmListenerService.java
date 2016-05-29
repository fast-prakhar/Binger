package in.aajhoga.com;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aprakhar on 4/20/2016.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static CountDownLatch _latch;
    private static int N = 3;
    private SharedPreferences sp;
    @Override
    public void onCreate() {
        /*
        try {
            logger = AndroidLogger.createInstance(getApplicationContext(), true, false, false, null, 0, "847369a6-8b5f-4a6e-b179-8de7d9682bef", true);
            Log.d("happened",logger.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        sp = this.getSharedPreferences("hello", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Log.d("WWE", "KUCH AAYA");
        int c = 1;
        String myJsonstring = "";
        String myImageTitle = "";
        myJsonstring = data.getString("downloadURL");
        myImageTitle = data.getString("Title");
        editor.putString("URL", myJsonstring);

        editor.apply();
        Log.d("wwe",1+"");
        String z=null;
        _latch = new CountDownLatch(N);
        final Utility t = new Utility(getApplicationContext());
        new Utility(getApplicationContext()).downloadImage(myJsonstring, myImageTitle);
        /*
        try {
            for (int i = 0; i < N; i++) {
                Log.d("wwe","in for loop");
                new Utility(getApplicationContext(),_latch).downloadImage(myJsonstring, "fro noe");
                Log.d("wwe","After call");
                if(i==1) {
                    Log.d("wwe", "exitedfor loop");
                    break;
                }
            }
            _latch.await();
            Log.d("wwe","out for loop");
            if (sp.getInt("downloadStatus",2) == 0 || sp.getInt("downloadStatus",2) == 2 || sp.getString("fileName",null) == null){
                Log.d("wwe","failure");
            }
            else {
                try {
                    t.setImageAsWallpaper(sp.getString("fileName",null));
                    Log.d("wwe","set");
                    Log.d("wwe else",sp.getString("fileName",null)+"kuch hai");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }


}
