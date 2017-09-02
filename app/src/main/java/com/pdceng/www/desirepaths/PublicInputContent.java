package com.pdceng.www.desirepaths;

import android.content.Context;

import com.pdceng.www.desirepaths.PublicInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicInputContent {

    static List<PublicInput> ITEMS = new ArrayList<>();

    static final Map<String, PublicInput> ITEM_MAP = new HashMap<>();

    static final int COUNT = 25;

    Context context;

    PublicInputContent(Context context) {
        this.context = context;
        DatabaseHelper dh = new DatabaseHelper(context);
        Collections.addAll(ITEMS, dh.getAllPublicInput());
        for (PublicInput publicInput:dh.getAllPublicInput()){
            ITEM_MAP.put(publicInput.getID(), publicInput);
        }
    }

}
