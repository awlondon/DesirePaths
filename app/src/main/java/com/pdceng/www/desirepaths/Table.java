package com.pdceng.www.desirepaths;

import java.util.List;

/**
 * Created by alondon on 5/4/2017.
 */

interface Table {
    List<String> getFields();
    String tableName();
    String nullColumnHack();
    String id();
}
