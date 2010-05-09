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

public class KMLImporter implements Importer {

	private static String TAG = "KMLImporter";
	private Uri data;
	
	public KMLImporter(Uri data){
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
		
		PlacemarkHandler placemark_handler = new PlacemarkHandler();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		XMLReader reader = sp.getXMLReader();
		reader.setContentHandler(placemark_handler);
		reader.parse(data.toString());
		return placemark_handler.getPoints();
	}
	
	private class PlacemarkHandler extends DefaultHandler{
		static final String KML_NS = "http://www.opengis.net/kml/2.2";
		
		private List<Point> points;
		
		private StringBuilder name;
		private StringBuilder coordinates;
		
		private boolean in_name = false;
		private boolean in_corrdinates = false;

		public PlacemarkHandler(){
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
			} else if (in_corrdinates){
				for (int i = 0; i < length; i++) {
					this.coordinates.append(ch[start + i]);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if(uri.equals(KML_NS) && localName.equals("name")){
				in_name = false;
			} else if(uri.equals(KML_NS) && localName.equals("coordinates")){
				in_corrdinates = false;
			} else if(uri.equals(KML_NS) && localName.equals("Placemark")){
				String point_name = this.name.toString();
				String[] coordinates = this.coordinates.toString().split(",");
				double longitude = Double.parseDouble(coordinates[0]);
				double latitude = Double.parseDouble(coordinates[1]);
				
				Log.d(TAG, "Add point " + point_name);
				points.add(new Point(point_name, latitude, longitude));	
			}
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if(uri.equals(KML_NS) && localName.equals("name")){
				in_name = true;
			} else if(uri.equals(KML_NS) && localName.equals("coordinates")){
				in_corrdinates = true;
			} else if(uri.equals(KML_NS) && localName.equals("Placemark")){
				this.name = new StringBuilder();
				this.coordinates = new StringBuilder();
			}
		}
	}
}
