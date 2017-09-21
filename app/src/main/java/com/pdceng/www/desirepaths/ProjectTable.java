package com.pdceng.www.desirepaths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 9/18/2017.
 */

public class ProjectTable implements Table {
    static final String ID = "_id";
    static final String NAME = "name";
    static final String LOCATION = "location";
    static final String DESCRIPTION = "description";
    static final String QUESTIONS = "questions"; //Separated by "/"
    static final String WEBSITE = "website";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String ZOOM = "zoom";
    static final String TABLE_NAME = "project_table";

    @Override
    public List<String> getFields() {
        List<String> result = new ArrayList<>();
        result.add(NAME);
        result.add(LOCATION);
        result.add(DESCRIPTION);
        result.add(QUESTIONS);
        result.add(WEBSITE);
        result.add(LONGITUDE);
        result.add(LATITUDE);
        result.add(ZOOM);
        return result;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String nullColumnHack() {
        return DESCRIPTION;
    }

    @Override
    public String id() {
        return ID;
    }
}
