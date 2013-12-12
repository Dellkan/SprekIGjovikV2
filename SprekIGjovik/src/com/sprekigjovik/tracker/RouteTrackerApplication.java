package com.sprekigjovik.tracker;

public class RouteTrackerApplication extends android.app.Application {
	private static RouteTrackerApplication mGlobalContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		RouteTrackerApplication.mGlobalContext = this;
	}
	
	public static RouteTrackerApplication getGlobalContext() {
		return RouteTrackerApplication.mGlobalContext;
	}
}
