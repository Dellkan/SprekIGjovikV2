package com.sprekigjovik.tracker;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;

public class Pole implements Comparable<Pole> {
	// Const's
	public static final int DIFF_UNKNOWN = 0;
	public static final int DIFF_GREEN = 1;
	public static final int DIFF_BLUE = 2;
	public static final int DIFF_RED = 3;
	public static final int DIFF_BLACK = 4;
	
	private int mId;
	private String mName;
	private Location mLocation;
	private static List<Pole> mPoles;
	private float mDistance;
	private float mEta;
	private int mDifficulty;
	
	public Pole() {
		this.mId = 0;
		this.mName = "";
		this.mLocation = new Location("RouteTracker");
		this.mDifficulty = 0;
	}
	
	public int getId() {
		return this.mId;
	}
	
	public void setId(int newId) {
		this.mId = newId;
	}
	
	public float getDistance() {
		return this.mDistance;
	}
	
	public Float getETA() {
		return this.mEta;
	}
	
	public void calcDistance(Location loc) {
		this.mDistance = this.mLocation.distanceTo(loc);
		this.mEta = (this.getDistance() / loc.getSpeed());
	}
	
	public String getName() {	
		return this.mName;
	}
	
	public void setName(String newName) {
		this.mName = newName;
	}

	public Location getLocation() {
		return this.mLocation;
	}
	
	public void setLocation(Location newLocation) {
		this.mLocation = newLocation;
	}
	
	public LatLng getLatLng() {
		return new LatLng(this.mLocation.getLatitude(), this.mLocation.getLongitude());
	}
	
	public void setDifficulty(String pDifficulty) {
		if (pDifficulty.equalsIgnoreCase("Green")) {
			this.mDifficulty = Pole.DIFF_GREEN;
		}
		
		else if (pDifficulty.equalsIgnoreCase("Blue")) {
			this.mDifficulty = Pole.DIFF_BLUE;
		}
		
		else if (pDifficulty.equalsIgnoreCase("Red")) {
			this.mDifficulty = Pole.DIFF_RED;
		}
		
		else if (pDifficulty.equalsIgnoreCase("Black")) {
			this.mDifficulty = Pole.DIFF_BLACK;
		}
		
		else {
			this.mDifficulty = Pole.DIFF_UNKNOWN;
		}
	}
	
	public int getDifficulty() {
		return this.mDifficulty;
	}
	
	/**
	 * Reads an xml file from a stream and generates a list of Post objects
	 * @param stream InputStream of an XML file
	 * @return List of Pole objects
	 */
	public static void createFromXML(Context pContext) {
		try {
			FileInputStream stream = pContext.openFileInput("poles.xml");
		
			List<Pole> poles = new ArrayList<Pole>();
			Pole current = null;
			
			XmlPullParserFactory factory;
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
						
			xpp.setInput(stream, null);
			int eventType = xpp.getEventType();
			String text = null;
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					if(xpp.getName().equals("Event")) {
						xpp.next();
						xpp.next();
						xpp.next();
						xpp.next();
					}
					else if(xpp.getName().equals("Control")) {
						xpp.next();
						poles.add(current = new Pole());
					}
					else if(xpp.getName().equals("Position")) {
						Location temp = new Location("RouteTracker");
						temp.setLatitude(Double.parseDouble(xpp.getAttributeValue(null, "lat")));
						temp.setLongitude(Double.parseDouble(xpp.getAttributeValue(null, "lng")));
						current.setLocation(temp);
					}
				}
				
				else if(eventType == XmlPullParser.TEXT && !xpp.isWhitespace()) {
					text = xpp.getText();
				}
				
				else if(eventType == XmlPullParser.END_TAG) {
					if (xpp.getName().equals("Id")) {
						current.setId(Integer.parseInt(text));
					}
					
					else if (xpp.getName().equals("Name")) {
						current.setName(text);
					}
					
					else if (xpp.getName().equals("Difficulty")) {
						current.setDifficulty(text);
					}
				}
				eventType = xpp.next();
			}
			Pole.mPoles = poles;
		}
		
		catch(Exception e) {
			// Create empty set
			Pole.mPoles = new ArrayList<Pole>();
		}
	}
	
	public static List<Pole> getPoles(Context pContext) {
		if (Pole.mPoles == null) {
			Pole.createFromXML(pContext);
		}
		return Pole.mPoles;
	}
	
	public static void sortPoles() {
		Collections.sort(Pole.mPoles);
	}

	@Override
	public int compareTo(Pole another) {
		return (int) (this.getDistance() - another.getDistance());
	}
}