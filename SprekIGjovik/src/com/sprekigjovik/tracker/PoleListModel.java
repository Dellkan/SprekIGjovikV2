package com.sprekigjovik.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/**
 * ListModel for pole objects.  Used in the slider menu.
 * @author Jehans, Martin, John
 *
 */
@SuppressLint("SimpleDateFormat")
public class PoleListModel extends ArrayAdapter<Pole> {
	private Activity mActivity;
	private List<Pole> mPoles;
	private Map<Pole, View> mViews = new HashMap<Pole, View>();
	public PoleListModel(Activity pActivity, List<Pole> pPoles) {
		super(pActivity, R.layout.pole_list_single, pPoles);
		this.mActivity = pActivity;
		this.mPoles = pPoles;
	}
	
	/**
	 * Gets pole object at chosen index.
	 * @param position index for chosen pole
	 * @return chosen pole object
	 */
	@Override
	public Pole getItem(int position) {
		return this.mPoles.get(position);
	}
	
	/**
	 * Gets what poles to show on the menu.
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		Pole pole = this.mPoles.get(position);
		
		View row = this.mViews.get(pole);
		
		if (row == null) {
			LayoutInflater inflater = this.mActivity.getLayoutInflater();
			row = inflater.inflate(R.layout.pole_list_single, null, true);
			this.mViews.put(pole, row);
			
			// Set color
			switch(pole.getDifficulty()) {
				case Pole.DIFF_GREEN: row.setBackgroundResource(R.drawable.polelist_row_green); break;
				case Pole.DIFF_BLUE: row.setBackgroundResource(R.drawable.polelist_row_blue); break;
				case Pole.DIFF_RED:	row.setBackgroundResource(R.drawable.polelist_row_red); break; 
				case Pole.DIFF_BLACK: row.setBackgroundResource(R.drawable.polelist_row_black); break;
				default: row.setBackgroundResource(R.drawable.polelist_row_white); break;
			}
			
			// Set title
			((TextView) row.findViewById(R.id.pole_list_item_title)).setText(
				Integer.toString(pole.getId()) + " " + pole.getName()
			);
		}
		
		// These can update, and should be refreshed
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
		boolean prefMapShowPoleDistance = pref.getBoolean("prefMapShowPoleDistance", true); 
		
		// Set distance		
		TextView distance = (TextView) row.findViewById(R.id.pole_list_item_distance);
		if (prefMapShowPoleDistance) {
			distance.setText(
				Integer.toString(Math.round(pole.getDistance())) + " " + this.mActivity.getResources().getString(R.string.unit_meter)
			);
		}
		distance.setVisibility(prefMapShowPoleDistance && pole.getDistance() > 0 ? View.VISIBLE : View.GONE);
		
		// Set eta
		TextView eta = (TextView) row.findViewById(R.id.pole_list_item_eta); 
		if (prefMapShowPoleDistance) {
			eta.setText( new FormatTime(pole.getETA()).format() );
		}
		eta.setVisibility(
			prefMapShowPoleDistance && 
			(pole.getETA() > 0 && pole.getETA() <= 86400) ? View.VISIBLE : View.GONE
		);
		
		return row;
	}
	
	/**
	 * 
	 * @param pole object chosen.
	 * @return view for the pole object
	 */
	public View getView(Pole pole) {
		return this.mViews.get(pole);
	}
	
	/**
	 * Gets how many poleobjects there are in the list.
	 * @return how many poles in the list
	 */
	@Override
	public int getCount() {
		if (this.mPoles != null) {
			return this.mPoles.size();
		}
		return 0;
	}
}
