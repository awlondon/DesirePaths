package com.pdceng.www.desirepaths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 8/2/2017.
 */

public class UserTable implements Table {
    static final String ID = "_id";
    static final String SOCIAL_MEDIA_ID = "social_media_id";
    static final String PHOTO_URL = "photo_url";
    static final String NAME = "name";
    static final String REGISTERED_TIMESTAMP = "registered_timestamp";
    static final String POSITIVE_RATINGS = "positive_ratings";
    static final String NEGATIVE_RATINGS = "negative_ratings";
    static final String PI_AGREE = "pi_agree";
    static final String PI_DISAGREE = "pi_disagree";
    static final String TABLE_NAME = "user_table";

    @Override
    public List<String> getFields() {
        List<String> result = new ArrayList<>();
        result.add(SOCIAL_MEDIA_ID);
        result.add(PHOTO_URL);
        result.add(NAME);
        result.add(REGISTERED_TIMESTAMP);
        result.add(POSITIVE_RATINGS);
        result.add(NEGATIVE_RATINGS);
        result.add(PI_AGREE);
        result.add(PI_DISAGREE);
        return result;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String nullColumnHack() {
        return SOCIAL_MEDIA_ID;
    }

    @Override
    public String id() {
        return ID;
    }
}
