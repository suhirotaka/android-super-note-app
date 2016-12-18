package jp.gr.java_conf.suhirotaka.android_noteapp_proto;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmallStepsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SmallSteps.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String TIMESTAMP_TYPE = " TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_GOALS_ENTRIES =
        "CREATE TABLE IF NOT EXISTS " + SmallStepsContract.Goals.TABLE_NAME + " (" +
        SmallStepsContract.Goals._ID + " INTEGER PRIMARY KEY," +
        SmallStepsContract.Goals.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
        SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS + TEXT_TYPE + COMMA_SEP +
        SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK + INT_TYPE + COMMA_SEP +
        SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT + TIMESTAMP_TYPE +
        " )";
    private static final String SQL_CREATE_TASKS_ENTRIES =
        "CREATE TABLE IF NOT EXISTS " + SmallStepsContract.Tasks.TABLE_NAME + " (" +
        SmallStepsContract.Tasks._ID + " INTEGER PRIMARY KEY," +
        SmallStepsContract.Tasks.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
        SmallStepsContract.Tasks.COLUMN_NAME_DEADLINE + TEXT_TYPE + COMMA_SEP +
        SmallStepsContract.Tasks.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
        SmallStepsContract.Tasks.COLUMN_NAME_BELONGING_GOAL_ID + INT_TYPE + COMMA_SEP +
        SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL + INT_TYPE + COMMA_SEP +
        SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT + TIMESTAMP_TYPE +
        " )";
    private static final String SQL_DELETE_GOALS_ENTRIES =
        "DROP TABLE IF EXISTS " + SmallStepsContract.Goals.TABLE_NAME;
    private static final String SQL_DELETE_TASKS_ENTRIES =
        "DROP TABLE IF EXISTS " + SmallStepsContract.Tasks.TABLE_NAME;

    public SmallStepsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GOALS_ENTRIES);
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_GOALS_ENTRIES);
        db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

	public static boolean validateGoalsValues(ContentValues values) {
		String title = values.get(SmallStepsContract.Goals.COLUMN_NAME_TITLE).toString();
	    if (title.length() == 0) return false;
	    return true;
	}

	public static boolean validateTasksValues(ContentValues values) {
		String title = values.get(SmallStepsContract.Tasks.COLUMN_NAME_TITLE).toString();
	    if (title.length() == 0) return false;
	    return true;
	}

	public static String makePlaceholders(int len) {
	    if (len < 1) {
	        throw new RuntimeException("No placeholders");
	    } else {
	        StringBuilder sb = new StringBuilder(len * 2 - 1);
	        sb.append("?");
	        for (int i = 1; i < len; i++) {
	            sb.append(",?");
	        }
	        return sb.toString();
	    }
	}
}