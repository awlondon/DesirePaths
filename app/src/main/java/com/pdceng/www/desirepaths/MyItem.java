package com.pdceng.www.desirepaths;

import android.os.Bundle;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.sql.Timestamp;

/**
 * Created by alondon on 7/24/2017.
 */

public class MyItem implements ClusterItem {
    private Bundle mBundle;
    private LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private BitmapDescriptor mIcon;
    private String mBitmapUrlString;
    private String mSentiment;
    private String mUser;
    private Timestamp mTimestamp;
    private Integer mId;

    public MyItem(Bundle bundle) {
        mBundle = bundle;
        mPosition = new LatLng(
                Float.valueOf(bundle.getString(PIEntryTable.LATITUDE)),
                Float.valueOf(bundle.getString(PIEntryTable.LONGITUDE)));

        mTitle = bundle.getString(PIEntryTable.TITLE);

        mSnippet = bundle.getString(PIEntryTable.SNIPPET);

        mSentiment = bundle.getString(PIEntryTable.SENTIMENT);

        switch (mSentiment) {
            case "positive":
                mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                break;
            case "neutral":
                mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                break;
            case "negative":
                mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

        mBitmapUrlString = bundle.getString(PIEntryTable.URL);

        mUser = bundle.getString(PIEntryTable.USER);

        mTimestamp = Timestamp.valueOf(bundle.getString(PIEntryTable.TIMESTAMP));

        mId = bundle.getInt(PIEntryTable.ID);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    public void setIcon(BitmapDescriptor mIcon) {
        this.mIcon = mIcon;
    }

    public String getBitmapUrlString() {
        return mBitmapUrlString;
    }

    public void setBitmapUrlString(String mBitmapUrlString) {
        this.mBitmapUrlString = mBitmapUrlString;
    }

    public String getSentiment() {
        return mSentiment;
    }

    public void setSentiment(String mSentiment) {
        this.mSentiment = mSentiment;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String mUser) {
        this.mUser = mUser;
    }

    public Timestamp getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Timestamp mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public Integer getId() {
        return mId;
    }

    public Bundle getBundle() {
        return mBundle;
    }
}
