package com.example.sprekigjovik;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Route {
	GoogleMap mMapReference;
	private Polyline mRoute;
	private float mTimeStamp;
	
	public Route() {
		this.load();
	}
	
	public Route(GoogleMap map) {
		this.mRoute = map.addPolyline(new PolylineOptions());
	}
	
	private void load() {
		
	}
	
	public void save() {
		
	}
}
