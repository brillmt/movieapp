package com.tutorial.app.movieapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by mbrillantes on 9/29/15.
 */
public class ImageAdapter extends BaseAdapter {
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();
    private Context mContext;
    private List<JSONObject> list;
    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w500";

    public ImageAdapter(Context c, List<JSONObject> a) {
        mContext = c;
        list = a;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        JSONObject movieUrl = list.get(position);
        String address = null;
        try {
            address = movieUrl.getString("poster_path");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        if (address == null){
            return null;
        }

        final Uri builtUri = Uri.parse(BASE_IMAGE_URL+address).buildUpon()
                .build();

        Picasso.with(mContext)
                .load(builtUri)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "Unable to get " + builtUri.toString());
                    }
                });

        return imageView;
    }


}
