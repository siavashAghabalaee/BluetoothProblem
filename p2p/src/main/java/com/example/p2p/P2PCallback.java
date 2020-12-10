package com.example.p2p;

public interface P2PCallback {
    void receivedMessage(String message);
    void connected();
    void disconnected();
}
