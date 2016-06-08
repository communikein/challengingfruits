package com.example.eliam.challengingfruits;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by eliam on 03/06/2016.
 */
public class DBAdapter{
    private static volatile DBAdapter instance;

    public static DBAdapter getInstance(Context ctx){
        if (instance == null)
            instance = new DBAdapter(ctx);
        return instance;
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DBdata.db";

    /** BATTERY CHARGING DATA *********************************************************************/
    public static final String TABLE_NAME = "challenges_data";
    public static final String _ID = BaseColumns._ID;
    public static final String COLUMN_CHALLENGED_BY = "challenged_by";
    public static final String COLUMN_DEADLINE = "deadline";
    public static final String COLUMN_POINTS = "points";
    public static final String COLUMN_WHAT = "what";
    /**********************************************************************************************/

    public final String[] COLUMNS_ALL = new String[]{
            _ID,
            COLUMN_CHALLENGED_BY,
            COLUMN_DEADLINE,
            COLUMN_POINTS,
            COLUMN_WHAT
    };

    public static final String TABLE_CHARGING_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CHALLENGED_BY + " TEXT NOT NULL, " +
                    COLUMN_DEADLINE + " TEXT NOT NULL, " +
                    COLUMN_POINTS + " INTEGER, " +
                    COLUMN_WHAT + " TEXT NOT NULL, " +
                    "UNIQUE (" + COLUMN_CHALLENGED_BY + ", " + COLUMN_DEADLINE + ", " +
                        COLUMN_POINTS + ", " + COLUMN_WHAT + "));";
    /*****************************************************************************************************/


    private final DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(final Context ctx){
        DBHelper = new DatabaseHelper(ctx);
        DBHelper.setDBUpgradedListener(new DBListener() {
            @Override
            public void DBupgraded() {
            }
        });
    }

    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        DBHelper.close();
    }

    public boolean deleteDB(Context context){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.close();

        return context.deleteDatabase(DATABASE_NAME);
    }

    public long newChallenge(Challenge data, ArrayList<Challenge> datas) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(COLUMN_CHALLENGED_BY, data.getChallengedBy());
        initialValues.put(COLUMN_DEADLINE, Utils.date.format(data.getDeadline()));
        initialValues.put(COLUMN_POINTS, data.getPoints());
        initialValues.put(COLUMN_WHAT, data.getWhat());

        long insert = db.insertOrThrow(TABLE_NAME, null, initialValues);
        if (insert != -1 && datas != null) datas.add(data);

        return insert;
    }

    public long newChallenge(Challenge data) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(COLUMN_CHALLENGED_BY, data.getChallengedBy());
        initialValues.put(COLUMN_DEADLINE, Utils.date.format(data.getDeadline()));
        initialValues.put(COLUMN_POINTS, data.getPoints());
        initialValues.put(COLUMN_WHAT, data.getWhat());

        return db.insertOrThrow(TABLE_NAME, null, initialValues);
    }

    public ArrayList<Challenge> deleteChallenge(Challenge data){
        String challenged_by = data.getChallengedBy();
        String deadline = Utils.date.format(data.getDeadline());
        String what = data.getWhat();

        int delete = db.delete(TABLE_NAME,
                COLUMN_CHALLENGED_BY + "='" + challenged_by + "' AND " +
                COLUMN_DEADLINE + "='" + deadline + "' AND " +
                COLUMN_POINTS + "=" + data.getPoints() + " AND " +
                COLUMN_WHAT + "=" + what
                , null);

        if (delete > 0){
            return getAllChallenges();
        }
        else return null;
    }

    public ArrayList<Challenge> getAllChallenges(){
        String[] COLUMNS = new String[] {
                _ID};

        ArrayList<Challenge> ris = new ArrayList<>();
        try{
            Cursor mCursor = db.query(
                    TABLE_NAME,
                    COLUMNS,
                    null,
                    null,
                    null,
                    null,
                    null);

            while(mCursor.moveToNext())
                ris.add(getChallenge(mCursor.getInt(0)));
            mCursor.close();
        } catch (SQLiteException e){
            ris = new ArrayList<>();
        }

        return ris;
    }

    private Challenge getChallenge(long id){
        String CONDITION = _ID + "=" + id;

        return getChallengeAux(CONDITION, null, null);
    }

    private Challenge getChallengeAux(String condition, String ORDER_BY, String LIMIT){
        String[] COLUMNS = COLUMNS_ALL;
        Challenge data;

        Cursor mCursor = null;
        try {
            mCursor = db.query(true,
                    TABLE_NAME,
                    COLUMNS,
                    condition ,
                    null,
                    null,
                    null,
                    ORDER_BY,
                    LIMIT);

            if (mCursor == null || mCursor.getCount() == 0) {
                return null;
            }
            mCursor.moveToFirst();

            if (mCursor.getInt(0) == -1) return null;

            String challenged_by = decodeSQL(mCursor.getString(1));
            Date date = Utils.date.parse(decodeSQL(mCursor.getString(2)));
            int points = mCursor.getInt(3);
            String what = decodeSQL(mCursor.getString(4));

            data = new Challenge(challenged_by, date, points, what);
        } catch (Exception e) {
            data = null;
        } finally {
            if (mCursor != null) mCursor.close();
        }

        return data;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DBListener listener;


        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void setDBUpgradedListener(DBListener listener){
            this.listener = listener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CHARGING_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
            listener.DBupgraded();
        }
    }

    public interface DBListener {
        void DBupgraded();
    }

    public static String encodeSQL(String in){
        if (in == null) return "";
        return in.replaceAll("'", "&#39;");
    }

    public static String decodeSQL(String in){
        return in.replaceAll("&#39;", "'");
    }
}