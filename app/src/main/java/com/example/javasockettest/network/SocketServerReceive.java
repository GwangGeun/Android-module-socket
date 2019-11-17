package com.example.javasockettest.network;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.javasockettest.item.SocketServerItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**

 1. 목적

     : SocketServerActivity 에서 서버에 접속한 각 사용자들마다 갖고 있는 소켓 객체 등을 발급하는 목적

 2. 주의점

     (1) TODO : 각 셀들로 부터 최초에 각 셀들의 고유 ID 를 받아야 함 ( 현재는 서버에서 셀들이 연결되는 시간으로 고유 ID 를 발급하는 로직 )

     (2) TODO : 각 셀들을 저장하는 파일명도 고려

 */
public class SocketServerReceive extends Thread {

    private String TAG = "SocketServerReceive";
    // TODO : 추후 셀의 고유 id 를 받아서 지정해줘야 한다.
    // 임시 아이디
    private String id = "" + System.currentTimeMillis();
    private final int cellCount = 3;
    private final int cellRemove = 4;


    // file
    private String folderName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Fitogether";
    private String fileName;

    private Socket sock;
    private BufferedReader br;
    private HashMap<String, PrintWriter> hm;
    private ArrayList<SocketServerItem> dataList;
    private Handler handler;


    // 생성자 : 사용자가 갖고 있는 소켓
    public SocketServerReceive(Socket sock, HashMap<String, PrintWriter> hm, ArrayList<SocketServerItem> dataList, Handler handler) {

        this.sock = sock;
        this.hm = hm;
        this.dataList = dataList;
        this.handler = handler;


        try {
            // 사용자의 소켓으로 부터 메세지 발송을 위한 pipeline 을 얻어서 PrintWriter 에 지정
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            // 사용자의 소켓으로 부터 메세지 수신을 위한 pipeline 을얻어서 BufferedReader 에 지정
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // TODO : 추후 셀이 서버에 연결되자마자 고유의 아이디를 서버에 전송해줘야 한다. 이를 ID 로 사용
            // id = br.readLine();

            // 파일명 TODO: 추후 파일명을 어떻게 명명할지 논의 필
            fileName = id + ".txt";

            SocketServerItem socketItem = new SocketServerItem(id, "hello");
            dataList.add(socketItem);
            handler.sendEmptyMessage(cellCount);

            Log.e(TAG, "접속한 사용자의 아이디는 " + id + "입니다.");

            // 추후 각 사용자들에게 메세지를 보내기 위해 id 와 PrintWriter 객체를 HashMap 에 담기
            synchronized (hm) {
                hm.put(this.id, pw);
            }

        } catch (Exception ex) {
            Log.e(TAG, String.valueOf(ex));
        }

    }

    // run() : thread 에서 실행되야 하는 내용을 정의하는 곳
    public void run() {

        File dir = new File(folderName);
        //디렉토리 폴더가 없으면 생성함
        if (!dir.exists()) {
            dir.mkdir();
        }
        //파일 output stream 생성
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(folderName + "/" + fileName, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //파일쓰기
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

        try {

            String line;
            // 메세지를 계속 읽어 온다
            while ((line = br.readLine()) != null) {
                // 1. 목적 : 사용자가 보낸 메세지 quit 일 경우에는 채팅 방을 나간다.
                // 2. 내용 : 무한루프를 빠져나가서 finally 에 있는 문장을 수행한다.
                if (line.equals("quit")) {
                    break;
                }
                // 사용자가 입력한 메세지를 같은 방에 있는 모든 사용자에게 전달해준다.
                // TODO : 사용자가 입력한 정보를 저장
                else {
                    Log.e("사용자가 나에게 준 정보", id + " : " + line);

                    // 파일에 셀에서 보낸 내용 입력하기
                    writer.write(line);
                    writer.write("\n");
                    writer.flush();

                }
            }

        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        } finally {
            // 방을 나간 사용자는 HashMap 에서 제거
            synchronized (hm) {
                hm.remove(id);
                // 연결이 끊긴 소켓을 recyclerView 에서 삭제하기 위한 목적
                Message message = handler.obtainMessage();
                // handler 에게 송신자의 이름 태깅
                message.what = cellRemove;
                // handler 에게 메세지 내용 (연결이 끊긴 socket id) 태깅
                message.obj = id;
                // SocketClientActivity 에 있는 핸들러에 메세지 전송
                handler.sendMessage(message);

                Log.e(TAG, "연결이 끊긴 소켓 id : " + id);
            }
            try {
                // 파일 닫기
                writer.close();
                fos.close();
                // 더 이상 필요 없는 소켓 닫기
                if (sock != null)
                    sock.close();
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
            }

        }

    }

}
