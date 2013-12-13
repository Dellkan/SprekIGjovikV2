package com.sprekigjovik.tracker;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Class to handle routedata and databases for route.
 * @author John, Jehans, Martin
 *
 */
public class Route {
	private long mID = 0;
	private Long mTimeStampStart;
	private Long mTimeStampStop;
	private float mDistance;
	private List<LatLng> mPoints = new ArrayList<LatLng>();
	private static GoogleMap mMap;
	private Polyline mRoute;
	private boolean mIsSelected = false;
	private static SQLiteDatabase mDB;
	private static List<Route> mRoutes;
	private boolean mLoaded = false;
	
	/**
	 * Opens and creates databases if needed.
	 * @return opened database object.
	 */
	private static SQLiteDatabase getDB() {
		if (Route.mDB == null) {
			Route.mDB = RouteTrackerApplication.getGlobalContext().openOrCreateDatabase("sprekigjovik.db", Context.MODE_PRIVATE, null);
			// Set up database tables
			Route.mDB.execSQL("CREATE TABLE IF NOT EXISTS routes " +
				"(" +
					"id INTEGER NOT NULL PRIMARY KEY," +
					"timeStart INTEGER NOT NULL," +
					"timeEnd INTEGER NULL," +
					"distance INTEGER NOT NULL" +
				")"
			);
			Route.mDB.execSQL("CREATE TABLE IF NOT EXISTS routeNodes " +
				"(" +
					"id INTEGER NOT NULL PRIMARY KEY," +
					"nodeParent INTEGER NOT NULL," +
					"longitude REAL NOT NULL," +
					"latitude REAL NOT NULL," +
					"timestamp INTEGER NOT NULL" +
				")"
			);
		}
		return Route.mDB;
	}
	/**
	 * Will get all earlier tracked routes that the user has tracked.
	 * @param pMap Assigns the map to the route.
	 * @return Returns a list over all the earlier tracked routes.
	 */
	public static List<Route> getRoutes(GoogleMap pMap) {
		if (pMap != null) {
			Route.mMap = pMap;
		}
		Route.mMap = pMap;
		if (Route.mRoutes == null) {
			Route.mRoutes = new ArrayList<Route>();
			
			Cursor cursor = Route.getDB().rawQuery("SELECT * FROM routes ORDER BY timeStart DESC", null);
		    if (cursor.moveToFirst()) {
		    	int idIndex = cursor.getColumnIndex("id");
		    	int timeStartIndex = cursor.getColumnIndex("timeStart");
		    	int timeEndIndex = cursor.getColumnIndex("timeEnd");
		    	int distanceIndex = cursor.getColumnIndex("distance");
		    	do { // For gods sake, let's hope these come in right order
		    		Route route = new Route(cursor.getInt(idIndex));
		    		route.mTimeStampStart = cursor.getLong(timeStartIndex);
		            if (!cursor.isNull(timeEndIndex)) {
		            	route.mTimeStampStop = cursor.getLong(timeEndIndex);
		            }
		    		route.mDistance = cursor.getFloat(distanceIndex);
		    		
		    		Route.mRoutes.add(route);
		    	} while(cursor.moveToNext());
		    }
		}
		return Route.mRoutes;
	}
	/**
	 * Makes a new Route object when a new tracking is started.
	 */
	public Route() {
		this.mTimeStampStart = System.currentTimeMillis();
		this.mTimeStampStop = this.mTimeStampStart;
		this.mDistance = 0;
		
		ContentValues values = new ContentValues();
		values.put("timeStart", this.mTimeStampStart);
		values.put("timeEnd", this.mTimeStampStop);
		values.put("distance", this.mDistance);
		
		// Insert into db. It'll return the new ID it created.
		this.mID = Route.getDB().insertOrThrow("routes", null, values);
		
		Route.getRoutes(Route.mMap).add(0, this);
		
		// Tag information of nodes as current. No need to load from db
		this.mLoaded = true;
	}
	
	/**
	 * Sets process-ID of this Route object. Used for service and tracking.
	 * @param pID Process-ID
	 */
	public Route(int pID) {
		this.mID = pID;
	}
	
	/**
	 * Gets all the coordinates for a shown route.
	 */
	public void loadNodes() {
		if (this.mID > 0 && !this.mLoaded) {
		    // Set up route points
		    Cursor routePointsCursor = Route.getDB().rawQuery(
	    		String.format("SELECT * FROM routeNodes WHERE nodeParent = %d", this.mID), null
	    	);
		    if (routePointsCursor.moveToFirst()) {
		    	int latitudeIndex = routePointsCursor.getColumnIndex("latitude");
		    	int longitudeIndex = routePointsCursor.getColumnIndex("longitude");
		    	do { // For gods sake, let's hope these come in right order
		    		this.mPoints.add(new LatLng(
		    			routePointsCursor.getFloat(latitudeIndex), 
		    			routePointsCursor.getFloat(longitudeIndex)
		    		));
		    	} while(routePointsCursor.moveToNext());
		    }
		    this.mLoaded = true;
		}
	}
	
