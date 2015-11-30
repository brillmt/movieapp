package com.tutorial.app.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mbrillantes on 11/17/15.
 */
public class ReviewAdapter extends BaseAdapter {


    public static class ReviewViewHolder {
        public final TextView author;
        public final TextView review;

        public ReviewViewHolder(View view) {
            author = (TextView) view.findViewById(R.id.review_author);
            review = (TextView) view.findViewById(R.id.review_description);
        }
    }
    private final String LOG_TAG = ReviewAdapter.class.getSimpleName();
    private final Context context;
    private final JSONArray list;

    public ReviewAdapter(Context context, JSONArray list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        JSONObject jo = null;
        String auth = null;
        String review = null;
        try {
            jo = list.getJSONObject(position);
            auth = jo.getString("author");
            review = jo.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ReviewViewHolder holder = new ReviewViewHolder(view);
        holder.author.setText(auth);
        holder.review.setText(review);
        view.setTag(holder);
        return view;
    }

    @Override
    public int getCount() {
        return list.length();
    }

    @Override
    public JSONObject getItem(int position) {
        try {
            return list.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        try {
            JSONObject j = list.getJSONObject(position);
            return j.getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

/**
     * USE LATER WHEN WORKING WITH TABLET
     */
//    @Override
//    public int getItemViewType(int position) {
//        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        return VIEW_TYPE_COUNT;
//    }
}
