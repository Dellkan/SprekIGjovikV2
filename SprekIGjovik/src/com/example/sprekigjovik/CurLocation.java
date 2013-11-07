package com.example.sprekigjovik;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class CurLocation implements LocationListener {
	private TrackingService service;
	private LocationManager lm;
	private Location curLoc;

	// Constructor
	CurLocation(TrackingService service) {
		
		// Get location manager
		this.lm = ((LocationManager)this.service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE));
		
		// Use last known location until updates are received
		this.curLoc = this.lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		
		// If there is no known location, replace null with an empty location
		if (this.curLoc == null) {
			// Init curLoc, hopefully fixing nullpointerexceptions
			this.curLoc = new Location(LocationManager.PASSIVE_PROVIDER);
		}
		
		// Set up criteria
		Criteria locCriterias = new Criteria();
		locCriterias.setAccuracy(Criteria.ACCURACY_FINE);
		
		// Request regular updates
		this.lm.requestLocationUpdates(5, 10, locCriterias, this, null);
	}
	
    @Override
    public void onLocationChanged(Location loc) {
        this.curLoc = loc;
        // Send updates somewhere
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    public Location getLocation() {
    	return this.curLoc;
    }
    
    public void close() {    	
    	// Remove all updates associated with this object
    	this.lm.removeUpdates(this);
    }
}