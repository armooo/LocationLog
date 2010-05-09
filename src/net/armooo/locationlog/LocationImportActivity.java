package net.armooo.locationlog;

import java.util.List;

import net.armooo.locationlog.importer.Importer;
import net.armooo.locationlog.importer.KMLImporter;
import net.armooo.locationlog.importer.LOCImporter;
import net.armooo.locationlog.importer.Point;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LocationImportActivity extends ListActivity {
	private static String TAG = "LocationImportActivity";

	private static String KML_MIME = "application/vnd.google-earth.kml+xml";
	private static String LOC_MIME = "application/xml-loc";

	private List<Point> points;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		Importer importer = null;

		Log.d(TAG, "MIME " + intent.getType());
		if (intent.getType().equals(KML_MIME)) {
			importer = new KMLImporter(intent.getData());
		} else if (intent.getType().equals(LOC_MIME)) {
			importer = new LOCImporter(intent.getData());
		}

		points = importer.getPoints();
		Log.d(TAG, "Points " + points.toString());
	}
}
