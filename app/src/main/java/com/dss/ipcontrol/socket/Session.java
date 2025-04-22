package com.dss.ipcontrol.socket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Session implements Runnable, ISocketSession {
    private String TAG = Session.class.getSimpleName();

    private final Socket mSocket;
    private final ISocketServer mServerCallback;

    private DataInputStream mIn;
    private DataOutputStream mOut;

    private boolean mRunning;

    public Session(Socket socket, ISocketServer callback) {

        mSocket = socket;
        mServerCallback = callback;
        String ip = socket.getInetAddress().getHostAddress();
        int port = socket.getPort();

        TAG = TAG + "[" + ip + "(" + port + ")]";
        Log.d(TAG, "Connection created");

        try {
            mIn = new DataInputStream(socket.getInputStream());
            mOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRunning = true;
    }

    @Override
    public void run() {
        Log.d(TAG, "run");

        int recvLen = 0;
        byte[] recvBuf = new byte[1024];

        while (true) {
            try {
                recvLen = mIn.read(recvBuf);
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (recvLen < 0) {
                break;
            }

            try {
                byte[] recvData = new byte[recvLen];
                System.arraycopy(recvBuf, 0, recvData, 0, recvLen);
                Log.d(TAG, "recvCmd=" + TypeUtils.toHexString(recvData));
                mServerCallback.onReceive(this, recvData);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        closeConnect();
    }

    public void send(byte[] data) {

        if (mSocket.isClosed() || mOut == null)
            return;

        Log.d(TAG, "reply data:" + TypeUtils.toHexString(data));

        try {
            mOut.write(data, 0, data.length);
        } catch (IOException e) {
            Log.e(TAG, "reply ERROR:", e);
        }
    }

    private void closeConnect() {

        Log.d(TAG, "Disconnect!");

        try {

            if (mSocket != null) {
                mSocket.close();
            }

            if (mIn != null) {
                mIn.close();
            }
            if (mOut != null) {
                mOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mServerCallback.onDisconnected(this);
    }

    @Override
    public void onReply(byte[] data) {
        send(data);
    }

    @Override
    public void onStop() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getHost() {
        return mSocket.getInetAddress().getHostAddress();
    }
}

