package net.armooo.locationlog;

import net.armooo.locationlog.util.LocationDatabase;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LocationActivity extends Activity implements LocationListener{

	public final static String LOCATION_ID = "location_id";

	private long location_id;
	private EditText name;
	private EditText latitude;
	private EditText longitude;
	private TextView current_latitude;
	private TextView current_longitude;
	private TextView current_source;
	private TextView current_accuracy;
	private LocationDatabase db;
	private BestLocationProxy best_location_proxy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new LocationDatabase(this);
		best_location_proxy = new BestLocationProxy(this);

		if (savedInstanceState != null) {
			location_id = savedInstanceState.getLong(LOCATION_ID);
		}

		Intent intent = getIntent();
		location_id = intent.getLongExtra(LOCATION_ID, -1);

		setContentView(R.layout.location);
		name = (EditText) findViewById(R.id.name);
		latitude = (EditText) findViewById(R.id.latitude);
		longitude = (EditText) findViewById(R.id.longitude);
		current_latitude = (TextView) findViewById(R.id.current_latitude);
		current_longitude = (TextView) findViewById(R.id.current_longitude);
		current_source = (TextView) findViewById(R.id.current_source);
		current_accuracy = (TextView) findViewById(R.id.current_accuracy);
		
		updateLocation(best_location_proxy.getLastKnownLocation());
		
		Button set_location = (Button) findViewById(R.id.set_location);
		set_location.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Location l = best_location_proxy.getLastKnownLocation();
				latitude.setText(Double.toString(l.getLatitude()));
				longitude.setText(Double.toString(l.getLongitude()));
			}
		});
		

		if (location_id != -1) {
			Cursor c = db.getLocation(location_id);
			if (c.getCount() != 1) {
				finish();
				return;
			}
			c.moveToFirst();
			int name_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_NAME);
			int latitude_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LATITUDE);
			int longitude_id = c.getColumnIndex(LocationDatabase.FIELD_LOCATIONS_LONGITUDE);

			name.setText(c.getString(name_id));
			latitude.setText(Double.toString(c.getDouble(latitude_id)));
			longitude.setText(Double.toString(c.getDouble(longitude_id)));
			c.close();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		best_location_proxy.requestLocationUpdates(100000, 0, this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		best_location_proxy.removeUpdates(this);

		String s_name = name.getText().toString();
		if (s_name.equals("")) {
			return;
		}

		Double d_latitude = null;
		String s_latitude = latitude.getText().toString();
		if (!s_latitude.equals("")) {
			d_latitude = Double.parseDouble(s_latitude);
		}

		Double d_longitude = null;
		String s_longitude = longitude.getText().toString();
		if (!s_longitude.equals("")) {
			d_longitude = Double.parseDouble(s_longitude);
		}

		if (location_id != -1) {
			db.updateLocation(location_id, s_name, d_latitude, d_longitude);
		} else {
			location_id = db.createLocation(s_name, d_latitude, d_longitude);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(LOCATION_ID, location_id);
	}

	private void updateLocation(Location l){

		if (l == null){
			current_source.setText(R.string.no_provider);
			current_latitude.setText(R.string.unavailable);
			current_longitude.setText(R.string.unavailable);
			current_accuracy.setText(R.string.unavailable);
			return;
		}
		
		String source;
		if (l.getProvider().equals(LocationManager.GPS_PROVIDER)){
			source = getString(R.string.gps);
		} else if (l.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
			source = getString(R.string.cell);
		} else {
			source = getString(R.string.unknown);
		}
			
		current_source.setText(source);
		current_latitude.setText(Double.toString(l.getLatitude()));
		current_longitude.setText(Double.toString(l.getLongitude()));
		current_accuracy.setText(Float.toString(l.getAccuracy()));
	}

	@Override
	public void onLocationChanged(Location location) {
		updateLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {	
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
