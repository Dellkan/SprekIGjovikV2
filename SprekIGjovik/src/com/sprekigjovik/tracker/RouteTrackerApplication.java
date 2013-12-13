package com.sprekigjovik.tracker;

/**
 * Used for united activity in some functions where needed.
 * @author John, Martin, Jehans
 *
 */
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
