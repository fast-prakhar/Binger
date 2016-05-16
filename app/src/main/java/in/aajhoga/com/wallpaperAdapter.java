package in.aajhoga.com;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by aprakhar on 5/10/2016.
 */
public class wallpaperAdapter extends BaseAdapter {
    private  ArrayList<Bitmap> list;
    private  Context mContext;
    private  LayoutInflater inflater;

    public wallpaperAdapter(Context mContext,ArrayList<Bitmap> l) {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext=mContext;
        this.list=l;
    }

    public void refresh(ArrayList<Bitmap> l) {
        this.list = l;
        Log.d("hello", list.size() + " check");
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder ;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.grid_single, null);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        //Bitmap myBitmap = BitmapFactory.decodeFile(list.get(position));
        viewHolder.imageView.setImageBitmap(list.get(position));
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}

