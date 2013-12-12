package com.sprekigjovik.tracker;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class TrackingService extends Service {
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_START_TRACKING = 3;
	public static final int MSG_STOP_TRACKING = 4;
	public static final int MSG_LOCATION_UPDATE = 5;
	public static final int MSG_GPSSTATUS_CHANGE = 6;
	
	private List<Messenger> mClients = new ArrayList<Messenger>();
	private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	private boolean mActiveTracking;
	private LocationTracker curLoc;
	private Route mRoute;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg) {
		return this.mMessenger.getBinder();
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		// Start actual tracking
		if (this.curLoc == null) {
			this.curLoc = new LocationTracker(this);
		}
	}
	
    @Override
    public void onRebind(Intent intent) {}
	
    @Override
    public boolean onUnbind(Intent intent) { return true; }
    
    @Override
    public void onDestroy() {
    	if (this.curLoc != null) {
    		this.curLoc.close();
    		this.curLoc = null;
    	}
    }
	
	public void runLocationUpdate(Location loc) {
        // Update poles, but only if there are alive clients 
        if (this.mClients.size() > 0) {
        	List<Pole> poles = Pole.getPoles(this.getApplicationContext());
        	for (int pole = 0; pole < poles.size(); pole++) {
        		poles.get(pole).calcDistance(loc);
        	}
        	
        	// Sort the poles according to their new distances
        	// Note, this have to be recoded if service is split into own process
        	Pole.sortPoles();
        }
        
        // Let connected clients know that location is updated
        Message locUpdateMsg = Message.obtain(null, TrackingService.MSG_LOCATION_UPDATE);
        locUpdateMsg.arg1 = (int) Math.floor(loc.getAccuracy());
        
        // If we are actively tracking route, and location is accurate enough, add latest point to list.
        if (this.mActiveTracking && loc.getAccuracy() <= 15) {
        	this.mRoute.addPoint(loc);
            locUpdateMsg.obj = this.mRoute;
        }
        
        // Send the message
        this.broadcastToClients(locUpdateMsg);
	}
	
	public void runGPSStatusUpdate(boolean toggle) {
		Message msg = Message.obtain(null, TrackingService.MSG_GPSSTATUS_CHANGE);
		msg.obj = toggle;
		this.broadcastToClients(msg);
	}
	
	private void broadcastToClients(Message msg) {
		for (int i = this.mClients.size()-1; i >= 0; i--) {
            try {
            	this.mClients.get(i).send(msg);
            } catch (RemoteException e) { // It's not throwing RemoteExceptions like it should. Revise!
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
            	this.mClients.remove(i);
            }
        }
	}
	
    @SuppressLint("NewApi")
	static class IncomingHandler extends Handler {
    	private TrackingService mService;
    	public IncomingHandler(TrackingService service) {
    		super();
    		this.mService = service;
    	}
        
		@Override
        public void handleMessage(Message input) {
            switch (input.what) {
                case TrackingService.MSG_REGISTER_CLIENT:
                	{
	                	Messenger client = input.replyTo;
	                    // Ooo, new client. Let the client know whether or not we're tracking
	                    Message output = Message.obtain(null, 
	                    	this.mService.mActiveTracking ? TrackingService.MSG_START_TRACKING : TrackingService.MSG_STOP_TRACKING
	                    );
	                    // Should send along the route object too.
	                    output.obj = this.mService.mRoute;
	                    try {
	                    	client.send(output);
	                    	this.mService.mClients.add(client);
	                    } catch (RemoteException e) {
	                    	// Client isn't added to list unless above message went well, so no need to do anything further
	                    }
                	}
                    break;
                case TrackingService.MSG_UNREGISTER_CLIENT:
                    // Find client
                	{
	                	for (Messenger client : this.mService.mClients) {
	                		if (client.equals(input.replyTo)) {
	                			// Terminate client
	                			this.mService.mClients.remove(client);
	                			break;
	                		}
	                	}
                	}
                    break;
                case TrackingService.MSG_START_TRACKING:
                	{
	                	// Mark as actively tracking
	                	this.mService.mActiveTracking = true;
	                	this.mService.mRoute = new Route();
	                	
	            		// Let the clients know that we've started tracking
	                    Message output = Message.obtain(null, TrackingService.MSG_START_TRACKING);
	                    // Should send along the route object too.
	                    output.obj = this.mService.mRoute;
	                    this.mService.broadcastToClients(output);
	                	
	            		// Notification
	                    Intent intentForeground = new Intent(this.mService, MapActivity.class)
	                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                    PendingIntent pendingIntent = PendingIntent.getActivity(
	                    	this.mService.getApplicationContext(), 0, intentForeground, 0
	                    );
	                    
	            		Notification notification = new Notification.Builder(this.mService.getApplicationContext())
	            		.setContentTitle(this.mService.getResources().getString(R.string.notification_title))
	            		.setContentText(this.mService.getResources().getString(R.string.notification_subtitle))
	            		.setSmallIcon(R.drawable.ic_launcher)
	            		.setContentIntent(pendingIntent)
	            		.setTicker(this.mService.getResources().getString(R.string.notification_ticker))
	            		.build();
	            		
	            		// Put service into foreground
	            		this.mService.startForeground(8123, notification);
                	}
                    break;
                case TrackingService.MSG_STOP_TRACKING:
                	{
	                	// Mark as no longer tracking
	                	this.mService.mActiveTracking = false;
	                	this.mService.mRoute.stop();
	                	this.mService.mRoute = null;
	                	
	            		// Let the clients know that we've stopped tracking
	                    Message output = Message.obtain(null, TrackingService.MSG_STOP_TRACKING);
	                    this.mService.broadcastToClients(output);
	                    
	                	// Remove from foreground
	                	this.mService.stopForeground(true);
                	}
                	break;
                default:
                    super.handleMessage(input);
            }
        }
    }
}
