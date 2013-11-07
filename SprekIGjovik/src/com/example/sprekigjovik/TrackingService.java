package com.example.sprekigjovik;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

public class TrackingService extends Service {
	private final IBinder mBinder = new TrackingBinder();
	private boolean mActiveTracking;
	private static Context mContext;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return this.mBinder;
	}
	
	@Override
	public void onCreate() {
		// Start actual tracking
		CurLocation curloc = new CurLocation(this);
	}
	
    @Override
    public boolean onUnbind(Intent intent) {
    	if (!mActiveTracking) {
    		stopSelf();
    		//mThread.;
    	}
        return true;
    }
	
	public class TrackingBinder extends Binder {
		TrackingService getService() {
			return TrackingService.this;
		}
	}
	
	public static Context getContext() {
		return TrackingService.mContext;
	}
	
	public void runLocationUpdate(Location curLoc) {
		
	}
}
