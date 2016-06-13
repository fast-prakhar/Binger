package in.aajhoga.com;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aprakhar on 6/9/2016.
 */
public class SplashScreen extends AppCompatActivity {
    private final int rCode = 3;
    private Context mContext;
    private String LOG_TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mContext=getApplicationContext();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashScreen.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, rCode);
                }
            }
        },1500);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(LOG_TAG,"Permission Request Start");
        switch (requestCode) {
            case rCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG,"Permission Granted");
                    startActivity(new Intent(SplashScreen.this,MainActivity.class));

                } else {
                    Log.d(LOG_TAG,"Permission Not Granted");
                    Toast t = Toast.makeText(getApplicationContext(), "Pls give permission", Toast.LENGTH_SHORT);
                    t.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(LOG_TAG,"Finishing Activity");
                            finish();
                        }
                    }, 1000);
                }
                return;
            }
        }
    }
}
