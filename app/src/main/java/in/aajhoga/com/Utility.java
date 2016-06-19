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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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
    private CountDownLatch _latch = null;
    private static final int MAX_RETRY_LIMIT = 5;
    private static final String LOG_TAG = Utility.class.getSimpleName();
    private SharedPreferences sp, retryCount;
    private Context context;
    public static final String ACTION = "hello";
    public String mRetryCount = "count";

    private String imageUrl;
    private String imageTitle;

    public Utility() {
        super();

    }

    public void initLatch(CountDownLatch _latch) {
        this._latch = _latch;
    }

    public Utility(Context context) {
        this.context = context;
        init(context);
    }

    public Utility(Context context, CountDownLatch _latch) {
        this.context = context;
        this._latch = _latch;
    }

    private void init(Context mContext) {
        retryCount = mContext.getSharedPreferences(mRetryCount, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = retryCount.edit();
        editor.putInt(mRetryCount, 0);
    }

    public boolean sendTokenToServer(final String token) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                Response response = null;
                try {
                    if (token == null) {
                        Log.d (LOG_TAG, "token is null");
                    }
                    else {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("regID", token)
                                .build();


                        Request request = new Request.Builder()
                                .url("https://helloman-1279.appspot.com")
                                .post(requestBody)
                                .build();

                        if (requestBody == null) {
                            Log.d(LOG_TAG, "request body is null");
                        }
                        response = okHttpClient.newCall(request).execute();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("WWE RESPONSE", response.body().toString());
                    response.body().close();
                }

            }


        }).start();

        return true;
    }

    public ArrayList<String> getAllFiles() {
        ArrayList<String> f = new ArrayList<String>();// list of file paths
        File[] listFile;
        File file = new File(android.os.Environment.getExternalStorageDirectory(), "Bing Images");

        if (file.isDirectory()) {
            listFile = file.listFiles();
            // Log.d("Total files",String.valueOf(listFile.length));


            Arrays.sort( listFile, new Comparator()
            {
                public int compare(Object o1, Object o2) {

                    if (((File)o1).lastModified() > ((File)o2).lastModified()) {
                        return -1;
                    } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
                        return +1;
                    } else {
                        return 0;
                    }
                }

            });


            if (listFile != null) {

                for (int i = 0; i < listFile.length; i++) {

                    f.add(listFile[i].getAbsolutePath());
                    Log.d("WWe", listFile[i].getAbsolutePath());

                }
                //Collections.reverse(f);
            }
            return f;
        } else {
            return f;
        }

    }

    public String downloadImage(String myJsonstring, final String myImageTitle) {
        imageUrl = myJsonstring;
        imageTitle = myImageTitle;
        sp = this.context.getSharedPreferences("hello", Context.MODE_PRIVATE);
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
            try {
                imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {


                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        // if(failReason.getCause() != null)
                        editor.putInt("downloadStatus", 0);
                        editor.apply();
                        Log.d("WWE", "Download failed");
                        //_latch.countDown();

                        retryDownload();

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        //String path = Environment.getExternalStorageDirectory().toString();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        String filename = sdf.format(Calendar.getInstance().getTime());
                        filename = filename + ".jpg";
                        //String filename = String.valueOf(System.currentTimeMillis()) +".jpg";
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
                        editor.putInt("downloadStatus", 1);
                        editor.putString("fileName", filename);
                        editor.apply();

                        try {
                            setImageAsWallpaper(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (isInForeground() == true) {
                            boolean b = LocalBroadcastManager.getInstance(context).sendBroadcast(I);
                            Log.d(LOG_TAG, String.valueOf(b));
                            Log.d(LOG_TAG, "in foreground");
                        } else {
                            createNotification(myImageTitle, filename);
                            Log.d(LOG_TAG, "in background");
                        }


                        //_latch.countDown();
                    }
                });
            } catch (Exception e) {
                Log.d(LOG_TAG, "Every error");
                e.printStackTrace();
            }

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
                        Log.d("wwe", "setted from retrier");
                    } catch (IOException p) {
                        p.printStackTrace();
                    }
                }
            }
            count++;
            editor.putInt(mRetryCount, count);
            editor.commit();
        } else {
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
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Download Failed")
                .setContentText("Image downloading failed after 5 attempts.")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
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
        File file = new File(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", fileName);
        Log.d(LOG_TAG, "setImageAsWallpaper " + fileName + "    " + file.getAbsolutePath());
        Bitmap loadedImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        float aspectRatio = loadedImage.getWidth() /
                (float) loadedImage.getHeight();
        width= (int) (height*aspectRatio);
        loadedImage = Bitmap.createScaledBitmap(loadedImage, width, height, false);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaperManager.setWallpaperOffsetSteps(1, 1);
        wallpaperManager.suggestDesiredDimensions(width, height);
        wallpaperManager.setBitmap(loadedImage);
        if (!loadedImage.isRecycled()) loadedImage.recycle();
    }

    @NonNull
    private static String md5(String s) {
        try {

            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
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

    private void createNotification(String myImageTitle, String filename) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Log.d(LOG_TAG, "creating notificaiton");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                    .setLargeIcon(bitmap)
                    .setColor(Color.BLACK)
                    .setSmallIcon(R.mipmap.ic_googleplaystore)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setContentText(myImageTitle);
            Intent I = new Intent(context, MainActivity.class);
            I.putExtra("Displayname", filename);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, I, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(7, mBuilder.build());

        }
        bitmap.recycle();
    }

    public boolean isInForeground() {
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

    public static void log(final String msg) {

        final Throwable t = new Throwable();
        final StackTraceElement[] elements = t.getStackTrace();

        final String callerClassName = elements[1].getFileName();
        final String callerMethodName = elements[1].getMethodName();

        String TAG = "[" + callerClassName + "]";

        Log.d(TAG, "[" + callerMethodName + "] " + msg);

    }
}
