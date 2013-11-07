package com.example.sprekigjovik;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;

public class MapActivity extends Activity {
	private TrackingService mService;
	private ServiceConnection mConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set up service connection
		this.mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mService = ((TrackingService.TrackingBinder) binder).getService();
			}

			public void onServiceDisconnected(ComponentName className) {
				mService = null;
			}
		};
	}
	
	protected void onResume() {
		// Start GPS-updater service
		Intent intent = new Intent(this, TrackingService.class);
		startService(intent);
		// Bind to the tracker service
	    bindService(new Intent(this, TrackingService.class), this.mConnection, Context.BIND_AUTO_CREATE);
	}
	
	protected void onPause() {
		// Unbind from the tracker service
		unbindService(this.mConnection);
	}

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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
