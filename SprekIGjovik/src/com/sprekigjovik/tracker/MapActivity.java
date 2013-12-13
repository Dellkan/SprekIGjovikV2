package com.sprekigjovik.tracker;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

/**
 * Main class for the map screen.
 * @author Jehans, John, Martin
 *
 */
public class MapActivity extends Activity {	
	// Action stuff
	private boolean isTracking = false;
	private SharedPreferences mPref;
	
	// Action bar
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerbutton;
	
	// Map stuff
	private GoogleMap mMap;
	private static final LatLng GJOVIK = new LatLng(60.795865, 10.687612);
	private List<PoleMarker> mPoleMarkers = new ArrayList<PoleMarker>();
	private TileOverlay mCustTile;
	private Route mActiveRoute;
	
	/**
	 * Shows poles on the map with different colors.
	 */
	private void showPoles() {
		// Remove old PoleMarkers, if any
		for (PoleMarker polemarker : this.mPoleMarkers) {
			polemarker.getMarker().remove();
		}
		this.mPoleMarkers.clear();
		
		// Load new list of poles
		List<Pole> poles = Pole.getPoles(this);
		
		// Traverse the poles, and add markers for them
		if (poles != null) {
			for (Pole pole : poles) {
				int flagResource = 0;
				// Find flag resource
				switch(pole.getDifficulty()) {
					case Pole.DIFF_UNKNOWN: flagResource = R.drawable.flag_white; break; 
					case Pole.DIFF_GREEN: flagResource = R.drawable.flag_green; break;
					case Pole.DIFF_BLUE: flagResource = R.drawable.flag_blue; break;
					case Pole.DIFF_RED:	flagResource = R.drawable.flag_red; break; 
					case Pole.DIFF_BLACK: flagResource = R.drawable.flag_black; break;
				}
				
				// Set options for the marker
				MarkerOptions options = new MarkerOptions().position(pole.getLatLng()).title(pole.getName());
				
				// Add custom icon for the marker, provided we have one for this difficulty
				if (flagResource > 0) {
					options.icon(BitmapDescriptorFactory.fromResource(flagResource));
				}
				
				// Create and cache the marker
				this.mPoleMarkers.add(new PoleMarker(pole, this.mMap.addMarker(options)));
			}
		}
	}
	/**
	 * Inserts custom map tiles of the orientation map
	 */
	private void insertCustTiles() {
		this.mCustTile = this.mMap.addTileOverlay(new TileOverlayOptions()
			.tileProvider(
				new CustomMapTileProvider(getResources().getAssets())
			)
			.zIndex(1)
		);
	}
	
	// Service stuff
	private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	private Messenger mService = null;
	private final ServiceConnection mConnection = new ServiceConnection() {
		// When tracking service is connected
		/**
		 * Handles the tracking service whenever it is connected.
		 */
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = new Messenger(binder);
			
            try {
                Message msg = Message.obtain(null, TrackingService.MSG_REGISTER_CLIENT);
                msg.obj = MapActivity.this.mPoleMarkers;
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
		}

		/**
		 * Tracking service is disconnected.
		 */
		public void onServiceDisconnected(ComponentName className) {
			MapActivity.this.mService = null;
		}
	};

	/** 
	 * Message handler. All incoming messages from the tracking service will be handled here
	 *
	 */
	static class IncomingHandler extends Handler {
		private MapActivity mAct;
		private RouteListModel mRoutelistAdapter; 
		public IncomingHandler(MapActivity act) {
			super();
			this.mAct = act;
		}
		
