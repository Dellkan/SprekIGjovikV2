package com.sprekigjovik.tracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class RouteListModel extends BaseAdapter implements OnClickListener {
	private MapActivity mActivity;
	public RouteListModel(MapActivity pAct) {
		this.mActivity = pAct;
	}
	
	@Override
	public Route getItem(int position) {
		return Route.getRoutes(this.mActivity.getMap()).get(position);
	}
	
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		Route route = this.getItem(position);

		if (row == null) {
			LayoutInflater inflater = this.mActivity.getLayoutInflater();
			row = inflater.inflate(R.layout.route_list_single, null, true);
			
			// Set onclick handler
			row.setOnClickListener(this);
			row.findViewById(R.id.select_route_check).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					((Route)view.getTag()).setSelected(((CheckBox) view).isChecked());
					RouteListModel.this.notifyDataSetChanged();
				}
			});
		}
		
		// Set datetime
		((TextView) row.findViewById(R.id.route_list_item_datetime)).setText(
			SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(
				new Date(route.getStartTimeStamp())
			)
		);
		
		CheckBox checkbox = ((CheckBox) row.findViewById(R.id.select_route_check));
		checkbox.setChecked(route.isSelected());
		row.setSelected(route.isSelected());
		((ListView) this.mActivity.findViewById(R.id.routelist)).setItemChecked(position, route.isSelected());
		checkbox.setTag(route);
		
		// Set tracking visibility
		if (this.mActivity.isTracking() && route.equals(this.mActivity.getActiveRoute())) { 
			row.findViewById(R.id.route_list_item_is_tracking).setVisibility(View.VISIBLE);
		}
		
		else {
			row.findViewById(R.id.route_list_item_is_tracking).setVisibility(View.GONE);
		}
		
		// Set timeago
		((TextView) row.findViewById(R.id.route_list_item_timeago)).setText(
			new FormatTime((System.currentTimeMillis() - route.getStartTimeStamp()) / 1000).format() + " ago"
		);
		
		// Set elapsed time and distance
		((TextView) row.findViewById(R.id.route_list_item_distance)).setText(
			new FormatTime(route.getElapsedTime()).format() + " : " + new FormatDistance(route.getDistance()).format()
		);
		
		return row;
	}
	
	@Override
	public int getCount() {
		return Route.getRoutes(this.mActivity.getMap()).size();
	}

	@Override
	public long getItemId(int position) {
		return this.getItem(position).getId();
	}

	@Override
	public void onClick(View view) {
		CheckBox checkbox = (CheckBox) view.findViewById(R.id.select_route_check);
		Route route = (Route) checkbox.getTag();
		checkbox.setChecked(!checkbox.isChecked());
		route.setSelected(checkbox.isChecked());
		this.notifyDataSetChanged();
	}
}