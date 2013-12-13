package com.sprekigjovik.tracker;

import android.app.Activity;
import android.os.Bundle;
/**
 * Shows the settings screen.
 * @author John, Martin, Jehans
 *
 */
public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	  
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}
}
