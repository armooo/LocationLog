package net.armooo.locationlog;

import java.util.List;
import java.util.Formatter;
import java.util.Locale;

import net.armooo.locationlog.util.LocationDatabase;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class LocationListActivity extends ListActivity {

	private Cursor locations;
	private LocationDatabase db;

	private final static String RADAR_ACTION = "com.google.android.radar.SHOW_RADAR";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		db = new LocationDatabase(this);

		setContentView(R.layout.list);
		registerForContextMenu(this.getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = (Cursor) parent.getAdapter().getItem(position);

				int name_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_NAME);
				int latitude_id = c
						.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LATITUDE);
				int longitude_id = c
						.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LONGITUDE);
				String name = c.getString(name_id);
				float latitude = c.getFloat(latitude_id);
				float longitude = c.getFloat(longitude_id);

				startBest(latitude, longitude, name);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem add = menu.add(R.string.location_add);
		add.setIcon(android.R.drawable.ic_menu_add);
		add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent();
				i.setClass(LocationListActivity.this, LocationActivity.class);
				startActivity(i);
				return true;
			}
		});

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (locations != null){
			locations.close();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = (Cursor) getListView().getItemAtPosition(info.position);

		int id_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_ID);
		int name_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_NAME);
		int latitude_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LATITUDE);
		int longitude_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LONGITUDE);

		final long id = c.getLong(id_id);
		final String name = c.getString(name_id);
		final float latitude = c.getFloat(latitude_id);
		final float longitude = c.getFloat(longitude_id);

		menu.setHeaderTitle(name);

		MenuItem radar = menu.add(R.string.location_radar);
		radar.setEnabled(isRadarAvailable());
		radar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				startRadar(latitude, longitude);
				return true;
			}
		});

		MenuItem map = menu.add(R.string.location_map);
		map.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				startMap(latitude, longitude, name);
				return true;
			}
		});

		MenuItem edit = menu.add(R.string.location_edit);
		edit.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent();
				i.setClass(LocationListActivity.this, LocationActivity.class);
				i.putExtra(LocationActivity.LOCATION_ID, id);
				startActivity(i);
				return true;
			}
		});

		MenuItem delete = menu.add(R.string.location_delete);
		delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				db.deleteLocation(id);
				updateList();
				return true;
			}
		});
	}

	private void updateList() {

		if (locations != null) {
			locations.close();
		}

		locations = db.getAllLocations();
		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, locations,
				new String[] { LocationDatabase.FIELD_LOCATIONS_NAME },
				new int[] { android.R.id.text1 });
		setListAdapter(adapter);
	}

	private void startRadar(float latitude, float longitude){
		Intent i = new Intent(RADAR_ACTION);
		i.putExtra("latitude", latitude);
		i.putExtra("longitude", longitude);
		startActivity(i);
	}

	private void startMap(float latitude, float longitude, String name){
        Formatter f = new Formatter(Locale.US);
        f.format("geo:0,0?q=%1$.5f,%2$.5f(%3$s)", latitude, longitude, name);
		Uri uri = Uri.parse(f.toString());
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
	}

	private void startBest(float latitude, float longitude, String name){
		if (isRadarAvailable()){
			startRadar(latitude, longitude);
		} else {
			startMap(latitude, longitude, name);
		}
	}

	private boolean isRadarAvailable(){
		return isIntentAvailable(RADAR_ACTION);
	}

	private boolean isIntentAvailable(String action) {
	    PackageManager packageManager = getPackageManager();
	    final Intent intent = new Intent(action);
	    List list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
}
