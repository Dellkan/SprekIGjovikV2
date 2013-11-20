package com.example.sprekigjovik;

import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MapActivity extends Activity {
	// Action stuff
	private boolean isTracking;
	
	// Map stuff
	private GoogleMap mMap;
	private static final LatLng GJOVIK = new LatLng(60.795865, 10.687612);
	private List<PoleMarker> mPoleMarkers = new ArrayList<PoleMarker>();
	
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
					case Pole.DIFF_GREEN: flagResource = R.drawable.flag_black; break;
					case Pole.DIFF_BLUE: flagResource = R.drawable.flag_blue; break;
					case Pole.DIFF_RED:	flagResource = R.drawable.flag_green; break; 
					case Pole.DIFF_BLACK: flagResource = R.drawable.flag_red; break;
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
	
	// Service stuff
	private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	private Messenger mService = null;
	private ServiceConnection mConnection = new ServiceConnection() {
		// When tracking service is connected
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

		// Tracking service is disconnected
		public void onServiceDisconnected(ComponentName className) {
			MapActivity.this.mService = null;
		}
	};

	// Message handler. All incoming messages from the tracking service will be handled here
	static class IncomingHandler extends Handler {
		private MapActivity act;
		public IncomingHandler(MapActivity act) {
			super();
			this.act = act;
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case TrackingService.MSG_LOCATION_UPDATE:
					// Run pole menu update (service updated their distances, etc)
					break;
				case TrackingService.MSG_ROUTE_UPDATE:
					Route route = (Route)msg.obj;
					route.exportToMap(act.mMap);
					break;
				case TrackingService.MSG_START_TRACKING:
					this.act.isTracking = true;
					// Update button
					((Button) this.act.findViewById(R.id.map_tracking_button)).setText(
						this.act.getResources().getString(R.string.map_stop_tracking)
					);
					break;
				case TrackingService.MSG_STOP_TRACKING:
					this.act.isTracking = false;
					// Update button
					((Button) this.act.findViewById(R.id.map_tracking_button)).setText(
						this.act.getResources().getString(R.string.map_start_tracking)
					);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	}
	
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
	
	/*
	 *  Button stuff
	 */
	
	public void fetchPolesFromServer(View v) {
		new DownloadXML(this).execute("http://john.nodedevs.net/sprekigjovik.xml");
	}
	
	public void toggleTracking(View v) {
        if (this.isTracking) {
        	this.stopTracking();
        }
        
        else {
        	this.startTracking();
        }
	}
	
	/*
	 * Activity Life cycle
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Setup map
		this.mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		//this.mMap.setMyLocationEnabled(false);
		this.mMap.addTileOverlay(new TileOverlayOptions()
			.tileProvider(
				new CustomMapTileProvider(getResources().getAssets())
			)
			.zIndex(1)
		);
		this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GJOVIK, 13));
		
		// Fetch poles
		this.showPoles();
	}
	
	@Override
	protected void onResume() {
		// Bind to the tracker service
		bindService(new Intent(this, TrackingService.class), this.mConnection, Context.BIND_AUTO_CREATE);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// Let the service know we don't need any more updates
        try {
            Message msg = Message.obtain(null, TrackingService.MSG_UNREGISTER_CLIENT);
            msg.replyTo = mMessenger;
            this.mService.send(msg);
		} catch (RemoteException e) {
			// Service is already dead. Do nothing :)
		}
		
		// Unbind from the tracker service
		unbindService(this.mConnection);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	/*
	 * Metastuff
	 */

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
