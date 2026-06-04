package com.example.moviedates.network.model;

public class MovieDTO {
    private int id;
    private String title;
    private String overview;
    private String posterPath;
    private double voteAverage;
    private String releaseDate;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public double getVoteAverage() { return voteAverage; }
    public String getReleaseDate() { return releaseDate; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
}