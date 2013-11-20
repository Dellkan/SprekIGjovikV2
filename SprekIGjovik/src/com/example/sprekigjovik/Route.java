package com.example.sprekigjovik;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Route {
	private int mID = 0;
	private long mTimeStampStart;
	private long mTimeStampStop;
	private List<LatLng> mPoints = new ArrayList<LatLng>();
	private GoogleMap mMap;
	private Polyline mRoute;
	
	public Route() {}
	
	public Route(int pID) {
		this.mID = pID;
		this.load();
	}
	
	private void load() {
		if (this.mID > 0) {
			// DB query. woop woop
		}
	}
	
	public void save() {
		if (this.mID > 0) {
			// Update DB query -- Does this really ever need to occur?
		}
		
		else {
			// Insert DB query
		}
	}
	
	public void addPoint(LatLng point) {
		// Set starting point
		if (this.mPoints.size() == 0) {
			this.mTimeStampStart = System.currentTimeMillis();
		}
		
		// Add point
		this.mPoints.add(point);
		
		// Set ending time
		this.mTimeStampStop = System.currentTimeMillis();
	}
	
	public void exportToMap(GoogleMap pMap) {
		// Only create the route once per map. If it exists, simply modify its points
		if (this.mMap == null || this.mMap.equals(pMap)) {
			this.mMap = pMap;
			this.mRoute = pMap.addPolyline(new PolylineOptions()
				.width(25)
				.color(Color.BLUE)
				.zIndex(2)
			);
		}
		this.mRoute.setPoints(this.mPoints);
	}
	
	public long getStartTimeStamp() {
		return this.mTimeStampStart;
	}
	
	public long getStopTimeStamp() {
		return this.mTimeStampStop;
	}
	
	/**
	 * 
	 * @return Elapsed seconds of the route
	 */
	public long getElapsedTime() {
		return (this.mTimeStampStop - this.mTimeStampStart) / 1000;
	}
}
