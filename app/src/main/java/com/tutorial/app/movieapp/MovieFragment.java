package com.tutorial.app.movieapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.tutorial.app.movieapp.model.Movie;

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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private List<JSONObject> imageList = new ArrayList<>();
    private ImageAdapter ia;
//    @Bind(R.id.grid_view_layout_main) GridView listView;


    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        ButterKnife.bind(getActivity(), getView());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ia = new ImageAdapter(getActivity(), imageList);

        GridView listView = (GridView) rootView.findViewById(R.id.grid_view_layout_main);
        listView.setAdapter(ia);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                JSONObject js = imageList.get(position);
                Intent intent = new Intent(getActivity(), DetailsActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, js.toString());
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    private void updateList() {
        Log.e(LOG_TAG, "Updating list");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortByPreference = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
        new MovieTask().execute(sortByPreference);
    }

    public class MovieTask extends AsyncTask<String, Void, JSONObject[]> {

        private final String LOG_TAG = MovieTask.class.getSimpleName();
        private final String TITLE = "title";
        private final String POSTER_PATH = "poster_path";
        private final String PLOT = "overview";
        private final String USER_RATING = "vote_average";
        private final String RELEASE_DATE = "release_date";

        private JSONObject[] parseMovieJson(String jsonStr)
                throws JSONException {

            final String LIST = "results";

            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray movieList = jsonObj.getJSONArray(LIST);

            JSONObject[] resultStrs = new JSONObject[movieList.length()];

            for(int i = 0; i < movieList.length(); i++) {

                JSONObject movie = movieList.getJSONObject(i);
                resultStrs[i] = movie;
            }
            return resultStrs;

        }

        @Override
        protected JSONObject[] doInBackground(String... params) {

            if (params.length == 0 || params[0] == null || params[0].equals("")) {
                return null;
            }

            //if favorite get from local DB
            if (params[0].equalsIgnoreCase(getString(R.string.pref_sort_favorites))){
                Log.e(LOG_TAG, "Call from local db");

                Realm realm = Realm.getInstance(getActivity());
                RealmQuery<Movie> query = realm.where(Movie.class);
                RealmResults<Movie> list = query.findAll();

                if (list.size() > 0) {
                    Log.e(LOG_TAG, list.size() + " LIST SIZE");
                    JSONObject[] resultStrs = new JSONObject[list.size()];

                    Log.e(LOG_TAG, list.size() + "");
                    for (int i = 0; i < list.size(); i++) {
                        Movie m = list.get(i);
                        Log.e(LOG_TAG, m.toString());

                        try {
                            JSONObject j = new JSONObject();
                            j.put("id", m.getTheMovieDbId());
                            j.put(PLOT, m.getPlot());
                            j.put(USER_RATING, m.getRating());
                            j.put(TITLE, m.getTitle());
                            j.put(RELEASE_DATE, m.getReleaseDate());
                            j.put(POSTER_PATH, m.getPosterPath());
                            resultStrs[i] = j;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return resultStrs;
                }
                return null;

            //else call movie api
            } else {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                String movieResponseJsonStr = null;

                try {
                    final String QUERY_PARAM = "api_key";

                    Uri builtUri = Uri.parse(Utility.BASE_URL).buildUpon().appendPath(params[0])
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
                    movieResponseJsonStr = buffer.toString();
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
                    return parseMovieJson(movieResponseJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }


            }
            return null;

        }

        @Override
        protected void onPostExecute(JSONObject[] result) {
            if (result != null) {
                imageList.clear();
                for(JSONObject str : result) {
                    imageList.add(str);
                }
                ia.notifyDataSetChanged();
            }
        }
    }
}
