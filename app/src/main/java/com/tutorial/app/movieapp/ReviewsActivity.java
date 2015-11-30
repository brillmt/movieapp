package com.tutorial.app.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReviewsActivity extends AppCompatActivity {

//    ArrayAdapter<String> reviewsAdapter;
    ReviewAdapter reviewsAdapter;
    JSONArray reviewsList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        ListView listView = (ListView) findViewById(R.id.listView_reviews);
        listView.setAdapter(reviewsAdapter);

        Intent i = getIntent();
        if (i != null && i.hasExtra(Intent.EXTRA_TEXT)) {
            String s = i.getStringExtra(Intent.EXTRA_TEXT);

            if (s != null && !s.equals("")) {
                JSONObject js = null;
                try {
                    js = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (js != null) {
                    try {
                        new ReviewsTask().execute(js.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    public class ReviewsTask extends AsyncTask<String, Void, JSONObject[]> {

        //https://api.themoviedb.org/3/movie/157336/videos?api_key=7a3d82f40add8205d2dec33bc580975d
        //ex to get trailers

        private final String LOG_TAG = ReviewsTask.class.getSimpleName();
        private final String BASE_URL = "https://api.themoviedb.org/3/movie/";
        private final String API_KEY = "7a3d82f40add8205d2dec33bc580975d";
        private final String PATH_REVIEWS = "reviews";
        private final String QUERY_PARAM = "api_key";

        @Override
        protected JSONObject[] doInBackground(String... params) {

            if (params.length == 0 || params[0] == null || params[0].equals("")) {
                return null;
            }

            String movieId = params[0];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewList = null;

            try {

                Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendPath(movieId).appendPath(PATH_REVIEWS)
                        .appendQueryParameter(QUERY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                reviewList = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return parseJson(reviewList);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }



            return null;

        }

        @Override
        protected void onPostExecute(JSONObject[] result) {
            if (result != null) {
                reviewsAdapter = new ReviewAdapter(getApplicationContext(), reviewsList);
                ListView listView = (ListView) findViewById(R.id.listView_reviews);
                listView.setAdapter(reviewsAdapter);
            }
        }

        private JSONObject[] parseJson(String jsonStr)
                throws JSONException {

            final String LIST = "results";

            JSONObject jsonObj = new JSONObject(jsonStr);
            reviewsList = jsonObj.getJSONArray(LIST);

            JSONObject[] resultStrs = new JSONObject[reviewsList.length()];

            for(int i = 0; i < reviewsList.length(); i++) {

                JSONObject movie = reviewsList.getJSONObject(i);
                resultStrs[i] = movie;
            }
            return resultStrs;

        }
    }
}