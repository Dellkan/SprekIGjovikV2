package com.example.sprekigjovik;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TrackingService extends Service {
	private final IBinder mBinder = new TrackingBinder();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 0;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return this.mBinder;
	}
	
	public class TrackingBinder extends Binder {
		TrackingService getService() {
			return TrackingService.this;
		}
	}
}
