package in.aajhoga.com;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.IOException;

public class ViewSelectedImage extends AppCompatActivity implements SetWallpaperListener{

    private static final String LOG_TAG = ViewSelectedImage.class.getSimpleName();
    Button setAsWallpaper;
    TextView imageDetail;
    ImageView selectedImage;
    String imagePath;
    String path;
    ProgressDialog dialog;
    Utility mUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_image);

//        getSupportActionBar().setTitle("Image");
        setAsWallpaper = (Button) findViewById(R.id.set_as_wallpaper);
        selectedImage = (ImageView) findViewById(R.id.selected_image);

        imagePath = getIntent().getStringExtra("image");
        path = imagePath;
        Log.d(LOG_TAG, "path : " + android.os.Environment.getExternalStorageDirectory() + "/Bing Images/");
        path = path.replace(android.os.Environment.getExternalStorageDirectory() + "/Bing Images/", "");
        String[] monthName = getResources().getStringArray(R.array.Month);
        Log.d(LOG_TAG, "path :  " + path);
        String date = "" + path.charAt(8) + path.charAt(9);
        Log.d(LOG_TAG, "date :  " + date);
        int month = (path.charAt(5) - '0') * 10 + (path.charAt(6) - '0');
        Log.d(LOG_TAG, "month :  " + month);
        date = date + " " + monthName[month - 1] + ", " + path.charAt(0) + path.charAt(1) + path.charAt(2) + path.charAt(3);

        final SetWallpaperOnUserAction task = new SetWallpaperOnUserAction(this);
        task.setWallpaperListener(this);

        Glide.with(this)
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .into(selectedImage);

        imageDetail = (TextView) findViewById(R.id.image_detail);
        imageDetail.setText(Html.fromHtml(String.format(getResources().getString(R.string.image_of_the_day_on), date)));


        mUtility = new Utility(this);
        setAsWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ProgressDialog(ViewSelectedImage.this);
                dialog.setMessage("Please Wait..!!");
                dialog.show();
                task.execute(path);
            }
        });
    }

    @Override
    public void onWallpaperSettingCompleted() {
        dialog.dismiss();
        Toast.makeText(ViewSelectedImage.this, "Image set as Wallpaper", Toast.LENGTH_SHORT).show();
    }
}
