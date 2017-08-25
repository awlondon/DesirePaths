package com.pdceng.www.desirepaths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 8/2/2017.
 */

public class CommentsTable implements Table {
    static final String ID = "_id";
    static final String PIEntry_ID = "pientry_id";
    static final String COMMENT = "comment";
    static final String RATING = "rating";
    static final String SOCIAL_MEDIA_ID = "social_media_id";
    static final String TIMESTAMP = "timestamp";
    static final String TABLE_NAME = "comments_table";

    @Override
    public List<String> getFields() {
        List<String> result = new ArrayList<>();
        result.add(PIEntry_ID);
        result.add(COMMENT);
        result.add(RATING);
        result.add(SOCIAL_MEDIA_ID);
        result.add(TIMESTAMP);
        return result;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String nullColumnHack() {
        return TIMESTAMP;
    }

    @Override
    public String id() {
        return ID;
    }
}
