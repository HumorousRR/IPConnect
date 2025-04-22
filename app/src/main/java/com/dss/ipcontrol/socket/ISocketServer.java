package com.dss.ipcontrol.socket;

public interface ISocketServer {

    void onReceive(ISocketSession callback, byte[] data);

    void onDisconnected(Session session);


}
