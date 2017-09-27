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

class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private final String mTitle;
    private final String mSnippet;
    private final Integer mId;
    private Bundle mBundle;
    private PublicInput mPublicInput;
    private BitmapDescriptor mIcon;
    private String mBitmapUrlString;
    private String mSentiment;
    private String mUser;
    private Timestamp mTimestamp;

    MyItem(Bundle bundle) {
        mBundle = bundle;
        mPosition = new LatLng(
                Float.valueOf(bundle.getString(PIEntryTable.LATITUDE)),
                Float.valueOf(bundle.getString(PIEntryTable.LONGITUDE)));
        mTitle = bundle.getString(PIEntryTable.TITLE);
        mSnippet = bundle.getString(PIEntryTable.SNIPPET);
        mSentiment = bundle.getString(PIEntryTable.SENTIMENT);
        createSentimentMarker();
        mBitmapUrlString = bundle.getString(PIEntryTable.URL);
        mUser = bundle.getString(PIEntryTable.SOCIAL_MEDIA_ID);
        mTimestamp = Timestamp.valueOf(bundle.getString(PIEntryTable.TIMESTAMP));
        mId = bundle.getInt(PIEntryTable.ID);
    }

    MyItem(PublicInput publicInput) {
        mPublicInput = publicInput;
        mPosition = new LatLng(
                mPublicInput.getLatitude(), mPublicInput.getLatitude());
        mTitle = mPublicInput.getTitle();
        mSnippet = mPublicInput.getSnippet();
        mSentiment = mPublicInput.getSentiment();
        createSentimentMarker();
        mBitmapUrlString = mPublicInput.getUrl();
        mUser = mPublicInput.getSocialMediaId();
        mTimestamp = Timestamp.valueOf(mPublicInput.getTimestamp());
        mId = Integer.valueOf(mPublicInput.getID());
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

    public PublicInput getPublicInput() {
        return mPublicInput;
    }

    private void createSentimentMarker() {
        switch (mSentiment) {
            case Universals.IDEA:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.idea_icon);
                break;
            case Universals.COMMENT:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.comment_icon);
                break;
            case Universals.WARNING:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.attention_icon);
        }
    }

    @Override
    public String toString() {
        return "MyItem{" +
                "mBundle=" + mBundle +
                ", mPublicInput=" + mPublicInput +
                ", mPosition=" + mPosition +
                ", mTitle='" + mTitle + '\'' +
                ", mSnippet='" + mSnippet + '\'' +
                ", mIcon=" + mIcon +
                ", mBitmapUrlString='" + mBitmapUrlString + '\'' +
                ", mSentiment='" + mSentiment + '\'' +
                ", mUser='" + mUser + '\'' +
                ", mTimestamp=" + mTimestamp +
                ", mId=" + mId +
                '}';
    }
}
