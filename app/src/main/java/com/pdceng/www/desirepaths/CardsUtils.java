package com.pdceng.www.desirepaths;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alondon on 7/27/2017.
 */

class CardsUtils {

    private static final String TAG = "Utils";

    public static List<PublicInput> loadPublicInputs(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context));
            List<PublicInput> publicInputList = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                PublicInput publicInput = gson.fromJson(array.getString(i), PublicInput.class);
                publicInputList.add(publicInput);
            }
            return publicInputList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context) {
        String json;
        InputStream is;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG, "path " + "public_input.json");
            is = manager.open("public_input.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
