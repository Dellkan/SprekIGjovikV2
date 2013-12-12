package com.sprekigjovik.tracker;

import android.content.Context;

public class FormatDistance {
	private Integer mMetres;
	private Context mContext = RouteTrackerApplication.getGlobalContext();
	public FormatDistance(float metres) {
		this.mMetres = (int) Math.floor(metres);
	}
	
	public String format() {
		String formatted = "";
		int kilometres = 0, metres = this.mMetres;
		if (this.mMetres == null) { return "N/A"; }
		if (metres > 1000) { // metres
			kilometres = (int) Math.floor(metres / 1000);
			metres = metres - (1000 * kilometres);
			
			formatted = kilometres + " " + this.mContext.getString(R.string.unit_kilometer);
			if (metres > 0) {
				formatted += ", " + metres + " " + this.mContext.getString(R.string.unit_meter);
			}
		}
		
		else {
			formatted = this.mMetres + " " + this.mContext.getString(R.string.unit_meter);
		}
		return formatted;
	}
}
