package in.aajhoga.com;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Prashant on 13/06/16.
 */
public class SetWallpaperOnUserAction {

    private final Context mContext;
    private Utility mUtility;
    SetWallpaperListener mListener;

    public SetWallpaperOnUserAction (Context context) {
        mContext = context;
        init();
    }

    private void init() {
        mUtility = new Utility(mContext);
    }

    public void setWallpaperListener(SetWallpaperListener listener) {
        mListener = listener;
    }

    public void execute(String path) {
        setImageAsWallpaper task = new setImageAsWallpaper();
        task.execute(path);
    }

    public class setImageAsWallpaper extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                mUtility.setImageAsWallpaper(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mListener.onWallpaperSettingCompleted();
        }
    }
}
