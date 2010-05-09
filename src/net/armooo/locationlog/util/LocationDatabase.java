package net.armooo.locationlog.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDatabase extends SQLiteOpenHelper {

	public final static String TAG = LocationDatabase.class.toString();

	public final static String DB_NAME = "locations";
	public final static int DB_VERSION = 1;

	public final static String TABLE_LOCATIONS = "locations";

	public final static String FIELD_LOCATIONS_ID = "_id";
	public final static String FIELD_LOCATIONS_NAME = "name";
	public final static String FIELD_LOCATIONS_LATITUDE = "latitude";
	public final static String FIELD_LOCATIONS_LONGITUDE = "longitude";

	public final static String[] PROJECTION_LOCATIONS = { FIELD_LOCATIONS_ID,
			FIELD_LOCATIONS_NAME, FIELD_LOCATIONS_LATITUDE,
			FIELD_LOCATIONS_LONGITUDE };

	public LocationDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		getWritableDatabase(); // make upgrades work
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_LOCATIONS + " ( " + 
				FIELD_LOCATIONS_ID + " INTEGER PRIMARY KEY NOT NULL, " + 
				FIELD_LOCATIONS_NAME + " Text, " + 
				FIELD_LOCATIONS_LATITUDE + " REAL, "
				+ FIELD_LOCATIONS_LONGITUDE + " REAL )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

	public long createLocation(String name, Double latitude, Double longitude) {
		Log.d(TAG, "Inserting location " + name);
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_LOCATIONS_NAME, name);
		values.put(FIELD_LOCATIONS_LATITUDE, latitude);
		values.put(FIELD_LOCATIONS_LONGITUDE, longitude);

		long id = db.insert(TABLE_LOCATIONS, null, values);
		Log.d(TAG, Long.toString(id));
		db.close();
		return id;
	}

	public void updateLocation(Long location_id, String name, Double latitude,
			Double longitude) {
		Log.d(TAG, "Updating location");
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_LOCATIONS_NAME, name);
		values.put(FIELD_LOCATIONS_LATITUDE, latitude);
		values.put(FIELD_LOCATIONS_LONGITUDE, longitude);

		db.update(TABLE_LOCATIONS, values, "_id = ?",
				new String[] { location_id.toString() });
		db.close();
	}

	public Cursor getAllLocations() {
		Log.d(TAG, "Selecting all locations");
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_LOCATIONS, PROJECTION_LOCATIONS, null, null,
				null, null, FIELD_LOCATIONS_NAME);
		Log.d(TAG, Integer.toString(c.getCount()));
		db.close();
		return c;
	}

	public Cursor getLocation(long location_id) {
		Log.d(TAG, "Selecting location " + Long.toString(location_id));
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_LOCATIONS, PROJECTION_LOCATIONS, "_id = ?",
				new String[] { Long.toString(location_id) }, null, null, null);
		Log.d(TAG, Integer.toString(c.getCount()));
		db.close();
		return c;
	}
	
	public void deleteLocation(long location_id){
		Log.d(TAG, "Deleting location " + Long.toString(location_id));
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_LOCATIONS, "_id = ?", new String[] {Long.toString(location_id)});
	}
}
