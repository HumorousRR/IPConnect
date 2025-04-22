package com.dss.ipcontrol.socket;

public interface ISocketSession {
    void onReply(byte[] data);

    void onStop();

    String getHost();
}
