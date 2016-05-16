package in.aajhoga.com;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by aprakhar on 5/2/2016.
 */
public class Utility {
    private SharedPreferences sp;
    private Context context;
    public static final String ACTION="hello";
    public String mRetryCount = "count";


    ImageDownloadFailedListener mListener;

    public Utility() {
        super();
    }
    public Utility(Context context){
        this.context=context;
    }

    public void setImageDownloadFailedListener(ImageDownloadFailedListener mListener) {
        this.mListener = mListener;
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

    public ArrayList<Bitmap> getAllFiles(){
        //List<Bitmap> list = new ArrayList<>();
        ArrayList<Bitmap> f = new ArrayList<Bitmap>();// list of file paths
        File[] listFile;
        File file= new File(android.os.Environment.getExternalStorageDirectory(),"Bing Images");

        if (file.isDirectory())
        {
            listFile = file.listFiles();
           // Log.d("Total files",String.valueOf(listFile.length));

            for (int i = 0; i < listFile.length; i++)
            {

                f.add(BitmapFactory.decodeFile(listFile[i].getAbsolutePath()));
                Log.d("WWe",listFile[i].getAbsolutePath());

            }
            Collections.reverse(f);
        }
        return f;
    }

    public String downloadImage(String myJsonstring,String myImageTitle){
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
                    mListener.onDownloadFailed();

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //String path = Environment.getExternalStorageDirectory().toString();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    String filename = sdf.format(Calendar.getInstance().getTime());
                    filename = filename + ".jpg";
                    File path = Environment.getExternalStorageDirectory();
                    File file = new File(path + "/Bing Images", filename);
                    Log.d("filename", filename);
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
                    boolean b = LocalBroadcastManager.getInstance(context).sendBroadcast(I);
                    Log.d("WWE", String.valueOf(b));
                }
            });

        }

        if (sp.getInt("downloadStatus",2) == 0 || sp.getInt("downloadStatus",2) == 2 ){
            return "false";
        }
        return sp.getString("fileName",null);
    }


    public void setImageAsWallpaper(String fileName) throws IOException {
        File file=new File(android.os.Environment.getExternalStorageDirectory()+"/Bing Images/",fileName);
        Log.d("Displayname",fileName +" " +  file.getAbsolutePath());
        Bitmap loadedImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaperManager.setBitmap(loadedImage);
        Log.d("wwe", "supported");

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


}
