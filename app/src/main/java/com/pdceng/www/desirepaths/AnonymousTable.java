package com.pdceng.www.desirepaths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 9/18/2017.
 */

public class AnonymousTable implements Table {

    static final String ID = "_id";
    static final String ANONYMOUS_USER = "anonymous_user";
    static final String TABLE_NAME = "anonymous_table";

    @Override
    public List<String> getFields() {
        List<String> result = new ArrayList<>();
        result.add(ANONYMOUS_USER);
        return result;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String nullColumnHack() {
        return ANONYMOUS_USER;
    }

    @Override
    public String id() {
        return ID;
    }
}
