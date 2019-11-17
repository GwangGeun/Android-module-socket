package com.example.javasockettest.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.net.Socket;


public class SocketClientReceive extends Thread{

    private final String TAG = "SocketClientReceive";
    private Socket sock;
    private BufferedReader br;

    private String dataFromServer;
    private Handler handler;

    private final int addChatContent = 1;

    public SocketClientReceive(Socket sock, BufferedReader br, Handler handler) {
        this.sock = sock;
        this.br = br;
        this.handler = handler;
    }

    public void run() {

        try {
            // 서버에서 받은 데이터를 handler 에 전달해주기
            while ((dataFromServer = br.readLine()) != null) {
                Log.e(TAG,"서버에서 받은 데이터 : "+dataFromServer);
                Message message = handler.obtainMessage();
                // handler 에게 송신자의 이름 태깅
                message.what = addChatContent;
                // handler 에게 메세지 내용 태깅
                message.obj = dataFromServer;
                // SocketClientActivity 에 있는 핸들러에 메세지 전송
                handler.sendMessage(message);

            }

        }
        catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }

    }
}
