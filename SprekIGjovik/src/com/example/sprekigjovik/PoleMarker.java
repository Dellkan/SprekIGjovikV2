package com.example.sprekigjovik;

import com.google.android.gms.maps.model.Marker;

public class PoleMarker {
	private Pole mPole;
	private Marker mMarker;
	public PoleMarker(Pole pole, Marker marker) {
		this.mPole = pole;
		this.mMarker = marker;
	}
	
	public Pole getPole() {
		return this.mPole;
	}
	
	public Marker getMarker() {
		return this.mMarker;
	}
}
