package in.aajhoga.com;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by aprakhar on 5/27/2016.
 */
public class CircleView extends View {
    Paint mPaint;
    public CircleView(Context context) {
        super(context);
        init();
    }
    public void  init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xff101010);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("Circle class","clsar");
        super.onDraw(canvas);
        canvas.drawCircle(50,50,40,mPaint);
    }
}
