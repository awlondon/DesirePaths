package com.pdceng.www.desirepaths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 8/2/2017.
 */

public class PIEntryTable implements Table {
    static final String ID = "_id";
    static final String URL = "url";
    static final String TITLE = "title";
    static final String SNIPPET = "snippet";
    static final String SENTIMENT = "sentiment";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String USER = "user";
    static final String TIMESTAMP = "timestamp";
    static final String TABLE_NAME = "PIEntry_table";

    @Override
    public List<String> getFields() {
        List<String> result = new ArrayList<>();
        result.add(URL);
        result.add(TITLE);
        result.add(SNIPPET);
        result.add(SENTIMENT);
        result.add(LATITUDE);
        result.add(LONGITUDE);
        result.add(USER);
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
