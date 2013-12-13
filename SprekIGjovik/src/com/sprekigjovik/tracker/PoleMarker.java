package com.sprekigjovik.tracker;

import com.google.android.gms.maps.model.Marker;
/**
 * Handles a single polemarker on the map
 * @author John, Martin, Jehans
 *
 */
public class PoleMarker {
	private Pole mPole;
	private Marker mMarker;
	public PoleMarker(Pole pole, Marker marker) {
		this.mPole = pole;
		this.mMarker = marker;
	}
	
	/**
	 * Gets the chosen pole on what marker has been chosen on the map.
	 * @return the pole from the chosen marker
	 */
	public Pole getPole() {
		return this.mPole;
	}
	
	/**
	 * 
	 * @return gets chosen marker from the map.
	 */
	public Marker getMarker() {
		return this.mMarker;
	}
}
