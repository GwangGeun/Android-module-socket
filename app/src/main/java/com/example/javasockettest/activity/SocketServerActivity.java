package com.example.javasockettest.activity;

import android.Manifest;
import android.annotation.SuppressLint;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javasockettest.adapter.SocketServerAdapter;
import com.example.javasockettest.dialog.SocketServerDialog;
import com.example.javasockettest.R;
import com.example.javasockettest.item.SocketServerItem;
import com.example.javasockettest.network.SocketServerMain;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.IOException;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 1. 목적
 *
 *  - 사용자들이 서버를 키고 끄는 Acitivty
 *
 * 2. 관련해서 볼 것
 *
 *  - SocketServerMain : 서버를 키고 끄는 곳
 *  - SocketServerDialog : 서버에서 Cell(클라이언트) 로 메세지를 보내는 곳
 *
 * 3. 참고사항
 *
 * (1) persmission : oreo 이후 권한 문제 해결을 위한 권한 리스너
 * (2) 서버에서 Cell(클라이언트) 로 메세지를 보내는 부분은 SocketServerDialog 에 정의되어 있다.
 *
 */
public class SocketServerActivity extends AppCompatActivity {


    String TAG = "SocketServer";
    // 셀의 고유 식별자와 메세지 발송 pipeline 을 담은 HashMap
    HashMap<String, PrintWriter> hm;

    // 핸들러에 UI 변경을 요청할 때 사용하는 상수들
    final int connectionSuccess = 1;
    final int connectionInit = 2;
    final int cellCount = 3;
    final int cellRemove = 4;


    // UI 셋업
    RecyclerView recycler;
    ArrayList<SocketServerItem> dataList = new ArrayList<>();
    SocketServerAdapter socketAdapter;

    boolean btnFlag = false;
    Button btn_start;
    Button btn_send_all;

    TextView text_server;
    TextView text_server_status;
    TextView text_server_cell_count;

    // 소켓 통신 셉업
    ServerSocket server;
    WifiManager wifiMgr;

    // 통신 ip, port
    String bindIp; // 예상 값 : "192.168.0.2"

    SocketServerMain startServer;
    SocketServerDialog serverDialog;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroyCalled");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStopCalled");

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_server);

        // 화면이 꺼지지 않도록 하기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();

    }

    // 초기 셋팅
    public void init() {

        text_server = findViewById(R.id.text_server);
        text_server_status = findViewById(R.id.text_server_status);
        text_server_cell_count = findViewById(R.id.text_server_cell_count);

        btn_start = findViewById(R.id.btn_server_start);
        btn_start.setOnClickListener(listener);

        // Cell 전체에게 메세지를 보내는 버튼 ( 서버 연결이 되어 있지 않은 경우에는 invisible 상태 )
        btn_send_all = findViewById(R.id.btn_server_send_all);
        btn_send_all.setOnClickListener(listener);

        recycler = findViewById(R.id.socket_server_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(SocketServerActivity.this));

        // recyclerView 에 어뎁터 지정 : List 에 담아 놓은 데이터를 표시
        socketAdapter = new SocketServerAdapter(dataList);
        recycler.setAdapter(socketAdapter);

        /**
         * Dialog 에 pw pipeline 전달을 위해, 리사이클러뷰 아이템클릭 리스너를 SocketServerActivity 에서 재정의해서 사용 함. ( SocketServerAdapter 내부에 인터페이스 정의해서 사용 중)
         */
        socketAdapter.setOnItemClickListener(new SocketServerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {

                serverDialog = new SocketServerDialog(SocketServerActivity.this, startServer, hm, dataList.get(pos).getSocketId(), "sendOne");
                serverDialog.show();

            }
        });

        hm = new HashMap<String, PrintWriter>();
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

    }


    // TODO : MemoryLeak 문제 해결
    // UI 변경 메인스레드에서만 가능은 함. 때문에,
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {

                case connectionSuccess:

                    // button 위에 나타나는 텍스트 : end 로 만들어주기
                    btn_start.setText(R.string.end);
                    btnFlag = true;

                    bindIp = (String) msg.obj;

                    // 서버가 현재 연결되어 있는 IP 표시
                    text_server.setText(R.string.HOSTIP_);
                    text_server_status.setText(bindIp);

                    break;

                case connectionInit:

                    // button 위에 나타나는 텍스트 : start 로 만들어주기
                    btn_start.setText(R.string.start);
                    btnFlag = false;

                    // recyclerView 초기화
                    dataList.clear();
                    socketAdapter.notifyDataSetChanged();

                    // 서버가 켜져있지 않음을 표시
                    text_server.setText(R.string.서버상태);
                    text_server_status.setText(R.string.off);

                    // 셀의 갯수도 갱신 시켜주기
                    sendEmptyMessage(cellCount);

                    break;

                case cellRemove :

                    String cellId = (String) msg.obj;

                    for(int i=0; i<dataList.size(); i++){
                        if(cellId.equals(dataList.get(i).getSocketId())){
                            dataList.remove(i);
                            socketAdapter.notifyDataSetChanged();
                            // 셀의 갯수도 갱신 시켜주기
                            sendEmptyMessage(cellCount);
                        }
                    }

                    break;

                case cellCount:

                    socketAdapter.notifyDataSetChanged();
                    int cellCount = socketAdapter.getItemCount();
                    text_server_cell_count.setText(cellCount + " 개");

                    break;

            }

        }
    };


    // Button click event 리스너
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_server_start:

                    // runtime 시에 필요한 권한 체크 ( oreo 버전 이후 대응 )
                    TedPermission.with(SocketServerActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .check();

                    break;


                case R.id.btn_server_send_all :

                    if(socketAdapter.getItemCount() <=0){
                        Toast.makeText(getApplicationContext(), "연결되어 있는 셀이 없습니다.",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    serverDialog = new SocketServerDialog(SocketServerActivity.this, startServer, hm, "sendAll");
                    serverDialog.show();

                    break;

            }

        }
    };

    // Oreo 이후 버전에서는 runtime 시에 권한 체크가 필요하다
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

            // "서버가 꺼져있는 상태" 에서 "서버를 켜져있는 상태" 로 바꾸려고 하는 경우
            if (!btnFlag) {
                // ServerSocket 객체 생성
                try {
                    server = new ServerSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Cell 전체에게 메세지를 보낼 수 있는 버튼 활성화
                btn_send_all.setVisibility(View.VISIBLE);

                startServer = new SocketServerMain(SocketServerActivity.this, server, hm, handler, dataList, wifiMgr, "start");
                startServer.start();
                Toast.makeText(getApplicationContext(), "server start ", Toast.LENGTH_LONG).show();
            }
            // "서버가 켜져있는 상태" 에서 "서버를 꺼져있는 상태" 로 버꾸려고 하는 경우
            else {

                // Cell 전체에게 메세지를 보낼 수 있는 버튼 비활성화
                btn_send_all.setVisibility(View.INVISIBLE);

                SocketServerMain endServer = new SocketServerMain(server, hm, handler,"end");
                endServer.start();
                Toast.makeText(getApplicationContext(), "server end ", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(SocketServerActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };

}
