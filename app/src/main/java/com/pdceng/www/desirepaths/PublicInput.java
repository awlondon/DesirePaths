package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.concurrent.ExecutionException;

/**
 * Created by alondon on 7/27/2017.
 */

public class PublicInput {

    private final Context context;
    private String ID;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("snippet")
    @Expose
    private String snippet;
    @SerializedName("sentiment")
    @Expose
    private String sentiment;
    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("socialMediaId")
    @Expose
    private String socialMediaId;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("project_id")
    @Expose
    private int project_id;


    public PublicInput(Context context) {
        this.context = context;
    }

    public String getID() { return ID; }

    public void setID(String ID) { this.ID = ID; }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLocation(){
        return new LatLng(this.getLatitude(),this.getLongitude());
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getSocialMediaId() {
        return socialMediaId;
    }

    public void setSocialMediaId(String socialMediaId) {
        this.socialMediaId = socialMediaId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    //Non-trivial
    public Bitmap getBitmap() throws ExecutionException, InterruptedException {
        DownloadImageTask downloadImageTask = new DownloadImageTask(context);
        downloadImageTask.execute(url);
        //        bitmap = Universals.sampleBitmap(bitmap);
        return downloadImageTask.get();

    }
}
