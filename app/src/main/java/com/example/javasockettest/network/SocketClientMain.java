package com.example.javasockettest.network;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClientMain extends Thread {

    private String TAG = "SocketClientMain";
    private Activity activity;
    private EditText editText;

    private Socket sock;
    private Handler handler;
    private BufferedReader br = null;
    private PrintWriter pw = null;

    private String purpose;
    private String hostIp;
    private int port;

    private final int connectionSuccess = 2;
    private final int connectionInit = 3;

    SocketSendMsg socketSendMsg;

    // 생성자 : 클라이언트 소켓을 닫을 때 사용
    public SocketClientMain( Handler handler, String purpose) {

        this.handler = handler;
        this.purpose = purpose;

    }

    // 생성자 : 클라이언트 소켓을 열때 사용
   public SocketClientMain(Activity activity, EditText editText, Handler handler, String hostIp, int port, String purpose) {

        this.activity = activity;
        this.editText = editText;
        this.handler = handler;
        this.hostIp = hostIp;
        this.port = port;
        this.purpose = purpose;

    }

    // main thread 내용
    @Override
    public void run() {
        super.run();
        // 서버에 소켓 연결하기
        if (purpose.equals("start")) {
            try {
                sock = new Socket(hostIp, port);
                pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                // UI 상에 소켓이 연결되었다는 것을 표시해주기
                handler.sendEmptyMessage(connectionSuccess);

                // 서버로 부터 메세지를 받아오는 역할을 하는 Thread start
                SocketClientReceive socketClientReceive = new SocketClientReceive(sock, br, handler);
                socketClientReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, String.valueOf(e));
            }
        }
        // 서버에서 소켓 연결 끊기
        else if (purpose.equals("end")) {

            // UI 상에 소켓이 해제되었다는 것을 표시해주기
            handler.sendEmptyMessage(connectionInit);
            // socket 을 비롯한 객체들 닫기
            try {
                if (br != null) {
                    br.close();
                }
                if(pw != null){
                    pw.close();
                }
                if (sock != null) {
                    sock.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, String.valueOf(e));
            }
        }

    }
    // 메세지 발신
    public void sednMsg(String msg){
        // 메세지 발신 전용 파이프 라인 연결 ( asyncTask 는 재활용이 불가능 하다, 즉 매번 객체를 생성해줘야 한다 )
        socketSendMsg = new SocketSendMsg(activity,pw, editText, TAG);
        socketSendMsg.execute(msg);

    }



}
