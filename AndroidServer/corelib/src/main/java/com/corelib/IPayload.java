package com.corelib;

import android.app.Activity;

/**
 * Payload interface
 * Created by hkff on 4/27/15.
 */
public interface IPayload {
    Packet handle(Packet msg);
    void setApp(Activity app);
    String man();
}
