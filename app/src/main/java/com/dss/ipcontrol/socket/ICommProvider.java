package com.dss.ipcontrol.socket;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

public abstract class ICommProvider {

    private final static String TAG = ICommProvider.class.getSimpleName();

    public String name = "";

    public abstract int init(Context context);

    public abstract int restart(Context context, Bundle b, IResultListener callback);

    public abstract int stop(Context context);

    public abstract boolean isConnected(Context context, String module);

    public abstract int send(Context context, String module, byte[] raw, Bundle meta);

    public abstract ArrayList<Profile> getProfiles();

    public class Profile {
        String module; // uart1, uart2, socket(port), etc...

        public Profile(String module) {
            this.module = module;
        }

        public String getModule() {
            return this.module;
        }
    }

    public void setEventListener(Context context, ICommProviderListener listener) {
    }

    public interface ICommProviderListener {

        void onRecvEvent(String event, String msg);

        void onRecvData(String module, byte[] raw, Bundle meta);
    }

    public interface IResultListener {
        void onResult(String module, String result, Bundle b);
    }

}
