package com.sprekigjovik.tracker;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
/**
 * Handles different GPS functions such as locationchanged.
 * @author Jehans, John, Martin
 *
 */
public class LocationTracker implements LocationListener {
	private TrackingService mService;
	private LocationManager lm;
	private Location curLoc;

	// Constructor
	LocationTracker(TrackingService service) {
		this.mService = service;
		// Get location manager
		this.lm = ((LocationManager)this.mService.getApplicationContext().getSystemService(Context.LOCATION_SERVICE));
		
		// If there is no known location, replace null with an empty location
		if (this.curLoc == null) {
			// Init curLoc, hopefully fixing nullpointerexceptions
			this.curLoc = new Location(LocationManager.PASSIVE_PROVIDER);
		}
		
		/*
		// Set up criteria
		Criteria locCriterias = new Criteria();
		locCriterias.setAccuracy(Criteria.ACCURACY_FINE);
		locCriterias.setSpeedRequired(true);
		locCriterias.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
		
		// Request regular updates
		this.lm.requestLocationUpdates(5000, 10, locCriterias, this, null);
		*/
		this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
	}
	
    @Override
    public void onLocationChanged(Location loc) {
        this.curLoc = loc;
    	// Run update
    	this.mService.runLocationUpdate(this.curLoc);
    }

    @Override
    public void onProviderDisabled(String provider) {
    	this.mService.runGPSStatusUpdate(false);
    }

    @Override
    public void onProviderEnabled(String provider) {
    	this.mService.runGPSStatusUpdate(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    public Location getLocation() {
    	return this.curLoc;
    }
    
    /**
     * Removes all updates associated with this GPS object.
     */
    public void close() {
    	// Remove all updates associated with this object
    	this.lm.removeUpdates(this);
    }
}