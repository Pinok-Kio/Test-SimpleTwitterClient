package com.example.Twitter_Android.Logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AppDatabase extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ThisAppDatabase.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + FieldEntry.TABLE_NAME + " (" +
			BaseColumns._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_ID + " LONG" + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_NAME + TEXT_TYPE + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_SCREEN_NAME + TEXT_TYPE + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_LOCATION + TEXT_TYPE + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_DESCRIPTION + TEXT_TYPE + COMMA_SEP
			+ FieldEntry.COLUMN_NAME_USER_PROFILE_IMAGE + TEXT_TYPE + ")";

	public static class FieldEntry {
		public static final String COLUMN_NAME_USER_ID = "user_id";
		public static final String COLUMN_NAME_USER_NAME = "user_name";
		public static final String COLUMN_NAME_USER_SCREEN_NAME = "user_screen_name";
		public static final String COLUMN_NAME_USER_PROFILE_IMAGE = "user_profile_image";
		public static final String COLUMN_NAME_USER_DESCRIPTION = "user_description";
		public static final String COLUMN_NAME_USER_LOCATION = "user_location";
		public static final String TABLE_NAME = "persons";
	}


	public AppDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("Database onCreate");
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
