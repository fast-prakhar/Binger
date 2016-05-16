package in.aajhoga.com;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.gcm.GcmListenerService;
import com.logentries.logger.AndroidLogger;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by aprakhar on 4/20/2016.
 */
public class MyGcmListenerService extends GcmListenerService {

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
        createNotification(myImageTitle);
        Log.d("wwe",1+"");
        final Utility t = new Utility(getApplicationContext());

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String z = t.downloadImage((String) params[0],"for now");
                Log.d("wwe",2+" ");
                Log.d("wwe","z is "+z);
                return z;
            }

            @Override
            protected void onPostExecute(Object o) {
                Log.d("wwe",3+"");
                String z = (String) o;
                if (z != "false") {
                    try {
                        t.setImageAsWallpaper(z);
                        Log.d("wwe","set");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(myJsonstring);
        Log.d("wwe",4+"");
    }

    private static String md5(String s) { try {

        // Create MD5 Hash
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
        return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
        return "";

    }


    private  void createNotification(String myImageTitle){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.cast_ic_notification_2)
                    .setContentTitle("GCM")
                    .setAutoCancel(true)
                    .setContentText(myImageTitle);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(7, mBuilder.build());

        }

    }
}
