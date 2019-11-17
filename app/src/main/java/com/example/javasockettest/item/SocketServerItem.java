package com.example.javasockettest.item;

public class SocketServerItem {

    private String socketId;
    private String socketContent;

    public SocketServerItem(String socketId, String socketContent){
        this.socketId = socketId;
        this.socketContent = socketContent;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public String getSocketContent() {
        return socketContent;
    }

    public void setSocketContent(String socketContent) {
        this.socketContent = socketContent;
    }

}
