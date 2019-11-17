package com.example.javasockettest.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javasockettest.R;
import com.example.javasockettest.adapter.SocketClientAdapter;
import com.example.javasockettest.item.SocketClientItem;
import com.example.javasockettest.network.SocketClientMain;
import com.example.javasockettest.network.SocketSendMsg;


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * 1. 목적
 *
 * - 사용자들이 서버에 메세지를 보내는 Activity
 *
 *
 * 2. 관련해서 볼 것
 *
 * (1) /network/SocketClientMain
 *
 * - socket 최초 설정
 * - 수신 파이프라인 연결
 * - 발신 파이프라인 연결
 *
 * (2) /network/SocketClientReceive
 *
 * - 수신 파이프라인 제작
 *
 * (3) /network/SocketSendMsg
 *
 * - 발신 파이프 라인 제작
 */
public class SocketClientActivity extends AppCompatActivity {

    // recyclerView
    RecyclerView recycler;
    ArrayList<SocketClientItem> dataList = new ArrayList<>();
    SocketClientAdapter socketClientAdapter;

    // UI
    Button btn_client_submit;
    EditText edit_client_content;
    Button btn_client_start;

    // 사용자가 작성한 HostIp
    String hostIp; // 기본 host ip : 192.168.0.2
    int port; // 기본 PORT 번호 : 8888
    boolean socketStatus = false;
    // 사용자가 작성한 내용
    String content;

    // Socket
    BufferedReader br = null;
    PrintWriter pw = null;

    SocketClientMain startClient;

    // 핸들러에서 사용하는 상수
    final int addChatContent = 1;
    private final int connectionSuccess = 2;
    private final int connectionInit = 3;

    // ClientSettingActivity 에서 가져온 내용 받는 목적의 intent
    Intent intent;

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);

        // 화면이 꺼지지 않도록 하기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();

    }

    // view 초기화
    public void init() {

        // 서버 포트 및 IP 지정
        intent = getIntent();
        hostIp = intent.getStringExtra("ip");
        port = Integer.parseInt(intent.getStringExtra("port"));

        edit_client_content = findViewById(R.id.edit_client_content);

        btn_client_submit = findViewById(R.id.btn_client_submit);
        btn_client_submit.setOnClickListener(listener);
        btn_client_start = findViewById(R.id.btn_client_start);
        btn_client_start.setOnClickListener(listener);

        recycler = findViewById(R.id.socket_client_recycler);

        // recyclerView 셋업
        recycler.setLayoutManager(new LinearLayoutManager(SocketClientActivity.this));
        socketClientAdapter = new SocketClientAdapter(dataList);
        recycler.setAdapter(socketClientAdapter);

    }

    // button listener
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_client_start:

                    // 소켓이 연결되어 있지 않은 경우 : 소켓 연결을 한다
                    if (!socketStatus) {
                        startClient = new SocketClientMain(SocketClientActivity.this, edit_client_content, handler, hostIp, port, "start");
                        startClient.start();
                    }
                    // 소켓이 연결되어 있는 경우 : 소켓 연결을 끊는다
                    else {
                        SocketClientMain endClient = new SocketClientMain(handler, "end");
                        endClient.start();
                    }

                    break;

                case R.id.btn_client_submit:

                    content = " 사용자가 작성한 내용 : " + edit_client_content.getText().toString();
                    startClient.sednMsg(content);

                    break;
            }
        }
    };


    /**
     * 1. 목적
     *
     * UI 변경 메인스레드에서만 가능은 함.
     * 때문에, 동적으로 UI 변경이 필요한 경우 사용하기 위한 핸들러
     *
     * 2. 참고
     *
     * (1) addChatContent : 사용자가 채팅 메세지를 입력했을 경우, 발생하는 UI 변경
     * (2) connectionInit : 사용자가 소켓을 해재할 경우, 발생하는 UI 변경
     * (3) connectionSuccess : 사용자가 소켓을 연결 할 경우, 발생하는 UI 변
     */
    // TODO : MemoryLeak 문제 해결
    // UI 변경 메인스레드에서만 가능은 함.
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {

                case addChatContent:

                    // 서버에서 받은 데이터
                    String dataFromServer = (String) msg.obj;

                    // 메세지를 받은 현재 시각
                    SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
                    Date time = new Date();
                    String currentTime = format1.format(time);

                    // 리사이클러뷰에 아이템 추가
                    SocketClientItem socketClientItem = new SocketClientItem(currentTime, dataFromServer);
                    dataList.add(socketClientItem);
                    socketClientAdapter.notifyDataSetChanged();

                    break;

                case connectionInit:

                    // recyclerView 초기화
                    dataList.clear();
                    socketClientAdapter.notifyDataSetChanged();

                    // socket 이 열려있는지 여부
                    socketStatus = false;
                    btn_client_start.setText(R.string.start);

                    break;


                case connectionSuccess:

                    // socket 이 열려있는지 여부
                    socketStatus = true;
                    btn_client_start.setText(R.string.end);

                    break;

            }

        }
    };


}
