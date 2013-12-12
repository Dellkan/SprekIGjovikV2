package com.sprekigjovik.tracker;

import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadXML extends AsyncTask<String, Integer, Void> {
	Context mContext;
        
	public DownloadXML(Context applicationContext) {
		this.mContext = applicationContext;
	}
        
    /**
     * Processes an xml file from the internet or a stored file and generates a list of pole objects from it
     * @return 
     */
    @Override
    protected Void doInBackground(String... urls) {
    	// Check if user has internet access
    	ConnectivityManager cm = (ConnectivityManager) this.mContext.
    			getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
    	
    	// Give error if not
    	if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
    		// Gief error to user
    		Log.d("sprekigjovik", "No internet for you");
    		Toast.makeText(this.mContext.getApplicationContext(), "No internet access!", Toast.LENGTH_SHORT).show();
    	}
                
    	else {
    		DefaultHttpClient client = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet(urls[0]);                        
    		try {
    			InputStream stream = client.execute(httpGet).getEntity().getContent();

    	    	FileOutputStream output = this.mContext.getApplicationContext().openFileOutput("poles.xml", Context.MODE_PRIVATE);
    	    	byte[] buffer = new byte[4096];
    	    	int n;
    	                
    	    	while((n = stream.read(buffer)) > 0) {
    	    		output.write(buffer, 0, n);
    	    	}
    	    	output.close();
    	    	stream.close();
    		}
    		
    		catch (Exception e) {
    			e.printStackTrace();
    		}
        }
		return null;
    }
    
    protected void onPostExecute(Void _void) {
        Pole.createFromXML(this.mContext);
        Toast.makeText(this.mContext.getApplicationContext(), "Poles updated!", Toast.LENGTH_SHORT).show();
        
        Editor pref = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit(); 
        pref.putLong("prefPolesLoaded", System.currentTimeMillis());
        pref.commit();
    }
}