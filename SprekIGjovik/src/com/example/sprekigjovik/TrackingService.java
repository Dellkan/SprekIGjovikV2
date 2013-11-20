package com.example.sprekigjovik;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import android.annotation.SuppressLint;
import android.app.Notification;
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
	public static final int MSG_ROUTE_UPDATE = 6;
	
	private List<TrackingService.Client> mClients = new ArrayList<TrackingService.Client>();
	private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	private boolean mActiveTracking;
	private LocationTracker curLoc;
	private Route mRoute = new Route();
	
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
        	for (int i = 0; i < poles.size(); i++) {
        		poles.get(i).calcDistance(loc);
        	}
        	for (int client = 0; client < this.mClients.size(); client++) {
        		List<PoleMarker> markers = this.mClients.get(client).getPoleMarkers();
        		for (int marker = 0; marker < markers.size(); marker++) {
        			markers.get(marker).getMarker().setSnippet(Float.toString( markers.get(marker).getPole().getDistance() ));
        		}
        	}
        }
        
        // Let connected clients know that location is updated
        Message locUpdateMsg = Message.obtain(null, TrackingService.MSG_LOCATION_UPDATE);
        locUpdateMsg.obj = loc;
        this.broadcastToClients(locUpdateMsg);
        
        // If we are actively tracking route, add latest point to list.
        if (this.mActiveTracking) {
        	this.mRoute.addPoint(new LatLng(loc.getLatitude(), loc.getLongitude()));
            Message updateRouteMsg = Message.obtain(null, TrackingService.MSG_ROUTE_UPDATE);
            updateRouteMsg.obj = this.mRoute;
            this.broadcastToClients(updateRouteMsg);
        }
	}
	
	private void broadcastToClients(Message msg) {
		for (int i = this.mClients.size()-1; i >= 0; i--) {
            try {
            	this.mClients.get(i).getMessenger().send(msg);
            } catch (RemoteException e) {
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
        @SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TrackingService.MSG_REGISTER_CLIENT:
                	Client client = this.mService.new Client(msg.replyTo, (List<PoleMarker>)msg.obj);
                    // Ooo, new client. Let the client know whether or not we're tracking
                    Message trackingStatusMsg = Message.obtain(null, 
                    	this.mService.mActiveTracking ? TrackingService.MSG_START_TRACKING : TrackingService.MSG_STOP_TRACKING
                    );
                    // Should send along the route object too.
                    trackingStatusMsg.obj = this.mService.mRoute;
                    try {
                    	client.getMessenger().send(trackingStatusMsg);
                    	this.mService.mClients.add(client);
                    } catch (RemoteException e) {
                    	// Client isn't added to list unless above message went well, so no need to do anything further
                    }
                    break;
                case TrackingService.MSG_UNREGISTER_CLIENT:
                    this.mService.mClients.remove(msg.replyTo);
                    break;
                case TrackingService.MSG_START_TRACKING:
                	// Mark as actively tracking
                	this.mService.mActiveTracking = true;
            		// Notification
            		Notification notification = new Notification.Builder(this.mService.getApplicationContext())
            		.setContentTitle(this.mService.getResources().getString(R.string.notification_title))
            		.setContentText(this.mService.getResources().getString(R.string.notification_subtitle))
            		.setSmallIcon(R.drawable.ic_launcher)
            		.build();
            		
            		// Put service into foreground
            		this.mService.startForeground(8123, notification);
            		
            		// Let the clients know that we've started tracking
                    Message trackingOnMsg = Message.obtain(null, TrackingService.MSG_START_TRACKING);
                    // Should send along the route object too.
                    trackingOnMsg.obj = this.mService.mRoute;
                    this.mService.broadcastToClients(trackingOnMsg);
                    break;
                case TrackingService.MSG_STOP_TRACKING:
                	// Mark as no longer tracking
                	this.mService.mActiveTracking = false;
                	// Remove from foreground
                	this.mService.stopForeground(true);
                	
            		// Let the clients know that we've stopped tracking
                    Message trackingOffMsg = Message.obtain(null, TrackingService.MSG_START_TRACKING);
                    // Should send along the route object too.
                    trackingOffMsg.obj = this.mService.mRoute;
                    this.mService.broadcastToClients(trackingOffMsg);
                	break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    public class Client {
    	private Messenger mMessenger;
    	private List<PoleMarker> mPoleMarkers;
    	public Client(Messenger pMessenger, List<PoleMarker> pPoleMarkers) {
    		this.mMessenger = pMessenger;
    		this.mPoleMarkers = pPoleMarkers;
    	}
    	
    	public Messenger getMessenger() {
    		return this.mMessenger;
    	}
    	
    	public List<PoleMarker> getPoleMarkers() {
    		return this.mPoleMarkers;
    	}
    }
}
