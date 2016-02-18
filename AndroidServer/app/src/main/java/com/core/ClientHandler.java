package com.core;

import android.util.Base64;
import com.corelib.Crypto;
import com.corelib.IPayload;
import com.corelib.Packet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.crypto.spec.SecretKeySpec;

/**
 * Client handler thread
 * Created by hkff on 4/28/15.
 */
public class ClientHandler extends Thread {

    // Attributes
    Socket socket;

    /**
     * Constructor
     * @param clientSocket
     */
    public ClientHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }


    /**
     * The run method of the thread
     */
    @Override
    public void run() {
        boolean exit = false;

        try{
            // Generate a encryption key for the communication with the client
            SecretKeySpec sks = Crypto.genAESkey("ffrozijgrzoig");
            System.out.println("Generated encryption key : " + Base64.encodeToString(sks.getEncoded(), Base64.DEFAULT));

            // We loop waiting for client connection until the client exit
            while (!Thread.currentThread().isInterrupted()) {
                // Initializing buffers to communicate with the client
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                // Send the key to the client
                System.out.println("Sending the encryption key ...");
                out.writeObject(sks);
                out.flush();

                // We read the message of the client
                while(!exit){
                    // We get the message
                    //Packet msg = (Packet) input.readObject();
                    Crypto.EncryptedPacket encryptedResult = (Crypto.EncryptedPacket) input.readObject();
                    Packet msg = Crypto.decrypt(encryptedResult, sks);

                    // We handle the message
                    Packet result = this.handle_client_msg(msg);

                    // We send the result to the client
                    //out.writeObject(result);
                    out.writeObject(Crypto.encrypt(result, sks));
                    out.flush();

                    // If it's an exit message, we close the connection with the client
                    if(msg.getMsg().toUpperCase().equals("EXIT"))
                        exit = true;
                }

                // We close the connection with the client
                this.exit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Close the server connection
     */
    public void exit() {
        try {
            this.interrupt();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method handle the msg from the client
     * it delegate the handling to each loaded payload
     * @param msg : the client's message
     */
    public Packet handle_client_msg(Packet msg) {
        // The result to return
        Packet result = null;

        // We handle the command
        for(IPayload payload : ServerThread.getPayloads()){
            result = payload.handle(msg);
            if (!result.getType().equals(Packet.Type.NONE))
                break;
        }

        // We return the result
        return result;
    }
}