		/**
		 * Handles different functions and options of the tracking service.
		 */
		@Override
		public void handleMessage(Message msg) {
			if (this.mRoutelistAdapter == null) {
				ListView routelist = (ListView)this.mAct.findViewById(R.id.routelist);
				if (routelist != null) {
					this.mRoutelistAdapter = (RouteListModel) routelist.getAdapter();
				}
			}
			switch (msg.what){
				case TrackingService.MSG_LOCATION_UPDATE:
					// Run pole menu update (service updated their distances, etc)
					if (this.mAct.isTracking && msg.obj != null) {
						this.mAct.mActiveRoute = (Route)msg.obj;
						
						if (this.mAct.getActiveRoute().isSelected()) {
							this.mAct.getActiveRoute().exportToMap(this.mAct.mMap);
						}
					}
					// Update pole list
					((PoleListModel)((ListView) this.mAct.findViewById(R.id.polelist)).getAdapter()).notifyDataSetChanged();
					// Update GPS signal image
					ImageView gps_signal = ((ImageView) this.mAct.findViewById(R.id.gps_signal));
					if (msg.arg1 <= 7) {
						gps_signal.setColorFilter(Color.argb(150, 0, 150, 0));
					}
					
					else if (msg.arg1 <= 15) {
						gps_signal.setColorFilter(Color.argb(150, 255, 102, 0));
					}
					
					else {
						gps_signal.setColorFilter(Color.argb(150, 255, 0, 0));
					}
					break;
				case TrackingService.MSG_START_TRACKING:
					this.mAct.isTracking = true;
					// Update button
					this.mAct.invalidateOptionsMenu();
					this.mAct.mActiveRoute = (Route)msg.obj;
					
					if (this.mAct.mPref.getBoolean("prefMapShowGPSLoc", false)) {
						this.mAct.mActiveRoute.setSelected(true);
					}
					break;
				case TrackingService.MSG_STOP_TRACKING:
					this.mAct.isTracking = false;
					// Update button
					this.mAct.invalidateOptionsMenu();
					break;
				case TrackingService.MSG_GPSSTATUS_CHANGE:
					boolean status = (Boolean) msg.obj;
					this.mAct.findViewById(R.id.gps_disabled_warning).setVisibility(!status ? View.VISIBLE : View.GONE);
					((ImageView) this.mAct.findViewById(R.id.gps_signal)).setColorFilter(Color.argb(150, 0, 0, 0));
					break;
				default:
					super.handleMessage(msg);
			}
			if (this.mRoutelistAdapter != null) {
				this.mRoutelistAdapter.notifyDataSetChanged();
			}
		}
	}
	/**
	 * Starts GPS tracking using a service so <br>
	 * it can be run while the phone is forexample in the pocket.
	 */
	public void startTracking() {
        try {
        	// Make the service persistent, so tracking can continue while cell phone is in pocket
    		startService(new Intent(this, TrackingService.class));
    		
    		// Tell the service to start tracking. This can probably be merged with the above using the intent
            Message msg = Message.obtain(null, TrackingService.MSG_START_TRACKING);
            mService.send(msg);
            
            this.isTracking = true;
        } 
        catch (RemoteException e) {
        	// Service is gone.. Does user care? Should we notify the user?
        }
	}
	
	/**
	 * Stops the service GPS tracking service.<br>
	 */
	public void stopTracking() {
        try {
        	// The service doesn't need to be persistent anymore. Stop it.
    		stopService(new Intent(this, TrackingService.class));
    		
    		// Tell the service to stop tracking
            Message msg = Message.obtain(null, TrackingService.MSG_STOP_TRACKING);
            mService.send(msg);
            
            this.isTracking = false;
        } 
        catch (RemoteException e) {
        	// Service is gone.. Does user care? Should we notify the user?
        }		
	}
	
	/**
	 *  Button stuff handling start/stop for gps tracking.
	 */
	public void toggleTracking() {
        if (this.isTracking) {
        	this.stopTracking();
        }
        
        else {
        	this.startTracking();
        }
	}
	
