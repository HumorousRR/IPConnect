package com.dss.ipcontrol.socket;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class SocketProvider extends ICommProvider {

    private static final String TAG = SocketProvider.class.getSimpleName();
    private final ArrayList<Profile> mSupportedProfiles;
    private ICommProviderListener mCallback = null;
    private SocketServer mSocketServer = null;

    private int DEF_PORT = 5000;
    private int mPort = -1;

    private static final class MCommonImplHolder {
        private static final SocketProvider mSocketProvider = new SocketProvider();
    }

    public static SocketProvider getInstance() {
        return MCommonImplHolder.mSocketProvider;
    }

    private SocketProvider() {
        this.name = TAG;
        mSupportedProfiles = new ArrayList<>();
        Profile socket = new Profile("socket");
        mSupportedProfiles.add(socket);
    }

    @Override
    public int init(Context context) {
        return 0;
    }

    @Override
    public void setEventListener(Context context, ICommProviderListener listener) {
        mCallback = listener;
        if (mSocketServer != null) {
            mSocketServer.setCallback(mCallback);
        }
    }

    @Override
    public int restart(Context context, Bundle b, IResultListener listener) {

        if (mSocketServer == null)
            mSocketServer = new SocketServer(context, mCallback);

        if (b != null && b.containsKey("port")) {
            mPort = b.getInt("port");
            mSocketServer.restart(mPort, listener);
        } else {
            mPort = DEF_PORT;
            mSocketServer.restart(DEF_PORT, listener);
        }
        Log.d(TAG, "restart: mPort:" + mPort);

        return 0;
    }

    @Override
    public int stop(Context context) {

        if (mSocketServer != null)
            mSocketServer.stop();

        return 0;
    }

    @Override
    public boolean isConnected(Context context, String module) {
        return mSocketServer.isOnline(mPort);
    }

    @Override
    public ArrayList<Profile> getProfiles() {
        return mSupportedProfiles;
    }

    @Override
    public int send(Context context, String module, byte[] raw, Bundle meta) {

        if (mSocketServer != null)
            mSocketServer.send(raw, meta);

        return 0;
    }
}
