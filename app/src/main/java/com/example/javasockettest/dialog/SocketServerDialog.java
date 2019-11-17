package com.example.javasockettest.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javasockettest.R;
import com.example.javasockettest.network.SocketServerMain;

import java.io.PrintWriter;
import java.util.HashMap;

public class SocketServerDialog extends Dialog {

    private Activity activity;
    private HashMap<String, PrintWriter> hm;
    private String id;
    private String purpose;

    // purpose 종류
    String sendAll = "sendAll";
    String sendOne = "sendOne";

    private TextView text_server_dialog_id;
    private EditText edit_server_message;

    private SocketServerMain startServer;

    // 서버에서 Cell (클라이언트) 전체에게 메세지를 보내려는 경우
    public SocketServerDialog(Activity activity, SocketServerMain startServer, HashMap<String, PrintWriter> hm, String purpose){

        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = activity;
        this.startServer = startServer;
        this.hm = hm;
        this.purpose = purpose;

    }

    // 서버에서 Cell (클라이언트) 한 개에게 메세지를 보내려는 경우
    public SocketServerDialog(Activity activity, SocketServerMain startServer, HashMap<String, PrintWriter> hm, String id, String purpose){

        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = activity;
        this.startServer = startServer;
        this.id = id;
        this.hm = hm;
        this.purpose = purpose;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dialog 셋업
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        lpWindow.height = 300;
        lpWindow.width = 300;

        setContentView(R.layout.dialog_socket_server);

        Button btn_send_to_client = findViewById(R.id.btn_send_to_client);
        Button btn_close_dialog = findViewById(R.id.btn_close_dialog);
        btn_send_to_client.setOnClickListener(listener);
        btn_close_dialog.setOnClickListener(listener);

        edit_server_message = findViewById(R.id.edit_server_message);
        text_server_dialog_id = findViewById(R.id.text_server_dialog_id);

        // < Dialog Title 설정 >
        // Cell 전체에 메세지를 보내기 위해 다이럴로그에 들어온 경우
        if(purpose.equals(sendAll)){
            text_server_dialog_id.setText(sendAll);
        }
        // Cell 한 개에 메세지를 보내기 위해 다이럴로그에 들어온 경우
        else if(purpose.equals(sendOne)){
            text_server_dialog_id.setText(id);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_send_to_client :

                    String content = edit_server_message.getText().toString();
                    if(content.length() <=0){
                        Toast.makeText(getContext(), "내용을 입력해주세요.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 전체 셀에게 메세지를 보내려는 경우
                    // < 주의 사항 > id 가 "0" 인 경우에는 셀들에게 전체 메세지를 보내는 경우이다
                    if(purpose.equals(sendAll)){
                        // 메세지 발송하는 부분
                        startServer.sednMsg(content,"0",edit_server_message);
                    }
                    // 특정 cell 한 개에게 메세지를 보내는 경우
                    else if(purpose.equals(sendOne)){
                        startServer.sednMsg(content,id,edit_server_message);
                    }

                    break;

                case R.id.btn_close_dialog :

                    dismiss();

                    break;

            }

        }
    };

}