	/**
	 * Adds a new coordinate to a route.
	 * @param point Coordinate of a users recent GPS position.
	 */
	public void addPoint(Location point) {
		// Add point
		this.mPoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
		
		// Update distance
		if (this.mPoints.size() >= 2) {
			LatLng last = this.mPoints.get(this.mPoints.size()-2);
			Location oldLoc = new Location("oldLoc");
			oldLoc.setLatitude(last.latitude);
			oldLoc.setLongitude(last.longitude);
			
			this.mDistance += point.distanceTo(oldLoc);
		}
		
		// Set end time
		this.mTimeStampStop = System.currentTimeMillis();
		
		// Store point in DB
		ContentValues pointValues = new ContentValues();
		pointValues.put("nodeParent", this.mID);
		pointValues.put("latitude", point.getLatitude());
		pointValues.put("longitude", point.getLongitude());
		pointValues.put("timestamp", System.currentTimeMillis());
		Route.getDB().insertOrThrow("routeNodes", null, pointValues);
		
		// Update route meta data
		ContentValues routeValues = new ContentValues();
		routeValues.put("timeEnd", this.getStopTimeStamp());
		routeValues.put("distance", this.getDistance());
		Route.getDB().update("routes", routeValues, "id = " + this.mID, null);
	}
	
	/**
	 * Stops the GPS tracking of a route, and finishes its enddata for the database.
	 */
	public void stop() {
		if (this.mID != 0) {
			// If empty, delete
			if (this.mPoints.isEmpty()) {
				Route.getDB().delete("routes", "id = " + this.mID, null);
				Route.getRoutes(Route.mMap).remove(this);
			}
			
			// Otherwise, update db
			else { 
				ContentValues values = new ContentValues();
				values.put("timeEnd", this.mTimeStampStop);
				Route.getDB().update("routes", values, "id=" + this.mID, null);
			}
		}
	}
	/**
	 * Shows polylines for a tracked route on the map.
	 * @param pMap Map which the polylines are to be added to.
	 */
	public void exportToMap(GoogleMap pMap) {
		// Only create the route once per map. If it exists, simply modify its points
		if (Route.mMap == null || !Route.mMap.equals(pMap)) {
			if (pMap == null) { return; }
			Route.mMap = pMap;
		}
		if (this.mRoute == null) {
			this.mRoute = Route.mMap.addPolyline(new PolylineOptions()
				.width(15)
				.color(Color.BLUE)
				.zIndex(2)
			);
		}
		this.loadNodes();
		this.mRoute.setPoints(this.mPoints);
	}
	
	/**
	 * 
	 * @return Time when the route started.
	 */
	public long getStartTimeStamp() {
		return this.mTimeStampStart;
	}
	
	/**
	 * 
	 * @return Time when the route ended.
	 */
	public long getStopTimeStamp() {
		return this.mTimeStampStop;
	}
	
	/**
	 * 
	 * @return Elapsed seconds of the route
	 */
	public long getElapsedTime() {
		if (this.mTimeStampStop != null) {
			return (this.mTimeStampStop - this.mTimeStampStart) / 1000;
		}
		
		else {
			return (System.currentTimeMillis() - this.mTimeStampStart) / 1000;
		}
	}
	
	/**
	 * 
	 * @return total distance traveled on the route.
	 */
	public float getDistance() {
		return this.mDistance;
	}

	/**
	 * 
	 * @return ID for this route.
	 */
	public long getId() {
		return this.mID;
	}

	/**
	 * Deletes entire route database if the user prompts to.
	 */
	public static void deleteAll() {
		Route.mRoutes.clear();
		Route.getDB().delete("routes", null, null);
		Route.getDB().delete("routeNodes", null, null);
	}

	/**
	 * 
	 * @return true/false if this route is selected
	 */
	public boolean isSelected() {
		return this.mIsSelected ;
	}
	
	/**
	 * Will export the points to the map if it is selected, or remove <br>
	 * the route from the map if it is already selected.
	 * @param toggle true/false if it is selected.
	 */
	public void setSelected(boolean toggle) {
		this.mIsSelected = toggle;
		if (this.mIsSelected) {
			this.exportToMap(Route.mMap);
		}
		
		else {
			if (this.mRoute != null) {
				this.mRoute.remove();
				this.mRoute = null;
			}
		}
	}
}