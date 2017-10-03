package com.pdceng.www.desirepaths;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by alondon on 7/27/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getResources().getString(R.string.parse_app_id),getResources().getString(R.string.parse_app_id));
    }
}
