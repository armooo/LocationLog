package net.armooo.locationlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContextWrapper;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class BestLocationProxy {

	private static final String TAG = "BestLocationProxy";

	private LocationManager location_mgr;
	private List<ListenerRequest> requests;
	private boolean listening = false;
	private List<Listener> real_listeners;
	private Location last_known_location;
	private String best_provider;
	private List<String> provider_priority;
	private Map<String, ProviderStatus> provider_status;

	public BestLocationProxy(ContextWrapper context_wrapper) {
		location_mgr = (LocationManager) context_wrapper
				.getSystemService(ContextWrapper.LOCATION_SERVICE);
		requests = new ArrayList<ListenerRequest>();
		provider_status = new HashMap<String, ProviderStatus>();
		real_listeners = new ArrayList<Listener>();

		provider_priority = new ArrayList<String>();
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider_priority.addAll(location_mgr.getProviders(criteria, false));
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		for (String provider : location_mgr.getProviders(criteria, false)) {
			if (!provider_priority.contains(provider)) {
				provider_priority.add(provider);
			}
		}

		for (String provider : location_mgr.getProviders(false)) {
			provider_status.put(provider, new ProviderStatus());
		}

		change_best_provider();
	}

	public Location getLastKnownLocation() {
		if (best_provider == null){
			return null;
		}
		if (last_known_location == null) {
			return location_mgr.getLastKnownLocation(best_provider);
		} else {
			return last_known_location;
		}
	}

	public void requestLocationUpdates(long minTime, float minDistance,
			LocationListener listener) {
		ListenerRequest request = new ListenerRequest(minTime, minDistance,
				listener);
		requests.add(request);
		updated_requests();
	}

	public void removeUpdates(LocationListener listener) {
		Iterator<ListenerRequest> itr = requests.iterator();
		while (itr.hasNext()) {
			if (itr.next().getListener() == listener) {
				itr.remove();
				updated_requests();
				break;
			}
		}
	}

	private void updated_requests() {
		if (requests.isEmpty()) {
			for (LocationListener listener : real_listeners) {
				location_mgr.removeUpdates(listener);
			}
			listening = false;
			return;
		}

		long min_time = 0;
		float min_distance = 0;

		for (ListenerRequest request : requests) {
			if (request.getMinTime() < min_time) {
				min_time = request.getMinTime();
			}
			if (request.getMinDistance() < min_distance) {
				min_distance = request.getMinDistance();
			}
		}

		if (listening) {
			for (LocationListener listener : real_listeners) {
				location_mgr.removeUpdates(listener);
			}
		}
		for (String provider : location_mgr.getProviders(false)) {
			Listener listener = new Listener();
			real_listeners.add(listener);
			location_mgr.requestLocationUpdates(provider, min_time,
					min_distance, listener);
		}
		listening = true;

	}

	private void change_best_provider() {
		Log.d(TAG, "Looking for best provider");

		String new_provider = null;
		
		for (String provider : provider_priority) {
			ProviderStatus pstatus = provider_status.get(provider);
			int enabled = pstatus.getEnabled();
			int status = pstatus.getStatus();
			if (enabled == ProviderStatus.UNKNOWN) {
				if (location_mgr.isProviderEnabled(provider)) {
					enabled = ProviderStatus.ENABLED;
				}
				pstatus.setEnabled(enabled);
			}
			if (status == ProviderStatus.UNKNOWN) {
				Log.d(TAG, "Provider status is unknown " + provider);
			}
			if (enabled == ProviderStatus.DISABLED) {
				Log.d(TAG, "Provider is disabled " + provider);
				continue;
			}
			if (status != ProviderStatus.AVAILABLE) {
				Log.d(TAG, "Provider is not available " + provider);
				continue;
			}

			Log.d(TAG, "Using provider " + provider);
			new_provider = provider;
			break;
		}

		if (new_provider == null){
			Criteria criteria = new Criteria();
			new_provider = location_mgr.getBestProvider(criteria, true);
			if (new_provider == null){
				Log.d(TAG, "No providers");
				return;
			}
			Log.d(TAG, "Faling back to provider " + new_provider);
		}
		
		best_provider = new_provider;
		Location location = location_mgr.getLastKnownLocation(best_provider);
		if (location != null){
			notifyLocationChanged(location);
		}
	}

	private void notifyLocationChanged(Location location){
		Log.d(TAG, "Notifing change from " + location.getProvider());
		last_known_location = location;
		for (ListenerRequest request : requests) {
			request.getListener().onLocationChanged(location);
		}
	}
	
	private class ListenerRequest {
		private long minTime;
		float minDistance;
		LocationListener listener;

		public ListenerRequest(long minTime, float minDistance,
				LocationListener listener) {
			this.minTime = minTime;
			this.minDistance = minDistance;
			this.listener = listener;
		}

		public long getMinTime() {
			return minTime;
		}

		public float getMinDistance() {
			return minDistance;
		}

		public LocationListener getListener() {
			return listener;
		}
	}

	private class ProviderStatus {
		public static final int UNKNOWN = -1;
		public static final int ENABLED = 2;
		public static final int DISABLED = 3;
		public static final int AVAILABLE = LocationProvider.AVAILABLE;
		public static final int OUT_OF_SERVICE = LocationProvider.OUT_OF_SERVICE;
		public static final int TEMPORARILY_UNAVAILABLE = LocationProvider.TEMPORARILY_UNAVAILABLE;

		private int enabled = UNKNOWN;
		private int status = UNKNOWN;

		public int getEnabled() {
			return enabled;
		}

		public void setEnabled(int enabled) {
			this.enabled = enabled;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
	}

	private class Listener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			ProviderStatus status = provider_status.get(location.getProvider());
			if (status.getStatus() == ProviderStatus.UNKNOWN){
				status.setStatus(ProviderStatus.AVAILABLE);
				change_best_provider();
			}
			
			if (!location.getProvider().equals(best_provider)) {
				Log.d(TAG, "Discarded change from " + location.getProvider());
				return;
			}
			
			notifyLocationChanged(location);

		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "Provider disabled " + provider);
			provider_status.get(provider).setEnabled(ProviderStatus.DISABLED);
			change_best_provider();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "Provider enabled " + provider);
			provider_status.get(provider).setEnabled(ProviderStatus.ENABLED);
			change_best_provider();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "Provider status changed " + provider + " "
					+ Integer.toString(status));
			provider_status.get(provider).setStatus(status);
			change_best_provider();
		}
	}
}
