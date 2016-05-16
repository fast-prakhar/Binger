package in.aajhoga.com;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.logentries.logger.AndroidLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {
    final int rCode=3;
    private Button start;
    private Button stop;
    private TextView textView;
    private AndroidLogger logger = null;
    private Context mContext;
    private ArrayList<Bitmap> bitmapList;
    private SharedPreferences sharedPreferences;
    private wallpaperAdapter adapter;
    private GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        textView = (TextView) findViewById(R.id.tv1);
        gridView = (GridView)findViewById(R.id.grid_view);
        sharedPreferences = getSharedPreferences("hello", MODE_PRIVATE);
        textView.setText(sharedPreferences.getString("imageTitle","No Image "));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, rCode);
        }

        new loadImage().execute();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdcard = Environment.getExternalStorageDirectory();
                File f=new File(sdcard+"/Bing Images");
                f.mkdir();
                Log.d("WWE", "STart clicked");
                if (isNetworkAvailable() == true) {
                    startService(new Intent(mContext, RegistrationIntentService.class));
                }

                    else {
                    Toast toast = Toast.makeText(getApplicationContext(), "NetWork not available", Toast.LENGTH_SHORT);
                    Log.d("WWE", "Network Not available");
                    toast.show();
                }

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("WWE", "Stop clicked");
                String defValue = null;
                String currentValue = sharedPreferences.getString("TOKEN", defValue);
                if (currentValue != null) {
                    try {
                        new SubscriptionHandler(mContext).unSubscribeTopics(currentValue);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("WWE", "Not Running");
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("WWE","On Receive called");
            String val = intent.getStringExtra("Filename");
            textView.setText(val);
            String displayname=intent.getStringExtra("Displayname");
            File file=new File(android.os.Environment.getExternalStorageDirectory()+"/Bing Images/",displayname);
            Log.d("Displayname",displayname +" " +  file.getAbsolutePath());
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            Collections.reverse(bitmapList);
            bitmapList.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
            Collections.reverse(bitmapList);
            Log.d("total files",String.valueOf(bitmapList.size()));
           // adapter.refresh(bitmapList);
            adapter.notifyDataSetChanged();
            //here add notifydataset changed after adding image to bitmap array
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter(Utility.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        textView.setText(sharedPreferences.getString("imageTitle","No Image "));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case rCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast t = Toast.makeText(getApplicationContext(), "Pls give permission", Toast.LENGTH_SHORT);
                    t.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1000);
                }
                return;
            }
        }
    }


    private class loadImage extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            bitmapList=new Utility().getAllFiles();
            adapter = new wallpaperAdapter(mContext,bitmapList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            gridView.setAdapter(adapter);
        }
    }


}
