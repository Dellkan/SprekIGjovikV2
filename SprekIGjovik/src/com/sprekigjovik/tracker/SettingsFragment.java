package com.sprekigjovik.tracker;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Functionality behind settings screen.
 * @author John, Martin, Jehans
 *
 */
public class SettingsFragment extends PreferenceFragment {
	/**
	 * Sets up settings menu with buttons and options<br>
	 * for the settings screen.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load the preferences
		this.addPreferencesFromResource(R.xml.preferences);
		
		// Create handler for fetch poles button
		Preference fetch_poles_button = (Preference) findPreference("prefMapLoadPoles");
		fetch_poles_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(isNetworkAvailable())
				{
					new DownloadXML(SettingsFragment.this.getActivity()).execute("http://dellkan.nodedevs.net/sprekigjovik.xml");
				}
				else 
				{
					Toast.makeText(SettingsFragment.this.getActivity().getApplicationContext(), 
							SettingsFragment.this.getActivity().getString(R.string.pref_no_internet), Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		// Update text on fetch poles button to show last download
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity());
		long timestamp = preferences.getLong("prefPolesLoaded", 0);
		String summary;
		if (timestamp == 0) {
			summary = this.getResources().getString(R.string.poles_never_updated);
		}
		
		else {
			Date date = new Date(timestamp);
			summary = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.SHORT).format(date);
		}
		
		fetch_poles_button.setSummary(fetch_poles_button.getSummary() + summary);
		
		// Create handler for delete routes button
		Preference delete_routes_button = (Preference) findPreference("prefRouteDeleteRoutes");
		delete_routes_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(SettingsFragment.this.getActivity())
					.setTitle(R.string.pref_delete_routes_confirm_title)
					.setMessage(R.string.pref_delete_routes_confirm_text)
					.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Delete stuff
							Route.deleteAll();
						}
					})
					.setNegativeButton(R.string.dialog_cancel, null)
					.show();
				return true;
			}
		});
	}

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) SettingsFragment.this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
