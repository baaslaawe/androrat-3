package com.core.payloads;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.corelib.IPayload;
import com.corelib.Packet;

/**
 * Geolocation class
 * Created by hkff on 4/22/15.
 */
public class Geolocation implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        LOCATE,
    }

    @Override
    public String man() {
        return "";
    }

    /***
     * Handle msg
     * @param msg
     * @return
     */
    @Override
    public Packet handle(Packet msg) {
        // The result to return
        Packet result = new Packet("NO_COMMAND_PROVIDED", Packet.Type.STRING);

        // Get the command (and convert it to uppercase)
        String cmd0 = msg.getMsg().toUpperCase();

        // Check if the command exists in our Commands enumeration
        Commands cmd;
        try{
            cmd = Commands.valueOf(cmd0);
        } catch (IllegalArgumentException e){
            // If there is no command, we return an error message
            result.setType(Packet.Type.NONE);
            return result;
        }

        // We handle the command
        switch (cmd){
            // Get location
            // Protocol : LOCATE
            case LOCATE:
                String pos = this.getLocation();
                String[] tmp = pos.split("-");
                String lat = tmp[0].replace("Lat : ", "");
                String lon = tmp[2].replace("Lon : ", "");
                result.setMsg(pos);
                result.addExtra(lat);
                result.addExtra(lon);
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//

    // Attributes
    Activity app;
    private static LocationListener locationListener;
    private static LocationManager locationManager;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    private static Location lastKnownLocation;

    public Geolocation(){}

    /**
     * Constructor
     * @param app
     */
    public Geolocation(Activity app){
        this.app = app;
        this.enableTracking();
    }


    /**
     * Get GPS position
     * @return
     */
    public String getLocation(){
        String result = "Unknown location";
        if(lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //  if(lastKnownLocation == null)
            //  lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(lastKnownLocation != null) {
            result = "Lat : " + lastKnownLocation.getLatitude() + " - Lon : " +  lastKnownLocation.getLongitude();
        }
        return result;
    }


    /**
     * Enable location tracking
     */
    public void enableTracking() {
        System.out.println("Enable tracking ...");

        // Acquire a reference to the system Location Manager
        if(locationManager == null)
            locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        if(locationListener == null) {
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    Geolocation.lastKnownLocation = location;
                    System.out.println("Location changed : " + location.toString());
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    System.out.println("Status changed : " + provider + "  " + status);
                }

                public void onProviderEnabled(String provider) {
                    System.out.println("Provider on : " + provider);
                }

                public void onProviderDisabled(String provider) {
                    System.out.println("Provider off : " + provider);
                }
            };
        }

        // Register the listener with the Location Manager to receive location updates
        // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }
}
