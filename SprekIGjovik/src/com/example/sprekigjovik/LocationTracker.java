package com.example.sprekigjovik;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

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
		
		// Set up criteria
		Criteria locCriterias = new Criteria();
		locCriterias.setAccuracy(Criteria.ACCURACY_FINE);
		
		// Request regular updates
		this.lm.requestLocationUpdates(500, 10, locCriterias, this, null);
	}
	
    @Override
    public void onLocationChanged(Location loc) {
        this.curLoc = loc;
        Toast.makeText(this.mService.getApplicationContext(), "Loc update! Acc: " + Float.toString(loc.getAccuracy()), Toast.LENGTH_SHORT).show();
        // Send updates somewhere
        if (this.curLoc.getAccuracy() <= 3000) { // Review required accuracy
        	// Run update
        	this.mService.runLocationUpdate(this.curLoc);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    	
    }

    @Override
    public void onProviderEnabled(String provider) {
    	
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	
    }
    
    public Location getLocation() {
    	return this.curLoc;
    }
    
    public void close() {
    	// Remove all updates associated with this object
    	this.lm.removeUpdates(this);
    }
}