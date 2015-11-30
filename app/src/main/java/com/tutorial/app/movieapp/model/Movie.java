package com.tutorial.app.movieapp.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by mbrillantes on 11/18/15.
 */
public class Movie extends RealmObject {

//    @PrimaryKey
//    private Long id;
    private Long theMovieDbId;
    private String title;
    private String plot;

    private String rating;

    private String releaseDate;

    @Required
    private String posterPath;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

    public Long getTheMovieDbId() {
        return theMovieDbId;
    }

    public void setTheMovieDbId(Long theMovieDbId) {
        this.theMovieDbId = theMovieDbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
