package com.example.sprekigjovik;

import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
    	if(!hasInternet()) {
    		// Gief error to user
    		Log.d("sprekigjovik", "No internet for you");
    	}
                
    	else {
    		DefaultHttpClient client = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet(urls[0]);                        
    		try {
    			InputStream stream = client.execute(httpGet).getEntity().getContent();

    	    	FileOutputStream output = this.mContext.getApplicationContext().openFileOutput(
    	    		this.mContext.getResources().getString(R.string.file_path_poles), Context.MODE_PRIVATE
    	    	);
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
        
    /**
     * Checks if the user has internet access
     * @return True of False if the user has access or not, respectively
     */
    private boolean hasInternet() {
    	ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
    	return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }
    
    protected void onPostExecute(Void _void) {
        Pole.createFromXML(this.mContext);
        Toast.makeText(this.mContext.getApplicationContext(), "Poles updated!", Toast.LENGTH_SHORT).show();
    }
}