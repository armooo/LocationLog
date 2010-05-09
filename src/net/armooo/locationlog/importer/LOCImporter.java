package net.armooo.locationlog.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.net.Uri;
import android.util.Log;

public class LOCImporter implements Importer {

	private static String TAG = "LOCImporter";
	private Uri data;
	
	public LOCImporter(Uri data){
		this.data = data;
	}

	@Override
	public List<Point> getPoints() {

		try {
			return parseXML();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	
	private List<Point> parseXML() throws SAXException, IOException, ParserConfigurationException{
		
		WaypointHandler waypoint_handler = new WaypointHandler();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		XMLReader reader = sp.getXMLReader();
		reader.setContentHandler(waypoint_handler);
		reader.parse(data.toString());
		return waypoint_handler.getPoints();
	}
	
	private class WaypointHandler extends DefaultHandler{		
		private List<Point> points;
		
		private StringBuilder name;
		private String lat;
		private String lon;
		
		private boolean in_name = false;

		public WaypointHandler(){
			points = new ArrayList<Point>();
	
		}
		
		public List<Point> getPoints(){
			return points;
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (in_name){
				for (int i = 0; i < length; i++) {
					this.name.append(ch[start + i]);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if(localName.equals("name")){
				in_name = false;
			} else if(localName.equals("waypoint")){
				String point_name = this.name.toString();
				double longitude = Double.parseDouble(lon);
				double latitude = Double.parseDouble(lat);
				
				Log.d(TAG, "Add point " + point_name);
				points.add(new Point(point_name, latitude, longitude));	
			}
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if(localName.equals("name")){
				in_name = true;
			} else if (localName.equals("coord")){
				lat = attributes.getValue("", "lat");
				lon = attributes.getValue("", "lon");
			}
			else if(localName.equals("waypoint")){
				this.name = new StringBuilder();
			}
		}
	}
}
