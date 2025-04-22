package com.dss.ipcontrol.socket;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer implements ISocketServer {
    final public static String TAG = SocketServer.class.getSimpleName();
    public static final String EVENT_SERVER_START = "run_server_start";
    public static final String EVENT_SERVER_SUCCESS = "run_server_success";
    public static final String EVENT_SERVER_FAIL = "run_server_fail";

    private Handler mHandler;

    final private static int MAX_SESSION_NUM = 5;

    private ServerSocket mServerSocket = null;
    private static final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<>();
    private Context mContext = null;
    private SocketServer mSocketServer = null;
    private static Integer mPort = 5000;
    private static Boolean mIsOnline = false;

    // updated flow
    private ICommProvider.ICommProviderListener mCallback;
    private ICommProvider.IResultListener mResultListener;
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public SocketServer(Context context, ICommProvider.ICommProviderListener callback) {
        mContext = context;
        mSocketServer = this;
        mCallback = callback;
        registerHandler();
    }

    public void setCallback(ICommProvider.ICommProviderListener listener) {
        this.mCallback = listener;
    }

    private void registerHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        mHandler = new Handler(handlerThread.getLooper());
    }

    public int restart(int port, ICommProvider.IResultListener listener) {

        Log.d(TAG, "restart->port=" + port);

        stop();
        mHandler.removeCallbacks(null);

        mResultListener = listener;

        if (port == -1)
            mHandler.post(new RunServer(5000));
        else
            mHandler.post(new RunServer(port));
        return 1;
    }

    private class RunServer implements Runnable {

        private final int port;

        RunServer(int port) {
            this.port = port;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void run() {
            if (port == -1) {
                Log.e(TAG, "RunServer port ERROR:" + port);
                updateServerInfo(false, port);
            } else {

                Log.d(TAG, "RunServer: run");
                TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId());
                boolean status = openSocket(port);
                updateServerInfo(status, port);

                if (mResultListener != null) {
                    mResultListener.onResult("socket", "connected", null);
                }

                while (mServerSocket != null) {
                    try {
                        if (mSessions.size() < MAX_SESSION_NUM) {
                            Log.d(TAG, "RunServer: waiting ...");

                            Socket clientSocket = mServerSocket.accept();

                            if (clientSocket != null && clientSocket.getInetAddress() != null) {
                                Session session = new Session(clientSocket, mSocketServer);

                                synchronized (mSessions) {
                                    mSessions.put(clientSocket.getInetAddress().getHostAddress(), session);
                                }

                                mExecutor.execute(session);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "RunServer: ERROR:", e);
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean openSocket(int port) {
        closeSocket();
        if (mCallback != null) {
            mCallback.onRecvEvent(EVENT_SERVER_START, "openSocket");
        }
        try {
            mServerSocket = new ServerSocket(port);

            if (!mServerSocket.isClosed())
                return true;
            else
                Log.d(TAG, "openSocket->failed!");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void closeSocket() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerSocket = null;
    }

    @TargetApi(Build.VERSION_CODES.N)
    synchronized public boolean stop() {
        // stop server
        closeSocket();
        mHandler.removeCallbacks(null);

        // clear all sessions
        mSessions.forEach((k, v) -> {
            v.onStop();
        });
        mSessions.clear();

        updateServerInfo(false, mPort);

        if (mResultListener != null) {
            mResultListener.onResult("socket", "disconnected", null);
        }

        return true;
    }

    private void updateServerInfo(boolean isOnline, int port) {
        synchronized (mIsOnline) {
            if (isOnline != mIsOnline) {
                mIsOnline = isOnline;
                if (mCallback != null) {
                    if (mIsOnline) {
                        mCallback.onRecvEvent(EVENT_SERVER_SUCCESS, "IsOnline");
                    } else {
                        mCallback.onRecvEvent(EVENT_SERVER_FAIL, "Port occupy");
                    }
                }
            }
        }

        synchronized (mPort) {
            if (port != mPort) {
                mPort = port;
            }
        }
        Log.d(TAG, "updateServerInfo: isOnline=" + isOnline + ", port=" + port);
    }

    public boolean isOnline(int nPort) {
        Log.d(TAG, "isOnline: mPort=" + mPort + ", nPort=" + nPort);
        if (nPort != mPort) {
            return false;
        } else {
            return mIsOnline;
        }
    }

    @Override
    public void onReceive(ISocketSession callback, byte[] data) {
        Bundle meta = new Bundle();
        meta.putString("host", callback.getHost());

        if (mCallback != null) {
            mCallback.onRecvData("socket", data, meta);
        } else {
            Log.w(TAG, "Callback is not exist!");
        }
    }

    @Override
    public void onDisconnected(Session session) {
        synchronized (mSessions) {
            mSessions.remove(session);
        }
    }

    public int send(byte[] raw, Bundle meta) {

        if (meta != null && meta.containsKey("host")) {
            String host = meta.getString("host");

            Session s = mSessions.get(host);

            if (s != null) {
                s.send(raw);
            }

            return 0;
        }

        return -1;
    }
}
