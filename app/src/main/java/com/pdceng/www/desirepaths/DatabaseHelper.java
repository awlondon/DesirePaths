package com.pdceng.www.desirepaths;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.database.sqlite.SQLiteQueryBuilder.appendColumns;

/**
 * Created by alondon on 8/2/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "adv_data.db";
    static final int NO_RATING_GIVEN = 0;
    static final int POS_RATING_GIVEN = 1;
    static final int NEG_RATING_GIVEN = 2;
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper instance;
    Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private synchronized static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
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

    synchronized long insert(Bundle bundle, Table table) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (String field : table.getFields()) cv.put(field, bundle.getString(field));

        long result = db.insert(table.tableName(), table.nullColumnHack(), cv);

        if (db.isOpen()) db.close();

        //start PHP
        String sql = insertSQLString(table.tableName(), table.nullColumnHack(), cv);
        postPHP(sql);

        return result;
    }

    synchronized Bundle getRow(Table table, String col, String... args) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(table.tableName(), null, col + "= ?", args, null, null, null);
        Bundle bundle = new Bundle();
        if (c != null && c.moveToFirst()) {
            bundle.putInt(table.id(), c.getInt(c.getColumnIndexOrThrow(table.id())));
            for (String field : table.getFields()) {
                bundle.putString(field, c.getString(c.getColumnIndexOrThrow(field)));
            }
        }
        c.close();
        db.close();
        return bundle;
    }

    synchronized void getAllFromSQL(AfterGetAll afterGetAll) {
        final Table[] tables = new Table[]{new PIEntryTable(), new CommentsTable(), new UserTable()};

        Toast.makeText(mContext, "Synchronizing...", Toast.LENGTH_SHORT).show();

        for (Table table : tables) {
            String sql = querySQLString(table.tableName(), null, null);
            getJSONFromUrl(sql, afterGetAll);
        }
    }

    synchronized Bundle[] getAllInTable(Table table) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Bundle> bundles = new ArrayList<>();
        Cursor c = db.query(table.tableName(), null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                Bundle bundle = new Bundle();
                bundle.putInt(table.id(), c.getInt(c.getColumnIndexOrThrow(table.id())));
                for (String field : table.getFields()) {
                    bundle.putString(field, c.getString(c.getColumnIndexOrThrow(field)));
                }
                bundles.add(bundle);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return bundles.toArray(new Bundle[bundles.size()]);
    }

    synchronized PublicInput[] getAllPublicInput() {
        Bundle[] bundles = getAllInTable(new PIEntryTable());
        ArrayList<PublicInput> resultArrayList = new ArrayList<>();

        int i = 0;
        for (Bundle bundle : bundles) {
            PublicInput publicInput = new PublicInput();
            publicInput.setID(String.valueOf(bundle.getInt(PIEntryTable.ID)));
            publicInput.setLatitude(Double.valueOf(bundle.getString(PIEntryTable.LATITUDE)));
            publicInput.setLongitude(Double.valueOf(bundle.getString(PIEntryTable.LONGITUDE)));
            publicInput.setSentiment(bundle.getString(PIEntryTable.SENTIMENT));
            publicInput.setSnippet(bundle.getString(PIEntryTable.SNIPPET));
            publicInput.setTimestamp(bundle.getString(PIEntryTable.TIMESTAMP));
            publicInput.setTitle(bundle.getString(PIEntryTable.TITLE));
            publicInput.setUrl(bundle.getString(PIEntryTable.URL));
            publicInput.setUser(bundle.getString(PIEntryTable.USER));
            resultArrayList.add(publicInput);
            i++;
        }

        String[] userPIRatings = getUserPIRatings();
        ArrayList<PublicInput> removeList = new ArrayList<>();
        if (userPIRatings != null) {
            for (PublicInput publicInput : resultArrayList) {
                for (String id : userPIRatings) {
                    if (publicInput.getID().equals(id)) {
                        removeList.add(publicInput);
                        break;
                    }
                }
            }
            resultArrayList.removeAll(removeList);
        }

        return resultArrayList.toArray(new PublicInput[resultArrayList.size()]);
    }

    private synchronized String[] getUserPIRatings() {
        Bundle user = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

        String agree = user.getString(UserTable.PI_AGREE);
        String disagree = user.getString(UserTable.PI_DISAGREE);
        String concat = "";
        if (agree != null && !agree.isEmpty()) concat += agree;
        if (disagree != null && !disagree.isEmpty()) concat += disagree;

        return !concat.isEmpty() ? concat.split(";") : null;
    }

    synchronized void updateUserPIRatings(String piID, boolean agree) {
        Bundle bundle = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

        String updateString;
        if (agree) updateString = bundle.getString(UserTable.PI_AGREE);
        else updateString = bundle.getString(UserTable.PI_DISAGREE);

        if (updateString == null || Objects.equals(updateString, "null")) updateString = "";

        updateString += piID + ";";

        //Update DB
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (agree) cv.put(UserTable.PI_AGREE, updateString);
        else cv.put(UserTable.PI_DISAGREE, updateString);

        Log.d("updated PI string", updateString);

        db.update(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + "= ?", new String[]{Universals.SOCIAL_MEDIA_ID});

        if (db.isOpen()) db.close();

        String sql = updateSQLString(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + "= " + Universals.SOCIAL_MEDIA_ID);

        postPHP(sql);
    }

    synchronized long adjustRating(boolean positive, String... commentId) {
        String rating = getRow(new CommentsTable(), CommentsTable.ID, commentId[0]).getString(CommentsTable.RATING);
        String newRating = rating;

        int ratingGiven = checkRatingGiven(commentId[0]);

        switch (ratingGiven) {
            case NO_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating) + 1);
                else newRating = String.valueOf(Integer.valueOf(rating) - 1);
                break;
            case POS_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating) - 1);
                else newRating = String.valueOf(Integer.valueOf(rating) - 2);
                break;
            case NEG_RATING_GIVEN:
                if (positive) newRating = String.valueOf(Integer.valueOf(rating) + 2);
                else newRating = String.valueOf(Integer.valueOf(rating) + 1);
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CommentsTable.RATING, newRating);

        long result = db.update(CommentsTable.TABLE_NAME, cv, CommentsTable.ID + "= ?", commentId);

        db.close();

        String sql = updateSQLString(CommentsTable.TABLE_NAME, cv, CommentsTable.ID + "= " + commentId[0]);

        postPHP(sql);

        updateRatingsGivenByUser(positive, commentId[0]);

        return result;
    }

    synchronized int checkRatingGiven(String commentId) {
        Bundle bundle = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

        String prs = bundle.getString(UserTable.POSITIVE_RATINGS);
        String nrs = bundle.getString(UserTable.NEGATIVE_RATINGS);
        int result = NO_RATING_GIVEN;
        if (prs != null && !Objects.equals(prs, "")) {
            String[] prsArray = prs.split(";");
            for (String aPrsArray : prsArray) {
                if (Objects.equals(aPrsArray, commentId)) {
                    result = POS_RATING_GIVEN;
                    break;
                }
            }
        }
        if (result == NO_RATING_GIVEN && nrs != null && !Objects.equals(nrs, "")) {
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

    private synchronized void updateRatingsGivenByUser(boolean positive, String commentId) {
        Bundle bundle = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

        String prs = bundle.getString(UserTable.POSITIVE_RATINGS);
        String nrs = bundle.getString(UserTable.NEGATIVE_RATINGS);
        String commentIdAdj = commentId + ";";

        if (prs == null || Objects.equals(prs, "null")) prs = "";
        if (nrs == null || Objects.equals(nrs, "null")) nrs = "";

        if (prs.contains(commentIdAdj)) prs = prs.replace(commentIdAdj, "");
        else if (positive) prs = prs.concat(commentIdAdj);

        if (nrs.contains(commentIdAdj)) nrs = nrs.replace(commentIdAdj, "");
        else if (!positive) nrs = nrs.concat(commentIdAdj);

        //Update db
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(UserTable.POSITIVE_RATINGS, prs);
        cv.put(UserTable.NEGATIVE_RATINGS, nrs);

        db.update(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + "= ?", new String[]{Universals.SOCIAL_MEDIA_ID});
        db.close();

        String sql = updateSQLString(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + " = " + Universals.SOCIAL_MEDIA_ID);
        postPHP(sql);
    }

    synchronized List<String> getComments(String... args) {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(CommentsTable.TABLE_NAME, null, CommentsTable.PIEntry_ID + "= ?", args, null, null, null);
        int i = 0;
        if (c != null && c.moveToFirst()) {
            do {
                result.add(String.valueOf(c.getInt(c.getColumnIndexOrThrow(CommentsTable.ID))));
                i++;
            } while (c.moveToNext());
        }
        c.close();

        HashMap<String, Integer> idsAndRatings = new HashMap<>();
        List<Integer> ratings = new ArrayList<>();
        for (String id : result) {
            c = db.query(CommentsTable.TABLE_NAME, null, CommentsTable.ID + "= ?", new String[]{id}, null, null, null);
            if (c != null && c.moveToFirst()) {
                int rating = Integer.valueOf(c.getString(c.getColumnIndexOrThrow(CommentsTable.RATING)));
                idsAndRatings.put(id, rating);
                ratings.add(rating);
            }
        }

        Collections.sort(ratings, Collections.<Integer>reverseOrder());

        result = new ArrayList<>();
        for (Integer rating : ratings) {
            for (String id : idsAndRatings.keySet()) {
                if (Objects.equals(idsAndRatings.get(id), rating)) {
                    result.add(id);
                    idsAndRatings.remove(id);
                    break;
                }
            }
        }

        if (db.isOpen()) db.close();
        return result;
    }

    private synchronized int count(Table table) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(table.tableName(), null, null, null, null, null, null, null);
        int count = 0;
        if (c != null && c.moveToFirst()) do count++; while (c.moveToNext());
        c.close();
        if (db.isOpen()) db.close();

        return count;
    }

    boolean isUser(String... social_media_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(UserTable.TABLE_NAME, null, UserTable.SOCIAL_MEDIA_ID + "=?", social_media_id, null, null, null);
        boolean result = c != null;
        c.close();
        db.close();
        return result;
    }

    private synchronized String createTableString(Table table) {
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

    private synchronized String insertSQLString(String table, String nullColumnHack,
                                                ContentValues initialValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');

        Object[] bindArgs;
        int size = (initialValues != null && initialValues.size() > 0)
                ? initialValues.size() : 0;
        if (size > 0) {
            bindArgs = new Object[size];
            int i = 0;
            for (String colName : initialValues.keySet()) {
                sql
                        .append((i > 0) ? "," : "")
                        .append(colName);

                bindArgs[i++] = initialValues.get(colName);
            }
            sql
                    .append(')')
                    .append(" VALUES (");
            i = 0;
            for (String colName : initialValues.keySet()) {
                sql
                        .append((i > 0) ? "," : "")
                        .append("\"")
                        .append(initialValues.get(colName))
                        .append("\"");
                i++;
            }
        } else {
            sql
                    .append(nullColumnHack)
                    .append(") VALUES (NULL");
        }
        sql.append(')');

        return sql.toString();
    }

    private synchronized String querySQLString(
            String table, String[] columns, String where) {

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");

        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(table);
        appendClause(query, " WHERE ", where);

        return query.toString();
    }

    private synchronized String updateSQLString(String table, ContentValues values,
                                                String whereClause) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(table);
        sql.append(" SET ");

        // move all bind args to one array
        int setValuesSize = values.size();
        int i = 0;
        for (String colName : values.keySet()) {
            sql.append((i > 0) ? "," : "");
            sql.append(colName);
            sql.append("=");
            sql.append("\'");
            sql.append(values.get(colName));
            sql.append("\'");
            i++;
        }
        if (!TextUtils.isEmpty(whereClause)) {
            appendClause(sql, " WHERE ", whereClause);
        }
        return sql.toString();
    }


    private synchronized void postPHP(final String sql) {
        PostPHP postPHP = new PostPHP();
        postPHP.execute(sql);
    }

    private synchronized void getJSONFromUrl(final String sql, AfterGetAll afterGetAll) {
        GetJSONFromUrl getJSONFromUrl = new GetJSONFromUrl(afterGetAll);
        getJSONFromUrl.execute(sql);
    }

    private class PostPHP extends AsyncTask<String, Void, String> {
        private String finalResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(mContext, "Updating...", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected void onPostExecute(String httpResponseMsg) {
            super.onPostExecute(httpResponseMsg);
            Log.d("POST result", finalResponse);
        }
        @Override
        protected String doInBackground(String... params) {

            HttpParse httpParse = new HttpParse();
            String httpUrl = "http://www.desirepaths.xyz/Post.php";

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("sql", params[0]);

            finalResponse = httpParse.postRequest(hashMap, httpUrl);

            return finalResponse;

        }
    }

    //     JSON parse class started from here.
    private class GetJSONFromUrl extends AsyncTask<String, Void, String> {
        public Context context;
        String JSONResult;
        String httpUrl = "http://www.desirepaths.xyz/Get.php";
        AfterGetAll afterGetAll = null;

        GetJSONFromUrl() {
        }

        GetJSONFromUrl(AfterGetAll afterGetAll) {
            this.afterGetAll = afterGetAll;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                InetAddress i = InetAddress.getByName(httpUrl);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("Ran with error!");
            }
            HttpParse httpParse = new HttpParse();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("sql", params[0]);
            JSONResult = httpParse.postRequest(hashMap, httpUrl);

            return JSONResult;
        }

        @Override
        protected void onPostExecute(String JSONResult) {
            super.onPostExecute(JSONResult);
            Table table;
            if (JSONResult.contains(PIEntryTable.SNIPPET)) {
                table = new PIEntryTable();
            } else if (JSONResult.contains(UserTable.PHOTO_URL)) {
                table = new UserTable();
            } else {
                table = new CommentsTable();
            }

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(JSONResult);
            } catch (JSONException e) {
                e.printStackTrace();
                jsonArray = null;
            }
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table.tableName(), null, null);

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ContentValues cv = new ContentValues();
                        cv.put(table.id(), jsonObject.getInt(table.id()));
                        for (String field : table.getFields()) {
                            cv.put(field, jsonObject.getString(field));
                        }
                        db.insert(table.tableName(), table.nullColumnHack(), cv);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (afterGetAll != null) afterGetAll.afterGetAll();
        }

   /* JSONArray getJSONFromUrl(final String sql) {
            JSONArray result = null;


            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<JSONArray> urlGetResult = executorService.submit(new Callable<JSONArray>() {
                @Override
                public JSONArray call() throws Exception {
                    HttpParse httpParse = new HttpParse();
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("sql", sql);
                    JSONResult = httpParse.postRequest(hashMap, httpUrl);

                    JSONArray jsonArray = null;
                    if (JSONResult != null) {
                        try {
                            jsonArray = new JSONArray(JSONResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            jsonArray = null;
                        }
                    }
                    return jsonArray;

                   *//* OkHttpClient client = new OkHttpClient();

                    RequestBody body = RequestBody.create(MediaType.parse(sql),sql);

                    Request request = new Request.Builder()
                            .url(httpUrl)
                            .post(body)
                            .build();

                    Response response;
                    JSONArray jsonArray = null;
                    try {
                        response = client.newCall(request).execute();
                        JSONResult = response.body().string();
                        if (JSONResult != null) {
                            try {
                                jsonArray = new JSONArray(JSONResult);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                jsonArray = null;
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return jsonArray;*//*
                }
            });
            try {
                result = urlGetResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            executorService.shutdown();
            return result;
        }*/
    }
}
