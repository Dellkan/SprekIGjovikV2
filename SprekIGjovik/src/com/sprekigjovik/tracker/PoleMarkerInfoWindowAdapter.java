package com.sprekigjovik.tracker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

/**
 * Handles showing the information window when a marker is pressed.
 * @author John, Martin, Jehans
 *
 */
public class PoleMarkerInfoWindowAdapter implements InfoWindowAdapter {
	private MapActivity mAct;
	public PoleMarkerInfoWindowAdapter(MapActivity pAct) {
		this.mAct = pAct;
	}
	
	/**
	 * Gets information about a chosen marker.
	 * @param Marker marker chosen marker object from map
	 */
	@Override
	public View getInfoContents(Marker marker) {
		// Find PoleMarker
		Pole pole = null;
		for (PoleMarker poleMarker : this.mAct.getPoleMarkers()) {
			if (poleMarker.getMarker().equals(marker)) {
				pole = poleMarker.getPole();
				break;
			}
		}
		if (pole == null) { return null; } // Couldn't find pole info.. abort abort!
		
		// Scroll to & show pole in polelist
		ListView polelist = (ListView) this.mAct.findViewById(R.id.polelist);
		PoleListModel adapter = (PoleListModel) polelist.getAdapter();
		
		adapter.getView(adapter.getPosition(pole), null, null).setActivated(true);
		polelist.setItemChecked(adapter.getPosition(pole), true);
		
		polelist.setSelection(adapter.getPosition(pole));
		
		// Inflate the xml containing the info window layout
		View poleMarkerInfo = this.mAct.getLayoutInflater().inflate(R.layout.pole_info_window, null);
		
		// Name
		((TextView)poleMarkerInfo.findViewById(R.id.pole_info_name)).setText(pole.getName());				
		
		// id
		((TextView)poleMarkerInfo.findViewById(R.id.pole_info_id)).setText("Pole " + Integer.toString(pole.getId())); 
		
		// Location
		((TextView)poleMarkerInfo.findViewById(R.id.pole_info_location)).setText(
			"Lat: " + Double.toString(pole.getLatLng().latitude) + ", " +
			"Long: " + Double.toString(pole.getLatLng().longitude)
		);
			
		// Only show distance, ETA, if preference is enabled
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.mAct);
		boolean prefMapShowPoleDistance = pref.getBoolean("prefMapShowPoleDistance", true);
		
		// Distance
		TextView distance = (TextView)poleMarkerInfo.findViewById(R.id.pole_info_distance);
		if (prefMapShowPoleDistance) {
			distance.setText(
				"Distance: " + 
				Integer.toString(Math.round(pole.getDistance())) + " " + 
				this.mAct.getResources().getString(R.string.unit_meter)
			);
		}
		distance.setVisibility(prefMapShowPoleDistance && pole.getDistance() > 0 ? View.VISIBLE : View.GONE);
		
		// ETA
		TextView eta = (TextView) poleMarkerInfo.findViewById(R.id.pole_info_eta); 
		if (prefMapShowPoleDistance) {
			eta.setText( new FormatTime(pole.getETA()).format() );
		}
		eta.setVisibility(prefMapShowPoleDistance && pole.getLocation().getSpeed() > 0 ? View.VISIBLE : View.GONE);
		
		// Return the info window layout
		return poleMarkerInfo;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		// Return null, so default view is used
		return null;
	}
}