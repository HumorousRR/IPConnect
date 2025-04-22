package com.dss.ipcontrol.socket;

public class SocketData {

    ISocketSession callback;
    byte[] data;

    SocketData(ISocketSession callback, byte[] data) {
        this.callback = callback;
        this.data = data;
    }

}
