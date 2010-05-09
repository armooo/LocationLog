package net.armooo.locationlog.importer;

public class Point {

	private String name;
	private double latitude;
	private double longitude;

	public Point(String name, double latitude, double longitude){
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}
