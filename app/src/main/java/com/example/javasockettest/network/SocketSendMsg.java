package com.example.javasockettest.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.EditText;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class SocketSendMsg extends AsyncTask<String, Void, Void> {

    private Activity activity;
    private PrintWriter pw;
    private EditText editText;
    private HashMap<String, PrintWriter> hm;
    // 발신자가 "서버" 인지 "클라이언트" 인지 구분하기 위한 목적
    private String sender;
    // 발신자가 "서버" 인 경우,
    // 전체 셀들에게 일괄 메세지 전송 목적인지
    // 각 셀들 하나에 대한 메세지 전송이 목적인지 구분하기 위함
    // 각 셀들에게 보내는 경우, 각 셀들의 고유 id 를 알아야 한다.
    // 전체 발송의 경우 --> id 는 "0" 으로 한
    String id;


    // 발신자가 "클라이언트" 인 경우 ( 즉, 클라이언트 --> 서버 )
    public SocketSendMsg(Activity activity, PrintWriter pw, EditText editText, String sender){
        this.activity = activity;
        this.pw = pw;
        this.editText = editText;
        this.sender = sender;
    }

    // 발신자가 "서버" 인 경우 ( 즉, 서버 --> 클라이언트 )
    public SocketSendMsg(Activity activity, HashMap<String, PrintWriter> hm, EditText editText, String sender, String id){
        this.activity = activity;
        this.hm = hm;
        this.editText = editText;
        this.sender = sender;
        this.id = id;
    }

    // 메인 로직이 실행 되는 곳 ( 메세지 발송 )
    @Override
    protected Void doInBackground(String... content) {

        try {
            String msg = new String(content[0].getBytes(StandardCharsets.UTF_8));

            switch (sender){

                case "SocketServerMain" :

                    // 셀들 전체에게 메세지를 발송하려는 경우
                    if(id.equals("0")){
                        broadcast(msg);
                    }
                    // 하나의 셀에게 메세지를 발송하려는 경우
                    else {
                        pw = hm.get(id);
                        pw.println(msg);
                        pw.flush();
                    }

                    break;

                case "SocketClientMain" :

                    pw.println(msg);
                    pw.flush();

                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 메인 로직 처리 후, UI 변경 해주는 부분 ( UI Thread )
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                editText.setText("");

            }
        });
    }

    // 발신자가 "서버" 일 경우 사용 : 모든 셀들에게 메세지 보내기
    public void broadcast(String msg) {

        synchronized (hm) {

            Collection<PrintWriter> collection = hm.values();
            Iterator<PrintWriter> iter = collection.iterator();

            while (iter.hasNext()) {
                PrintWriter pw = (PrintWriter) iter.next();
                pw.println(msg);
                pw.flush();
            }
        }

    }
}
