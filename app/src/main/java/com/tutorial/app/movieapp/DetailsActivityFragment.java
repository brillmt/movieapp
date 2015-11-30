package com.tutorial.app.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.tutorial.app.movieapp.model.Movie;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {
    private final String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    private JSONObject js = null;
    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        final String TITLE = "title";
        final String POSTER_PATH = "poster_path";
        final String PLOT = "overview";
        final String USER_RATING = "vote_average";
        final String RELEASE_DATE = "release_date";
//        final JSONObject js = null;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String s = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (s != null && !s.equals("")){

                try {
                    js = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (js != null) {
                try {

                    Realm realm = Realm.getInstance(getActivity());
                    Movie m = realm.where(Movie.class).equalTo("theMovieDbId", js.getLong("id")).findFirst();
                    CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.check_box_favorite);
                    if (m != null) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }

                    ((TextView) rootView.findViewById(R.id.details_title)).setText("Title: " + js.getString(TITLE));
                    ((TextView) rootView.findViewById(R.id.details_plot)).setText("Plot: " + js.getString(PLOT));
                    ((TextView) rootView.findViewById(R.id.details_rating)).setText("Rating: " + js.getString(USER_RATING));
                    ((TextView) rootView.findViewById(R.id.details_release_date)).setText("Release Date: " + js.getString(RELEASE_DATE));

                    ImageView imageView = ((ImageView) rootView.findViewById(R.id.details_thumb));
                    String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w500";
                    String address = js.getString(POSTER_PATH);

                    final Uri builtUri = Uri.parse(BASE_IMAGE_URL+address).buildUpon()
                            .build();

                    Picasso.with(getActivity())
                            .load(builtUri)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //Log.e(LOG_TAG, "Good " + builtUri.toString());
                                }

                                @Override
                                public void onError() {
                                    Log.e(LOG_TAG, "Unable to get " + builtUri.toString());
                                }
                            });


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }

        Button tb = (Button) rootView.findViewById(R.id.button_watch_trailer);
        tb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TrailersActivity.class).putExtra(Intent.EXTRA_TEXT, js.toString());
                startActivity(intent);

            }
        });

        Button rb = (Button) rootView.findViewById(R.id.button_reviews);
        rb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReviewsActivity.class).putExtra(Intent.EXTRA_TEXT, js.toString());
                startActivity(intent);

            }
        });

        CheckBox fav = (CheckBox) rootView.findViewById(R.id.check_box_favorite);
        fav.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                RealmConfiguration config0 = new RealmConfiguration.Builder(getActivity())
                        .name("default.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm realm = Realm.getInstance(config0);

                Long movieDbId = null;
                try {
                    movieDbId = js.getLong("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (movieDbId == null)
                    return;

                Movie m = realm.where(Movie.class).equalTo("theMovieDbId", movieDbId).findFirst();

                boolean isChecked = ((CheckBox)v).isChecked();
                if (isChecked){
                    //check if in database. if not save it.
                    Toast.makeText(getActivity(), "Saved to favorites", Toast.LENGTH_SHORT).show();
                    if (m == null) {
                        try {
                            realm.beginTransaction();
                            Movie m2 = realm.createObject(Movie.class);
                            m2.setTheMovieDbId(js.getLong("id"));
                            m2.setPlot(js.getString(PLOT));
                            m2.setRating(js.getString(USER_RATING));
                            m2.setTitle(js.getString(TITLE));
                            m2.setReleaseDate(js.getString(RELEASE_DATE));
                            m2.setPosterPath(js.getString(POSTER_PATH));
                            realm.commitTransaction();
                            realm.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(LOG_TAG, m.toString());
                    }
                } else {
                    //check if in database. if it is; delete it.
                    if (m != null) {
                        realm.beginTransaction();
                        m.removeFromRealm();
                        realm.commitTransaction();
                        realm.close();
                    }
                    Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();

                }


            }
        });

        return rootView;



    }
}
