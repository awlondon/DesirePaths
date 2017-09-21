package com.pdceng.www.desirepaths;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
        db.execSQL(createTableString(new ProjectTable()));
        db.execSQL(createTableString(new AnonymousTable()));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "Upgrading database from version" + oldVersion + " to " +
                newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + PIEntryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AnonymousTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProjectTable.TABLE_NAME);

        onCreate(db);
    }

    synchronized long insert(Bundle bundle, Table table) {
        System.out.println("Starting insert!");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (String field : table.getFields()) {
            cv.put(field, bundle.getString(field));
        }

        long result = db.insert(table.tableName(), table.nullColumnHack(), cv);

        if (db.isOpen()) db.close();

        //start PHP
        String sql = insertSQLString(table.tableName(), table.nullColumnHack(), cv);
        postPHP(sql);

        return result;
    }

    synchronized void setAnonymousUser() {
        SQLiteDatabase db = getWritableDatabase();
        Table anonymousTable = new AnonymousTable();
        String thisAnonymousUser;
        Cursor c = db.query(anonymousTable.tableName(), null, null, null, null, null, null);
        //Checking if already logged in as anonymous
        if (c != null && c.moveToFirst()) {
            thisAnonymousUser = c.getString(c.getColumnIndexOrThrow(AnonymousTable.ANONYMOUS_USER));
        } else {
            //Finds next anonymous user to add and adds user
            Table userTable = new UserTable();
            c = db.query(userTable.tableName(), null, null, null, null, null, null);
            String anonymousString = "Anonymous";
            List<String> anonymousList = new ArrayList<>();
            if (c != null && c.moveToFirst()) {
                do {
                    String username = c.getString(c.getColumnIndexOrThrow(UserTable.SOCIAL_MEDIA_ID));
                    if (username.contains(anonymousString)) anonymousList.add(username);
                } while (c.moveToNext());
            }
            int anonNum = 1;
            while (anonymousList.contains(anonymousString + String.valueOf(anonNum))) anonNum++;
            String addAnonymousString = anonymousString + String.valueOf(anonNum);
            ContentValues cv = new ContentValues();
            cv.put(AnonymousTable.ANONYMOUS_USER, addAnonymousString);
            db.insert(anonymousTable.tableName(), anonymousTable.nullColumnHack(), cv);
            Bundle bundle = new Bundle();
            for (String key :
                    cv.keySet()) {
                bundle.putString(UserTable.SOCIAL_MEDIA_ID, (String) cv.get(key));
                bundle.putString(UserTable.NAME, "Anonymous");
            }
            insert(bundle, userTable);
            thisAnonymousUser = addAnonymousString;
        }
        Universals.SOCIAL_MEDIA_ID = thisAnonymousUser;
        Universals.USER_NAME = "Anonymous User";
    }

    synchronized Project getProjectObject(String project_name) {
        Bundle bundle = getRow(new ProjectTable(), ProjectTable.NAME, project_name);
        Project result = new Project();
        result.setId(bundle.getInt(ProjectTable.ID));
        result.setName(bundle.getString(ProjectTable.NAME));
        result.setDescription(bundle.getString(ProjectTable.DESCRIPTION));
        result.setLatLng(new LatLng(
                Double.valueOf(bundle.getString(ProjectTable.LATITUDE)),
                Double.valueOf(bundle.getString(ProjectTable.LONGITUDE))));
        result.setLocation(bundle.getString(ProjectTable.LOCATION));
        result.setWebsite(bundle.getString(ProjectTable.WEBSITE));
        result.setZoom(Integer.valueOf(bundle.getString(ProjectTable.ZOOM)));
        List<String> questionsList = new ArrayList<>();
        String questions = bundle.getString(ProjectTable.QUESTIONS);
        assert questions != null;
        String[] questionsArray = questions.split("/");
        Collections.addAll(questionsList, questionsArray);
        result.setQuestions(questionsList);

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
        } else {
            System.out.println("Could not find row!!!");
        }
        c.close();
        db.close();
        return bundle;
    }

    synchronized void getAllFromSQL(AfterGetAll afterGetAll) {
        final Table[] tables = new Table[]{new PIEntryTable(), new CommentsTable(), new UserTable(), new ProjectTable()};

//        Toast.makeText(mContext, "Synchronizing...", Toast.LENGTH_SHORT).show();

        for (Table table : tables) {
            String sql = querySQLString(table.tableName(), null, null);
            getJSONFromUrl(sql, afterGetAll);
        }
//        Toast.makeText(mContext, "All data is loaded", Toast.LENGTH_SHORT).show();
    }

    synchronized Bundle[] getAllInTable(Table table) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Bundle> bundles = new ArrayList<>();
        Cursor c = db.query(table.tableName(), null, null, null, null, null, null);
        System.out.println("rows found: " + c.getCount());
        if (c.moveToFirst()) {
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

    synchronized PublicInput[] getPublicInputByCurrentProject() {
        Bundle[] bundles = getAllInTable(new PIEntryTable());
        ArrayList<PublicInput> resultArrayList = new ArrayList<>();

        List<Bundle> bundleList = new ArrayList<>();
        List<Bundle> removeList = new ArrayList<>();
        Collections.addAll(bundleList, bundles);
        for (Bundle bundle :
                bundleList) {
            if (Integer.valueOf(bundle.getString(PIEntryTable.PROJECT_ID)) != Universals.PROJECT.getId()) {
                removeList.add(bundle);
            }
        }
        bundleList.removeAll(removeList);
        bundles = bundleList.toArray(new Bundle[bundleList.size()]);

        for (Bundle bundle : bundles) {
            PublicInput publicInput = new PublicInput(mContext);
            publicInput.setID(String.valueOf(bundle.getInt(PIEntryTable.ID)));
            publicInput.setLatitude(Double.valueOf(bundle.getString(PIEntryTable.LATITUDE)));
            publicInput.setLongitude(Double.valueOf(bundle.getString(PIEntryTable.LONGITUDE)));
            publicInput.setSentiment(bundle.getString(PIEntryTable.SENTIMENT));
            publicInput.setSnippet(bundle.getString(PIEntryTable.SNIPPET));
            publicInput.setTimestamp(bundle.getString(PIEntryTable.TIMESTAMP));
            publicInput.setTitle(bundle.getString(PIEntryTable.TITLE));
            publicInput.setUrl(bundle.getString(PIEntryTable.URL));
            publicInput.setSocialMediaId(bundle.getString(PIEntryTable.SOCIAL_MEDIA_ID));
            publicInput.setProject_id(Integer.valueOf(bundle.getString(PIEntryTable.PROJECT_ID)));
            resultArrayList.add(publicInput);
        }

        String[] userPIRatings = getUserPIRatings();
        ArrayList<PublicInput> removeList2 = new ArrayList<>();
        if (userPIRatings != null) {
            for (PublicInput publicInput : resultArrayList) {
                for (String id : userPIRatings) {
                    if (publicInput.getID().equals(id)) {
                        removeList2.add(publicInput);
                        break;
                    }
                }
            }
            resultArrayList.removeAll(removeList2);
        }

        return resultArrayList.toArray(new PublicInput[resultArrayList.size()]);
    }

    synchronized String getRatingForPublicInput(String myId) {
        SQLiteDatabase db = getReadableDatabase();
        System.out.println("id: " + myId);
        int count = 0;
        Cursor c = db.query(UserTable.TABLE_NAME, null, null, null, null, null, null);
        System.out.println("users: " + c.getCount());
        if (c != null && c.moveToFirst()) {
            do {
                String pi_agree = c.getString(c.getColumnIndexOrThrow(UserTable.PI_AGREE));
                String pi_disagree = c.getString(c.getColumnIndexOrThrow(UserTable.PI_DISAGREE));
                if (pi_agree != null && !Objects.equals(pi_agree, "null")) {
                    String[] pi_agreeArray = pi_agree.split(";");
                    for (String id : pi_agreeArray) {
                        if (Objects.equals(myId, id)) {
                            count++;
                            break;
                        }
                    }
                }

                if (pi_disagree != null && !Objects.equals(pi_disagree, "null")) {
                    String[] pi_disagreeArray = pi_disagree.split(";");
                    for (String id :
                            pi_disagreeArray) {
                        if (Objects.equals(myId, id)) {
                            count--;
                            break;
                        }
                    }
                }
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        return String.valueOf(count);
    }

    synchronized String[] getAllProjectNames() {
        Bundle[] bundles = getAllInTable(new ProjectTable());
        System.out.println("projects: " + bundles.length);
        ArrayList<String> resultArrayList = new ArrayList<>();
        for (Bundle bundle : bundles) {
            System.out.println(bundle.toString());
            String string = bundle.getString(ProjectTable.NAME);
            System.out.println("project: " + string);
            resultArrayList.add(string);
        }
        return resultArrayList.toArray(new String[resultArrayList.size()]);
    }

    private synchronized String[] getUserPIRatings() {
        if (Universals.USER_NAME != null) {
            Bundle user = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

            String agree = user.getString(UserTable.PI_AGREE);
            String disagree = user.getString(UserTable.PI_DISAGREE);
            String concat = "";
            if (agree != null && !agree.isEmpty()) concat += agree;
            if (disagree != null && !disagree.isEmpty()) concat += disagree;

            return !concat.isEmpty() ? concat.split(";") : null;
        } else {
            return null;
        }
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

        int ratingGiven = checkCommentRatingGiven(commentId[0]);

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

        updateCommentRatingGivenByUser(positive, commentId[0]);

        return result;
    }

    synchronized int checkCommentRatingGiven(String commentId) {
        if (Universals.USER_NAME != null) {
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
        } else {
            return NO_RATING_GIVEN;
        }
    }

    synchronized int checkPIRatingGiven(String myId) {
        if (Universals.USER_NAME != null) {
            Bundle bundle = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

            String pi_agree = bundle.getString(UserTable.PI_AGREE);
            String pi_disagree = bundle.getString(UserTable.PI_DISAGREE);
            int result = NO_RATING_GIVEN;
            if (pi_agree != null && !Objects.equals(pi_agree, "")) {
                String[] pi_agreeArray = pi_agree.split(";");
                for (String string : pi_agreeArray) {
                    if (Objects.equals(string, myId)) {
                        result = POS_RATING_GIVEN;
                        break;
                    }
                }
            }
            if (result == NO_RATING_GIVEN && pi_disagree != null && !Objects.equals(pi_disagree, "")) {
                String[] pi_disagreeArray = pi_disagree.split(";");
                for (String string : pi_disagreeArray) {
                    if (Objects.equals(string, myId)) {
                        result = NEG_RATING_GIVEN;
                        break;
                    }
                }
            }
            return result;
        } else {
            return NO_RATING_GIVEN;
        }
    }

    private synchronized void updateCommentRatingGivenByUser(boolean positive, String commentId) {
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

    synchronized void updatePIRatingGivenByUser(boolean positive, String myId) {
        System.out.println("getRow from: " + Universals.SOCIAL_MEDIA_ID);
        Bundle bundle = getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);

        String pi_agree = bundle.getString(UserTable.PI_AGREE);
        String pi_disagree = bundle.getString(UserTable.PI_DISAGREE);
        String myIdAdj = myId + ";";

        if (pi_agree == null || Objects.equals(pi_agree, "null")) pi_agree = "";
        if (pi_disagree == null || Objects.equals(pi_disagree, "null")) pi_disagree = "";

        if (pi_agree.contains(myIdAdj)) pi_agree = pi_agree.replace(myIdAdj, "");
        else if (positive) pi_agree = pi_agree.concat(myIdAdj);

        if (pi_disagree.contains(myIdAdj)) pi_disagree = pi_disagree.replace(myIdAdj, "");
        else if (!positive) pi_disagree = pi_disagree.concat(myIdAdj);

        System.out.println("pi_agree: " + pi_agree);
        System.out.println("pi_disagree: " + pi_disagree);

        //Update db
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(UserTable.PI_AGREE, pi_agree);
        cv.put(UserTable.PI_DISAGREE, pi_disagree);

        db.update(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + "= ?", new String[]{Universals.SOCIAL_MEDIA_ID});
        db.close();

        String sql = updateSQLString(UserTable.TABLE_NAME, cv, UserTable.SOCIAL_MEDIA_ID + " = " + Universals.SOCIAL_MEDIA_ID);
        System.out.println("sql: " + sql);
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
        Cursor c = null;
        try {
            c = db.query(UserTable.TABLE_NAME, null, UserTable.SOCIAL_MEDIA_ID + "=?", social_media_id, null, null, null);
        } catch (SQLiteException ignored){

        }
        boolean result = c == null || c.moveToFirst();
        if (c != null) {
            c.close();
        }
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
//            Toast.makeText(mContext, "Updating...", Toast.LENGTH_SHORT).show();
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
            } else if (JSONResult.contains(CommentsTable.COMMENT)) {
                table = new CommentsTable();
            } else {
                table = new ProjectTable();
            }
            System.out.println("table: " + table.tableName());

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
    }
}
