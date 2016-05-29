package in.aajhoga.com;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MAX_RETRY_LIMIT = 5;
    final int rCode = 3;
    private Button start;
    private Button stop;
    private TextView textView;
    private Context mContext;
    private ArrayList<String> bitmapList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences retryCount;
    private wallpaperAdapter adapter;
    private GridView gridView;
    private CircleView circleView;
    private LinearLayout linearLayout;
    Utility mUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        circleView = new CircleView(this);
        linearLayout = (LinearLayout) findViewById(R.id.lL);
        linearLayout.addView(circleView);
        */
        mContext = getApplicationContext();
        //start = (Button) findViewById(R.id.start);
        //stop = (Button) findViewById(R.id.stop);
        textView = (TextView) findViewById(R.id.tv1);
        gridView = (GridView) findViewById(R.id.grid_view);
        registerForContextMenu(gridView);

        sharedPreferences = this.getSharedPreferences("hello", MODE_PRIVATE);

        textView.setText(sharedPreferences.getString("imageTitle", "No Image "));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, rCode);
        }
        mUtility = new Utility(this);


        retryCount = getSharedPreferences(mUtility.mRetryCount, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = retryCount.edit();
        editor.putInt(mUtility.mRetryCount, 0);
        editor.commit();

        File sdcard = Environment.getExternalStorageDirectory();
        final File f = new File(sdcard + "/Bing Images");
        String defValue = null;
        String token = sharedPreferences.getString("TOKEN", defValue);

        f.mkdir();
        Log.d("WWEtoken at strt","@@@"+token);
        if (token == null) {
            if (isNetworkAvailable() == true) {
                Log.d("WWE", "going through");
                startService(new Intent(mContext, RegistrationIntentService.class));
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "NetWork not available", Toast.LENGTH_SHORT);
                Log.d("WWE", "Network Not available");
                toast.show();
            }
        }


        new loadImage().execute();
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // String str=gridView.getItemAtPosition(position).toString();
                // Log.d("LONG click",str);
                return false;
            }
        });
        /*
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdcard = Environment.getExternalStorageDirectory();
                File f=new File(sdcard+"/Bing Images");
                f.mkdir();
                Log.d(LOG_TAG, "STart clicked");
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
        */
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


            Log.d("WWE", "On Receive called");
            String val = intent.getStringExtra("Filename");
            textView.setText(val);
            String displayname = intent.getStringExtra("Displayname");
            File file = new File(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", displayname);
            Log.d("Displayname", displayname + " " + file.getAbsolutePath());

            Collections.reverse(bitmapList);
            bitmapList.add(file.getAbsolutePath());
            Collections.reverse(bitmapList);
            Log.d("total files", String.valueOf(bitmapList.size()));
            adapter.notifyDataSetChanged();

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
        IntentFilter iff = new IntentFilter(mUtility.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        textView.setText(sharedPreferences.getString("imageTitle", "No Image"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case rCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new loadImage().execute();

                } else {
                    Toast t = Toast.makeText(getApplicationContext(), "Pls give permission", Toast.LENGTH_SHORT);
                    t.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
                return;
            }
        }
    }


    private class loadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            bitmapList = mUtility.getAllFiles();
            adapter = new wallpaperAdapter(mContext, bitmapList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            gridView.setAdapter(adapter);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getStringExtra("Displayname") != null) {
            Log.d("WWE", "IN New Intent");
            String displayname = intent.getStringExtra("Displayname");
            File file = new File(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", displayname);
            Log.d("Displayname", displayname + " " + file.getAbsolutePath());
            Collections.reverse(bitmapList);
            bitmapList.add(file.getAbsolutePath());
            Collections.reverse(bitmapList);
            Log.d("total files", String.valueOf(bitmapList.size()));
            adapter.notifyDataSetChanged();
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.helper_menu, menu);
        Log.d("wwe", "options menu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final String defValue = null;
        final String token = sharedPreferences.getString("TOKEN", defValue);

        //noinspection SimplifiableIfStatement
        if (id == R.id.start_stop) {
            if (item.getTitle().toString().compareTo("Stop Service") == 0) {
                if (token != null) {
                    try {
                        new SubscriptionHandler(mContext).unSubscribeTopics(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("WWE", "Not Running");
                }
                item.setTitle("Start Service");
                Log.d("WWE", "stop chnged");
               /* new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {


                    }
                }, 1500);*/
            } else {
                try {
                    new SubscriptionHandler(getApplicationContext()).subscribeTopics(token);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                    }
                }, 1500);*/
                item.setTitle("Stop Service");
                Log.d("WWE", "start chnged");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.cntxt_menu, menu);
        Log.d("WWE", "CONTEXT MENU CREATED");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View v = adapterContextMenuInfo.targetView;
        int clickedPosition = adapterContextMenuInfo.position;
        String val = (String) gridView.getItemAtPosition(clickedPosition);
        Log.d("WWE clicked path", val);
        String[] parts = val.split("/");
        val = parts[parts.length - 1];
        Log.d("WWE clicked path", val);
        final String finalVal = val;
        new Thread(new Runnable() {
            public void run() {
                try {
                    new Utility(mContext).setImageAsWallpaper(finalVal);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.d("WWE", "Set using long click listener");
        return super.onContextItemSelected(item);
    }
}

