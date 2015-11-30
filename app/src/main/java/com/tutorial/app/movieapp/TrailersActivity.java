package com.tutorial.app.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrailersActivity extends AppCompatActivity {

    private final String LOG_TAG = TrailersActivity.class.getSimpleName();
    ArrayAdapter<String> trailerListAdapter;
    JSONArray trailersList = null;

    private final String PATH_WATCH = "watch";
    private final String BASE_URL_YOUTUBE = "https://www.youtube.com";
    private final String YOUTUBE_VIDEO_PARAM = "v";
//    private ArrayAdapter<ButtonView> ia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailers);


        String[] data = {
        };

        List<String> holderList = new ArrayList<>(Arrays.asList(data));

        trailerListAdapter =
                new ArrayAdapter<>(
                        getApplicationContext(),
                        R.layout.trailer_item,
                        R.id.trailer_item_button,
                        holderList);

        ListView listView = (ListView)findViewById(R.id.listView_trailers);
        listView.setAdapter(trailerListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                JSONObject jo;
                String trailerKey = "";
                try {
                    jo = trailersList.getJSONObject(position);
                    trailerKey = jo.getString("key");
                    Log.e(LOG_TAG, trailerKey);
                    Log.e(LOG_TAG, jo.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Uri builtUri = Uri.parse(BASE_URL_YOUTUBE).buildUpon().appendPath(PATH_WATCH)
                        .appendQueryParameter(YOUTUBE_VIDEO_PARAM, trailerKey)
                        .build();

                Log.e(LOG_TAG, builtUri.toString());

                startActivity(new Intent(Intent.ACTION_VIEW, builtUri));
            }
        });


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
                        new TrailersTask().execute(js.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public class TrailersTask extends AsyncTask<String, Void, JSONObject[]> {

        private final String LOG_TAG = TrailersTask.class.getSimpleName();
        private final String PATH_VIDEOS = "videos";
        private final String QUERY_PARAM = "api_key";

        @Override
        protected JSONObject[] doInBackground(String... params) {

            if (params.length == 0 || params[0] == null || params[0].equals("")) {
                return null;
            }

            String movieId = params[0];


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailerList = null;

            try {

                Uri builtUri = Uri.parse(Utility.BASE_URL).buildUpon().appendPath(movieId).appendPath(PATH_VIDEOS)
                        .appendQueryParameter(QUERY_PARAM, Utility.API_KEY)
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
                trailerList = buffer.toString();
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
                return parseJson(trailerList);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }



            return null;

        }

        @Override
        protected void onPostExecute(JSONObject[] result) {
            if (result != null) {
                trailerListAdapter.clear();
                for(JSONObject trailerObj : result) {

                    try {
                        trailerListAdapter.add(getTrailerName(trailerObj));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }

                }
                trailerListAdapter.notifyDataSetChanged();
            }
        }

        private JSONObject[] parseJson(String jsonStr)
                throws JSONException {

            final String LIST = "results";

            JSONObject jsonObj = new JSONObject(jsonStr);
            trailersList = jsonObj.getJSONArray(LIST);

            JSONObject[] resultStrs = new JSONObject[trailersList.length()];

            for(int i = 0; i < trailersList.length(); i++) {

                JSONObject movie = trailersList.getJSONObject(i);
                resultStrs[i] = movie;
            }
            return resultStrs;

        }

        private String getTrailerName(JSONObject jsonObject)
                throws JSONException {
            return jsonObject.getString("name");

        }
    }
}
