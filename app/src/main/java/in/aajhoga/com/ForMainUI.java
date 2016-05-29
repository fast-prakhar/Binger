package in.aajhoga.com;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

/**
 * Created by aprakhar on 5/6/2016.
 */
public class ForMainUI extends Activity {
    private TextView textView;
    private Context mContext;

    public ForMainUI(Context context) {
        this.mContext=context;
    }

    public void modifyMainUI(String value){
        /*
        textView = (TextView)((Activity)mContext).findViewById(R.id.tv1);
        textView.setText(value);
        Log.d("WWE",textView.getText().toString());
        */
    }
}
