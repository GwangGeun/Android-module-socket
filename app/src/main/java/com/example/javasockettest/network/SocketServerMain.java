package com.example.javasockettest.network;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import com.example.javasockettest.item.SocketServerItem;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 1. 목적
 *
 * : SocketServerActivity 에서 소켓 서버를 키고 끄는데 사용 ( 즉, 본 스레드를 호출하는 곳은 SocketServerActivity.java 임 )
 *
 * 2. 로직
 *
 * (1) 서버 킴
 *
 * : IP 와 port 를 지정함으로써 서버를 킨다. ( 추후 고정 IP 를 사용할 수 없을 수도 있기 때문에 하단에 본 기기에 할당되는 유동 IP 를 알아내는 로직 첨부 )
 *
 * (2) 서버 끔
 *
 * : 서버는 종료되기 직전에 셀들에게 특정 메세지(serverMessage)를 송신함으로써 본인이 종료됨을 알려줌.
 *
 *
 * 3. 주의점
 *
 * (1) call by reference 에 대한 개념 숙지
 *
 * (2) 추후 유동 아이피를 사용해야 하는 경우 handler 의 메세지를 통해 안드로이드에 할당 된 IP 를 SocketServerActivity 에 전달
 *
 */
public class SocketServerMain extends Thread {

    private Activity activity;
    private SocketSendMsg socketSendMsg;

    private ServerSocket server;
    private HashMap<String, PrintWriter> hm;
    private Handler handler;
    private WifiManager wifiMgr;
    private ArrayList<SocketServerItem> dataList;

    // 통신 ip, port
    private String bindIp = "192.168.0.2";
    private String TAG = "SocketServerMain";
    private final int port = 8888;
    private String purpose;
    private String serverMessage = "stop";

    // handler 에 보낼 메세지
    private final int connectionSuccess = 1;
    private final int connectionInit = 2;

    // 생성자 : 서버 소켓을 닫을 때 사용
    public SocketServerMain(ServerSocket server, HashMap<String, PrintWriter> hm, Handler handler, String purpose) {

        this.server = server;
        this.hm = hm;
        this.handler = handler;
        this.purpose = purpose;
    }

    // 생성자 : 서버 소켓을 열 때 사용
    public SocketServerMain(Activity activity, ServerSocket server, HashMap<String, PrintWriter> hm, Handler handler,
                            ArrayList<SocketServerItem> dataList, WifiManager wifiMgr, String purpose) {

        this.activity = activity;
        this.server = server;
        this.hm = hm;
        this.handler = handler;
        this.dataList = dataList;
        this.wifiMgr = wifiMgr;
        this.purpose = purpose;
    }


    @Override
    public void run() {
        super.run();
        // 1. 사용자가 서버를 시작하려고 하는 경우
        if (purpose.equals("start")) {

            try {
                // TODO : TEST 목적의 IP 받아오는 코드임. 추후 삭제
                bindIp = getLocalIpAddress();
                // ip 와 port 를 server 에 지정
                server.bind(new InetSocketAddress(bindIp, port));
                Log.e(TAG, bindIp);

                // 서버가 연결되었을 경우, 사용자가 서버가 연결되었음을 인지하기 위해 UI 상으로 표시해주기
                Message message = handler.obtainMessage();
                // handler 에게 송신자의 이름 태깅
                message.what = connectionSuccess;
                // handler 에게 메세지 내용 태깅 ( bind 된 HostIp )
                message.obj = bindIp;
                // SocketServerActivity 에 있는 핸들러에 메세지 전송
                handler.sendMessage(message);

                // 서버를 멈추기 전까지는 지속적으로 소켓 열어 놓기
                while (true) {
                    Log.e(TAG, "클라이언트 대기 중");
                    Socket sock = server.accept();
                    Log.e(TAG, "클라이언트 연결 ");
                    SocketServerReceive thread = new SocketServerReceive(sock, hm, dataList, handler);
                    thread.start();
                }

            } catch (Exception e) {

                Log.e(TAG, String.valueOf(e));
            }

        }
        // 2. 사용자가 서버를 멈추려고 할 경우
        else if (purpose.equals("end")) {
            try {
                // 서버가 멈추기 전에 셀들에게 서버가 멈춘다는 신호를 보낸다.
                if (hm.size() > 0) {
                    // TODO : 서버가 멈출 때, 셀들에게 특정 메세지를 보낼 필요가 없다면. 하단의 synchronized() 는 삭제해도 무관하다.
                    synchronized (hm) {

                        Collection<PrintWriter> collection = hm.values();
                        Iterator<PrintWriter> iterator = collection.iterator();

                        while (iterator.hasNext()) {
                            PrintWriter pw = (PrintWriter) iterator.next();
                            pw.println(serverMessage);
                            pw.flush();
                        }
                    }

                    // client 정보들이 저장되어 있는 HashMap 초기화 시켜주기
                    hm.clear();
                }

                // recyclerView 초기화
                // 연결 된 셀의 갯수 초기화 : 이 부분은 핸들러에서 자체적으로 셀의 갯수를 체크하는 로직을 한번 더 호출함으로써 갱신한다.
                handler.sendEmptyMessage(connectionInit);

                if (server != null) {
                    Log.e(TAG, "socket close");
                    server.close();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    // 메세지 발신
    // id 가 "0" 인 경우는 셀 전체에게 메세지를 보내는 경우이다
    public void sednMsg(String msg, String id, EditText editText){
        // 메세지 발신 전용 파이프 라인 연결 ( asyncTask 는 재활용이 불가능 하다, 즉 매번 객체를 생성해줘야 한다 )
        socketSendMsg = new SocketSendMsg(activity,hm,editText,TAG, id);
        socketSendMsg.execute(msg);
    }

    // 현재 와이파이에 연결되어 있는 ip 가져오기
    private String getLocalIpAddress() {
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        @SuppressLint("DefaultLocale")
        String ipAddress = String.format("%d.%d.%d.%d"
                , (ip & 0xff)
                , (ip >> 8 & 0xff)
                , (ip >> 16 & 0xff)
                , (ip >> 24 & 0xff));
        return ipAddress;
    }

}
