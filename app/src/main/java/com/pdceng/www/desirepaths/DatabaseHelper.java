package com.pdceng.www.desirepaths;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by alondon on 8/2/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    static final int NO_RATING_GIVEN = 0;
    static final int POS_RATING_GIVEN = 1;
    static final int NEG_RATING_GIVEN = 2;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance==null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    public static final String DATABASE_NAME = "adv_data.db";

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableString(new PIEntryTable()));
        db.execSQL(createTableString(new CommentsTable()));
        db.execSQL(createTableString(new UserTable()));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "Upgrading database from version" + oldVersion + " to " +
                newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + PIEntryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);

        onCreate(db);
    }

    long add(Bundle bundle, Table table) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (String field : table.getFields()) cv.put(field, bundle.getString(field));

        long result = db.insert(table.tableName(), table.nullColumnHack(), cv);

        if (db.isOpen()) db.close();

        return result;
    }

    Bundle getRow(Table table, String col, String...args) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(table.tableName(), null, col + "= ?", args, null, null, null);
        Bundle result = new Bundle();
        if (c != null && c.moveToFirst()) {
            for(String field : table.getFields()){
                result.putString(field, c.getString(c.getColumnIndexOrThrow(field)));
            }
            c.close();
            if (db.isOpen()) db.close();
            return result;
        }
        if (db.isOpen()) db.close();

        return null;
    }

    boolean isUser(String...facebook_id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(UserTable.TABLE_NAME, null, UserTable.FACEBOOK_ID + "= ?", facebook_id,null,null,null);
        boolean result = c.moveToFirst();
        c.close();
        db.close();
        return result;
    }

    Bundle[] getAllInTable(Table table){
        Bundle[] result = new Bundle[count(table)];
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(table.tableName(), null, null, null, null, null, null);
        if(c!=null&&c.moveToFirst()) {
            int i = 0;
            do{
                Bundle bundle = new Bundle();
                bundle.putInt(table.id(), c.getInt(c.getColumnIndexOrThrow(PIEntryTable.ID)));
                for (String field : table.getFields()) {
                    bundle.putString(field, c.getString(c.getColumnIndexOrThrow(field)));
                }
                result[i] = bundle;
                i++;
            } while (c.moveToNext());
        }
        c.close();
        if (db.isOpen()) db.close();

        return result;
    }

    long adjustRating(boolean positive, String...commentId){
        String rating = getRow(new CommentsTable(),CommentsTable.ID,commentId[0]).getString(CommentsTable.RATING);
        String newRating = rating;

        int ratingGiven = checkRatingGiven(commentId[0]);

        switch (ratingGiven){
            case NO_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating)+1);
                else newRating = String.valueOf(Integer.valueOf(rating)-1);
                break;
            case POS_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating)-1);
                else newRating = String.valueOf(Integer.valueOf(rating)-2);
                break;
            case NEG_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating)+2);
                else newRating = String.valueOf(Integer.valueOf(rating)+1);
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CommentsTable.RATING, newRating);

        long result = db.update(CommentsTable.TABLE_NAME,cv,CommentsTable.ID + "= ?",commentId);
        if (db.isOpen()) db.close();

        updateRatingsGivenByUser(positive,commentId[0]);

        return result;
    }

    int checkRatingGiven(String commentId){
        Bundle bundle = getRow(new UserTable(),UserTable.FACEBOOK_ID,Universals.FACEBOOK_ID);

        String prs = bundle.getString(UserTable.POSITIVE_RATINGS);
        String nrs = bundle.getString(UserTable.NEGATIVE_RATINGS);
        int result = NO_RATING_GIVEN;
        if (prs!=null&& !Objects.equals(prs, "")){
            String[] prsArray = prs.split(";");
            for (String aPrsArray : prsArray) {
                if (Objects.equals(aPrsArray, commentId)) {
                    result = POS_RATING_GIVEN;
                    break;
                }
            }
        }
        if (result==NO_RATING_GIVEN && nrs!=null&&!Objects.equals(nrs, "")){
            String[] nrsArray = nrs.split(";");
            for (String aNrsArray : nrsArray) {
                if (Objects.equals(aNrsArray, commentId)) {
                    result = NEG_RATING_GIVEN;
                    break;
                }
            }
        }
        return result;
    }

    private void updateRatingsGivenByUser(boolean positive, String commentId) {
        Bundle bundle = getRow(new UserTable(),UserTable.FACEBOOK_ID,Universals.FACEBOOK_ID);

        String prs = bundle.getString(UserTable.POSITIVE_RATINGS);
        String nrs = bundle.getString(UserTable.NEGATIVE_RATINGS);
        String commentIdAdj = commentId+";";

        if(prs==null)prs="";
        if(nrs==null)nrs="";

        if (prs.contains(commentIdAdj)) prs = prs.replace(commentIdAdj, "");
        else if (positive) prs = prs.concat(commentIdAdj);

        if (nrs.contains(commentIdAdj)) nrs = nrs.replace(commentIdAdj,"");
        else if (!positive) nrs = nrs.concat(commentIdAdj);

        //Update db
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(UserTable.POSITIVE_RATINGS, prs);
        cv.put(UserTable.NEGATIVE_RATINGS, nrs);

        db.update(UserTable.TABLE_NAME,cv,UserTable.FACEBOOK_ID + "= ?",new String[]{Universals.FACEBOOK_ID});
        if (db.isOpen()) db.close();
        System.out.println(getRow(new UserTable(),UserTable.FACEBOOK_ID, Universals.FACEBOOK_ID).toString());

    }

    List<String> getComments(String...args) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(CommentsTable.TABLE_NAME, null, CommentsTable.PIEntry_ID + "= ?", args,null,null,null);
        if(c!=null&&c.moveToFirst()){
            do {
//                Bundle bundle = new Bundle();
//                bundle.putInt(CommentsTable.ID,c.getInt(c.getColumnIndexOrThrow(CommentsTable.ID)));
//                for (String field : new CommentsTable().getFields()) {
//                    bundle.putString(field, c.getString(c.getColumnIndexOrThrow(field)));
//                }
                result.add(String.valueOf(c.getInt(c.getColumnIndexOrThrow(CommentsTable.ID))));
            } while (c.moveToNext());
        }
        c.close();

        HashMap<String, Integer> idsAndRatings = new HashMap<>();
        List<Integer> ratings = new ArrayList<>();
        for (String id:result){
            c = db.query(CommentsTable.TABLE_NAME, null, CommentsTable.ID + "= ?", new String[]{id},null,null,null);
            if(c!=null&&c.moveToFirst()){
                int rating = Integer.valueOf(c.getString(c.getColumnIndexOrThrow(CommentsTable.RATING)));
                idsAndRatings.put(id,rating);
                ratings.add(rating);
            }
        }

        Collections.sort(ratings, Collections.<Integer>reverseOrder());

        result = new ArrayList<>();
        for (Integer rating:ratings){
            for (String id:idsAndRatings.keySet()) {
                if(Objects.equals(idsAndRatings.get(id), rating)) {
                    result.add(id);
                    idsAndRatings.remove(id);
                    break;
                }
            }
        }

        if (db.isOpen()) db.close();
        return result;
    }

    private int count(Table table) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(table.tableName(), null, null, null, null, null, null, null);
        int count = 0;
        if (c != null && c.moveToFirst()) do count++; while (c.moveToNext());
        c.close();
        if (db.isOpen()) db.close();

        return count;
    }

    private String createTableString(Table table){
        String string = "CREATE TABLE ";
        string += table.tableName();
        string += " ( ";
        string += table.id();
        string += " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, ";
        List<String> fields = table.getFields();

        for (String field : fields)
            if (fields.indexOf(field) != fields.size() - 1) string += field + " TEXT, ";
            else string += field + " TEXT );";
        return string;
    }
}
