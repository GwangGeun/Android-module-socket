package com.example.javasockettest.item;

public class SocketClientItem {
    
    String chatTime;
    String chatContent;

    public SocketClientItem(String chatTime, String chatContent){
        
        this.chatTime = chatTime;
        this.chatContent = chatContent;
        
    }

    public void setChatContent(String chatContent) {
        this.chatContent = chatContent;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }

    public String getChatContent() {
        return chatContent;
    }

    public String getChatTime() {
        return chatTime;
    }
}