	/**
	 * Activity Life cycle
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set default preferences values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// Get preferences
		this.mPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Layout
		this.setContentView(R.layout.map_activity);
		
		// Setup action bar home button
	    // Set up drawer toggle button
	    this.mDrawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
	    this.mDrawerbutton = new ActionBarDrawerToggle(
	    	this, this.mDrawer, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                MapActivity.this.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                MapActivity.this.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        this.getActionBar().setDisplayShowHomeEnabled(true);
        this.mDrawerbutton.setDrawerIndicatorEnabled(true);
        this.mDrawerbutton.syncState();

        // Set the drawer toggle as the DrawerListener
        this.mDrawer.setDrawerListener(this.mDrawerbutton);
		
		// Setup map
		this.mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		this.mMap.setMyLocationEnabled(this.mPref.getBoolean("prefMapShowGPSLoc", false));
		// Load custom tiles, if user wants them
		if (this.mPref.getBoolean("prefMapUseCustTiles", true)) {
			this.insertCustTiles();
		}
		this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GJOVIK, 13));
		
		// Fetch poles
		this.showPoles();
		
		// Set custom info window
		this.mMap.setInfoWindowAdapter(new PoleMarkerInfoWindowAdapter(this));
		
		// Set pole list adapter 
		ListView polelist = (ListView)this.findViewById(R.id.polelist);
		polelist.setAdapter(new PoleListModel(this, Pole.getPoles(this)));
		polelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				for (PoleMarker poleMarker : MapActivity.this.mPoleMarkers) {
					Pole pole = (Pole) parent.getItemAtPosition(pos);
					if (poleMarker.getPole().equals(pole)) {
						poleMarker.getMarker().showInfoWindow();
						
						// TODO: Code to focus on marker
						// MapActivity.this.mMap.animateCamera()
						break;
					}
				}
			}
		});
		
		// Load pole list show preference
		this.togglePolelist(this.mPref.getBoolean("prefPoleListShow", true));
		
		// Set route list adapter
		ListView routelist = (ListView)this.findViewById(R.id.routelist);
		routelist.setAdapter(new RouteListModel(this));
		polelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// Setup warning click handler
		((TextView) this.findViewById(R.id.gps_disabled_warning)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});
	}
	
	/**
	 * Handles onReseume and preferences for the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Bind to the tracker service
		this.bindService(new Intent(this, TrackingService.class), this.mConnection, Context.BIND_AUTO_CREATE);
		
		// Update preferences
		this.mPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Check if myLocation setting was changed
		if (this.mMap.isMyLocationEnabled() != this.mPref.getBoolean("prefMapShowGPSLoc", false)) {
			this.mMap.setMyLocationEnabled(this.mPref.getBoolean("prefMapShowGPSLoc", false));
		}
		
		// Check if custom tiles option was changed
		if (this.mCustTile != null && !this.mPref.getBoolean("prefMapUseCustTiles", true)) {
			this.mCustTile.remove();
		}
		
		else if (this.mCustTile == null && this.mPref.getBoolean("prefMapUseCustTiles", true)) {
			this.insertCustTiles();
			this.mCustTile = null;
		}
		
		// Check GPS
		if (((LocationManager) this.getSystemService( Context.LOCATION_SERVICE )).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			((ImageView) this.findViewById(R.id.gps_signal)).setColorFilter(Color.argb(150, 255, 0, 0));
			this.findViewById(R.id.gps_disabled_warning).setVisibility(View.GONE);
		}
		
		else {
			((ImageView) this.findViewById(R.id.gps_signal)).setColorFilter(Color.argb(150, 0, 0, 0));
			this.findViewById(R.id.gps_disabled_warning).setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Handles pausing of the application.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Let the service know we don't need any more updates
        try {
        	if (this.mService != null) { // TODO: mService throwing nullpointerexceptions. Find out why. Replace cheap fix.
	            Message msg = Message.obtain(null, TrackingService.MSG_UNREGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            this.mService.send(msg);
        	}
		} catch (RemoteException e) {
			// Service is already dead. Do nothing :)
		}
		
		// Unbind from the tracker service
		this.unbindService(this.mConnection);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * Metastuff
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_track)
			.setIcon(this.isTracking ? R.drawable.tracking : R.drawable.track)
			.setTitle(this.isTracking ? R.string.action_tracking : R.string.action_track);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			this.mDrawerbutton.onOptionsItemSelected(item);
			// Update info in route list
			((RouteListModel)((ListView)this.findViewById(R.id.routelist)).getAdapter()).notifyDataSetChanged();
			return true;
		} else if (id == R.id.action_track) {
			this.toggleTracking();
			return true;
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent();
			intent.setClass(MapActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		} else if (id == R.id.action_showpolelist) {
			this.togglePolelist(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Shows polelist menu.
	 * @param toggle true/false for toggling polelist menu
	 */
	public void togglePolelist(Boolean toggle) {
		ListView polelist = (ListView) this.findViewById(R.id.polelist);
		LayoutParams layout = polelist.getLayoutParams();
		
		if (toggle == null) {
			toggle = layout.width == 0;
		}
		
		if (toggle) {
			layout.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
			polelist.setLayoutParams(layout);
		}
		
		else {
			layout.width = 0;
			polelist.setLayoutParams(layout);
		}
		
        Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit(); 
        pref.putBoolean("prefPoleListShow", toggle);
        pref.commit();
	}
	
	/**
	 * 
	 * @return List of poleMarkers
	 */
	public List<PoleMarker> getPoleMarkers() {
		return this.mPoleMarkers;
	}
	/**
	 * 
	 * @return boolean true/false if application is tracking or not.
	 */
	public boolean isTracking() {
		return this.isTracking;
	}
	
	/**
	 * 
	 * @return active tracked route.
	 */
	public Route getActiveRoute() {
		return this.mActiveRoute;
	}
	
	/**
	 * 
	 * @return active google map object.
	 */
	public GoogleMap getMap() {
		return this.mMap;
	}
}
