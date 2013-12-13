package com.sprekigjovik.tracker;

import android.content.Context;
/**
 * Class used to properly format time when writing out.
 * @author jehans, John, Martin
 *
 */
public class FormatTime {
	private Integer mSeconds;
	private Context mContext = RouteTrackerApplication.getGlobalContext();
	public FormatTime(float seconds) {
		this.mSeconds = (int) Math.floor(seconds);
	}
	
	public String format() {
		String formatted = "";
		int weeks = 0, days = 0, hours = 0, minutes = 0, seconds = this.mSeconds;
		if (this.mSeconds == null) { return "N/A"; }
		if (seconds > 60) { // Minutes
			if (seconds > 3600) { // Hours
				if (seconds > 86400) { // Days
					if (seconds > 604800) {
						weeks = (int) Math.floor(seconds / 604800);
						seconds = seconds - (604800 * weeks);
					}
					days = (int) Math.floor(seconds / 86400);
					seconds = seconds - (86400 * days);
				}
				hours = (int) Math.floor(seconds / 3600);
				seconds = seconds - (3600 * hours);
			}
			minutes = (int) Math.floor(seconds / 60);
			seconds = seconds - (60 * minutes);
			if (weeks > 0) {
				formatted = weeks + " " + this.mContext.getString(R.string.unit_weeks);
				if (days > 0) {
					formatted += ", " + days + " " + this.mContext.getString(R.string.unit_days);
				}
			}
			
			else if (days > 0) {
				formatted = days + " " + this.mContext.getString(R.string.unit_days);
				if (hours > 0) {
					formatted += ", " + hours + " " + this.mContext.getString(R.string.unit_hours);
				}
			}
			
			else if (hours > 0) {
				formatted = hours + " " + this.mContext.getString(R.string.unit_hours);
				if (minutes > 0) {
					formatted += ", " + minutes + " " + this.mContext.getString(R.string.unit_minutes);
				}
			}
			
			else if (minutes > 0) {
				formatted = minutes + " " + this.mContext.getString(R.string.unit_minutes);
				if (seconds > 0) {
					formatted += ", " + seconds + " " + this.mContext.getString(R.string.unit_seconds);
				}
			}
		}
		
		else {
			formatted = this.mSeconds + " " + this.mContext.getString(R.string.unit_seconds);
		}
		return formatted;
	}
}
