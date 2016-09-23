package in.aajhoga.com;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SetWallpaperListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MAX_RETRY_LIMIT = 5;
    final int rCode = 3;
    private Context mContext;
    private ArrayList<String> bitmapList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences retryCount;
    private wallpaperAdapter adapter;
    private GridView gridView;
    ScrollView mScrollView;
    private ImageView imageOfTheDay;
    ProgressDialog dialog;


    //private ProgressBar progressBar;
    //AnalyticsApplication application;
    //Tracker mTracker;
    Utility mUtility;
    private int mTotalRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       

        mContext = getApplicationContext();
        //progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollView.fullScroll(View.FOCUS_UP);
        gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = bitmapList.get(position);

                Intent intent = new Intent(MainActivity.this, ViewSelectedImage.class);
                intent.putExtra("image", bitmapList.get(position));
                startActivity(intent);
            }
        });
        imageOfTheDay = (ImageView) findViewById(R.id.image_of_the_day);
        imageOfTheDay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.my_logo));


        imageOfTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getSharedPreferences("hello", MODE_PRIVATE).getString("fileName", null) == null) {
                    Toast.makeText(MainActivity.this, "Please wait, fetching image", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ViewSelectedImage.class);
                    intent.putExtra("image", Environment.getExternalStorageDirectory()+"/Bing Images/"+getSharedPreferences("hello", MODE_PRIVATE).getString("fileName", "null"));
                    startActivity(intent);
                }
            }
        });
        //AnalyticsApplication application = (AnalyticsApplication) getApplication();
        // mTracker = application.getDefaultTracker();

        registerForContextMenu(gridView);

        mUtility = new Utility(this);

        sharedPreferences = this.getSharedPreferences("hello", MODE_PRIVATE);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Main Activity");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, rCode);
        } else {
            try {
                toBeExecutedEveryTime();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Please Wait..!!");

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
            Log.d(LOG_TAG, "BroadcastReceiver On Receive Called");
            String val = intent.getStringExtra("Filename");
            String displayname = intent.getStringExtra("Displayname");
            File file = new File(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", displayname);
            Log.d(LOG_TAG, "File sent to BroadCastReceiver " + displayname + " " + file.getAbsolutePath());
            //      progressBar.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(file.getAbsolutePath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(imageOfTheDay);

//            Collections.reverse(List);
//            Collections.reverse(bitmapList);
            Log.d(LOG_TAG, "Total Files" + String.valueOf(bitmapList.size()));
            adapter.notifyDataSetChanged();

            //mTracker.send(new HitBuilders.EventBuilder().setCategory("Foreground").setAction("Share").build());

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Paused");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter(mUtility.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        Log.d(LOG_TAG, "Resumed ");
        //mTracker.send(new HitBuilders.EventBuilder().setCategory("Resume").setAction("Share").build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(LOG_TAG, "Permission Request Start");
        switch (requestCode) {
            case rCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission Granted");
                    try {
                        toBeExecutedEveryTime();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(LOG_TAG, "Permission Not Granted");
                    Toast t = Toast.makeText(getApplicationContext(), "Pls give permission", Toast.LENGTH_SHORT);
                    t.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(LOG_TAG, "Finishing Activity");
                            finish();
                        }
                    }, 1000);
                }
                return;
            }
        }
    }

    @Override
    public void onWallpaperSettingCompleted() {
        dialog.dismiss();
        Toast.makeText(MainActivity.this, "Image set as Wallpaper", Toast.LENGTH_SHORT).show();
    }


    private class loadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            bitmapList = mUtility.getAllFiles();
            Log.d(LOG_TAG, "Total size of bitmapList doInBackground " + bitmapList.size());
            if (bitmapList.size() > 0)
                bitmapList.remove(0);
            if (bitmapList.size() % 3 == 0) {
                mTotalRows = bitmapList.size() / 3;
                Log.d(LOG_TAG, "doInBackground If executed" + mTotalRows);
            } else {
                mTotalRows = bitmapList.size() / 3 + 1;
                Log.d(LOG_TAG, "doInBackground else executed" + mTotalRows);
            }
            adapter = new wallpaperAdapter(mContext, bitmapList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setHeight();
            gridView.setAdapter(adapter);
        }
    }

    private void setHeight() {
        Log.d(LOG_TAG, "roes : " + mTotalRows);
        if (bitmapList.size() == 0) {

        }
        gridView.getLayoutParams().height = (int) (mTotalRows * getResources().getDimension(R.dimen.size_100));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getStringExtra("Displayname") != null) {
            Log.d(LOG_TAG, "onNewIntent");
            String displayname = intent.getStringExtra("Displayname");
            File file = new File(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", displayname);
            Log.d(LOG_TAG, "File sent to onNewIntent" + displayname + " " + file.getAbsolutePath());
            //    progressBar.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(file.getAbsolutePath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(imageOfTheDay);

            //Collections.reverse(bitmapList);
            //bitmapList.add(file.getAbsolutePath());
            //Collections.reverse(bitmapList);
            bitmapList.add(0, file.getAbsolutePath());
            Log.d(LOG_TAG, "Total Files" + String.valueOf(bitmapList.size()));
            adapter.notifyDataSetChanged();
        }
        super.onNewIntent(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.helper_menu, menu);
        Log.d(LOG_TAG, "onCreateOptionsMenu Created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //mTracker.send(new HitBuilders.EventBuilder().setCategory("Start/Stop").setAction("Share").build());
        int id = item.getItemId();
        final String defValue = null;
        final String token = sharedPreferences.getString("TOKEN", defValue);

        //noinspection SimplifiableIfStatement
        if (id == R.id.start_stop) {
            if (item.getTitle().toString().compareTo("Stop Service") == 0) {
                if (token != null) {
                    try {
                        new SubscriptionHandler().unSubscribeTopics();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(LOG_TAG, "onOptionsItemSelected Error Not running");
                }
                item.setTitle("Start Service");
                Log.d(LOG_TAG, "onOptionsItemSelected Start TO Stopped Unsubscribed");
               /* new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {


                    }
                }, 1500);*/
            } else {
                try {
                    new SubscriptionHandler().subscribeTopics();
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
                Log.d(LOG_TAG, "onOptionsItemSelected Stop TO Start Subscribed");
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
        Log.d(LOG_TAG, "onCreateContextMenu Created");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //mTracker.send(new HitBuilders.EventBuilder().setCategory("Wallpaper").setAction("Share").build());
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View v = adapterContextMenuInfo.targetView;
        int clickedPosition = adapterContextMenuInfo.position;
        String val = (String) gridView.getItemAtPosition(clickedPosition);
        Log.d(LOG_TAG, "onContextItemSelected File path Clicked is " + val);
        String[] parts = val.split("/");
        val = parts[parts.length - 1];
        Log.d(LOG_TAG, "onContextItemSelected File path Clicked is After split" + val);

        dialog.show();
        final String finalVal = val;
        SetWallpaperOnUserAction task = new SetWallpaperOnUserAction(this);
        task.setWallpaperListener(this);
        task.execute(finalVal);

        Log.d(LOG_TAG, "onContextItemSelected  Image changed using ContextMenu");
        return super.onContextItemSelected(item);
    }


    public void toBeExecutedEveryTime() throws IOException {
        Log.d(LOG_TAG, "toBeExecutedEveryTime Inside");
        retryCount = getSharedPreferences(mUtility.mRetryCount, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = retryCount.edit();
        editor.putInt(mUtility.mRetryCount, 0);
        editor.commit();

        File sdcard = Environment.getExternalStorageDirectory();
        final File f = new File(sdcard + "/Bing Images");
        String defValue = null;
        String token = sharedPreferences.getString("TOKEN", defValue);

        f.mkdir();

        try {
            File image_of_the_day = new File(sdcard + "/Bing Images", sharedPreferences.getString("fileName", null));
            Log.d(LOG_TAG, "toBeExecutedEveryTime IMage of the day found");
            Glide
                    .with(this)
                    .load(image_of_the_day.getAbsolutePath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(imageOfTheDay);

        } catch (Exception e) {
            Log.d(LOG_TAG, "toBeExecutedEveryTime Image of the day not found");
            e.printStackTrace();
        }

        if (token == null) {
            if (isNetworkAvailable() == true) {
                //      progressBar.setVisibility(View.VISIBLE);
                Log.d(LOG_TAG, "toBeExecutedEveryTime Network Available");
                Log.d(LOG_TAG, "toBeExecutedEveryTime FCM token " + token);


                for (int i = 0; i <= 4; i++) {
                    token = FirebaseInstanceId.getInstance().getToken();
                    if (token == null)
                        continue;
                    else {
                        Log.d(LOG_TAG, "toBeExecutedEveryTime FCM token " + token);

                        try {
                            registerToken(token);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                if (token == null) {
                    Log.d(LOG_TAG, "NOT Able to genetate token ");
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "NetWork not available", Toast.LENGTH_SHORT);
                Log.d(LOG_TAG, "toBeExecutedEveryTime Network Not Available");
                toast.show();

            }
        } else {
            Log.d(LOG_TAG, "toBeExecutedEveryTime Token is not null");
            Log.d(LOG_TAG, "toBeExecutedEveryTime FCM token " + token);
            registerToken(token);

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


    }

    private void registerToken(String refreshedToken) throws IOException {

        SharedPreferences sharedPreferences = getSharedPreferences("hello", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TOKEN", refreshedToken);
        editor.apply();
        //
        new SubscriptionHandler().subscribeTopics();
        if (sharedPreferences.getBoolean("TOKENSENT", false) == false) {
            for (int i = 0; i < 5; i++) {
                if (new Utility().sendTokenToServer(refreshedToken) == true) {
                    Log.d(LOG_TAG, "Token sent to server");
                    editor.putBoolean("TOKENSENT", true);
                    editor.apply();
                    break;
                }
            }
            if (sharedPreferences.getBoolean("TOKENSENT", false) == false) {
                Log.d(LOG_TAG, "Token not sent after 5 retries");
            }

        } else {
            Log.d(LOG_TAG, "Token was already sent to server");
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed Before Finishing");
        super.onBackPressed();
        Log.d(LOG_TAG, "onBackPressed Finishing");
        finish();

    }
}

