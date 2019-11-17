package com.example.javasockettest.util;

import android.util.Log;

public class SocketCellTimer extends Thread{

    public SocketCellTimer(){

    }

    @Override
    public void run() {
        super.run();

        int count = 60;
        count = count-1;

        Log.e("result","아랏 : "+count);

//        while (true){
//
//
//
//        }
    }

}
