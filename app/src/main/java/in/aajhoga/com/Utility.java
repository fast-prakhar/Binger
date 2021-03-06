package in.aajhoga.com;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by aprakhar on 5/2/2016.
 */
public class Utility {
    private CountDownLatch _latch=null;
    private static final int MAX_RETRY_LIMIT = 5;
    private static final String LOG_TAG = Utility.class.getSimpleName();
    private SharedPreferences sp, retryCount;
    private Context context;
    public static final String ACTION="hello";
    public String mRetryCount = "count";

    private String imageUrl;
    private String imageTitle;

    public Utility() {
        super();

    }

    public void initLatch(CountDownLatch _latch){
        this._latch=_latch;
    }
    public Utility(Context context){
        this.context=context;
        init(context);
    }

    public Utility(Context context,CountDownLatch _latch){
        this.context=context;
        this._latch=_latch;
    }

    private void init(Context mContext) {
        retryCount = mContext.getSharedPreferences(mRetryCount, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = retryCount.edit();
        editor.putInt(mRetryCount, 0);
    }

    public boolean sendTokenToServer(String token) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("regID",token)
                .build();
        Request request = new Request.Builder()
                .url("https://helloman-1279.appspot.com")
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        Log.d("WWE RESPONSE",response.body().toString());
        response.body().close();
        return true;
    }

    public ArrayList<String> getAllFiles(){
        //List<Bitmap> list = new ArrayList<>();
        ArrayList<String> f = new ArrayList<String>();// list of file paths
        File[] listFile;
        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Bing Images");

        if (file.isDirectory()) {
            listFile = file.listFiles();
            // Log.d("Total files",String.valueOf(listFile.length));
            if (listFile != null) {

                for (int i = 0; i < listFile.length; i++) {

                    f.add(listFile[i].getAbsolutePath());
                    Log.d("WWe", listFile[i].getAbsolutePath());

                }
                Collections.reverse(f);
            }
            return f;
        }
        else {
            return f;
        }

    }

    public String downloadImage(String myJsonstring, final String myImageTitle){
        imageUrl = myJsonstring;
        imageTitle = myImageTitle;
        sp=this.context.getSharedPreferences("hello",Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        String imageMd5;
        String spImageMd5;
        String imageUrl = myJsonstring;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .writeDebugLogs()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        imageMd5 = md5(myJsonstring);

        spImageMd5 = sp.getString("imageMd5", "temp");
        if (spImageMd5 != "temp" || spImageMd5.compareTo(imageMd5) != 0) {
            //Log.d("Past Image", spImageMd5);
            //Log.d("Current Image", imageMd5);
            editor.putString("imageMd5", imageMd5);
            editor.putString("imageTitle", myImageTitle);
            editor.commit();
            final String finalMyImageTitle = myImageTitle;
            imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    // if(failReason.getCause() != null)
                    editor.putInt("downloadStatus",0);
                    editor.apply();
                    Log.d("WWE", "Download failed");
                    //_latch.countDown();

                    retryDownload();

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //String path = Environment.getExternalStorageDirectory().toString();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    //String filename = sdf.format(Calendar.getInstance().getTime());
                    //filename = filename + ".jpg";
                    String filename = String.valueOf(System.currentTimeMillis()) +".jpg";
                    File path = Environment.getExternalStorageDirectory();
                    File file = new File(path + "/Bing Images", filename);
                    Log.d("filename loading", filename);
                    Intent I = new Intent(ACTION);
                    I.putExtra("Filename", finalMyImageTitle);
                    I.putExtra("Displayname", filename);


                    OutputStream fout = null;
                    try {
                        fout = new FileOutputStream(file);
                        Log.d("WWE", "File written");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    loadedImage.compress(Bitmap.CompressFormat.JPEG, 85, fout);
                    try {
                        fout.flush();
                        fout.close();
                        Log.d("WWE", "FLUSHED");

                    } catch (IOException e) {
                        Log.d("WWE ERROR", "Not changed");
                        e.printStackTrace();
                    }
                    editor.putInt("downloadStatus",1);
                    editor.putString("fileName",filename);
                    editor.apply();

                    try {
                        setImageAsWallpaper(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(isInForeground() == true){
                        boolean b = LocalBroadcastManager.getInstance(context).sendBroadcast(I);
                        Log.d(LOG_TAG, String.valueOf(b));
                        Log.d(LOG_TAG,"in foreground");
                    }
                    else {
                        createNotification(myImageTitle,filename);
                        Log.d(LOG_TAG,"in background");
                    }


                    //_latch.countDown();
                }
            });

        }


        return imageMd5;
    }

    private void retryDownload() {
        SharedPreferences.Editor editor = retryCount.edit();
        int count = 0;
        count = retryCount.getInt(mRetryCount, 0);
        if (count < MAX_RETRY_LIMIT) {
            File sdcard = Environment.getExternalStorageDirectory();
            File f = new File(sdcard + "/Bing Images");
            f.mkdir();
            Log.d(LOG_TAG, "STart clicked");
            if (isNetworkAvailable() == true) {
                String e = downloadImage(imageUrl, imageTitle);
                if (e != "false") {
                    try {
                        setImageAsWallpaper(e);
                        Log.d("wwe","setted from retrier");
                    } catch (IOException p) {
                        p.printStackTrace();
                    }
                }
            }
            count++;
            editor.putInt(mRetryCount, count);
            editor.commit();
        }  else {
            generateNotification();
        }
    }

    private void generateNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.cast_ic_notification_1)
                .setContentTitle("Download Failed")
                .setContentText("Image downloading failed after 5 attempts.")
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void setImageAsWallpaper(String fileName) throws IOException {
        File file=new File(android.os.Environment.getExternalStorageDirectory()+"/Bing Images/",fileName);
        Log.d("Displayname from utilit",fileName +" " +  file.getAbsolutePath());
        Bitmap loadedImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaperManager.setBitmap(loadedImage);
        Log.d("wwe", "supported");

    }

    @NonNull
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

    public interface DownloadCompleteListener {
        void onDownloadComplete();
    }

    private  void createNotification(String myImageTitle,String filename) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.cast_ic_notification_2)
                    .setContentTitle("GCM")
                    .setAutoCancel(true)
                    .setContentText(myImageTitle);
            Intent I = new Intent(context,MainActivity.class);
            I.putExtra("Displayname", filename);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,I, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(7, mBuilder.build());

        }
    }

    public  boolean isInForeground() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        }


        return false;
    }

}
