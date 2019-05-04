package com.example.tcpclient;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class CommunicationThread extends Thread {

    private final static int TCP_SERVER_PORT = 1234;

    private ArrayList<String> messageList = new ArrayList<>();
    private ArrayList<Long> numList = new ArrayList<>();
    private String server;

    private boolean mRun = true;

    public CommunicationThread(String server){
        this.server = server;
    }

    @Override
    public void run() {
        //super.run();
        Log.d("commThread", "Running");
        mRun = true;
        while (mRun) {
            Socket s = null;
            try {
                s = new Socket(server, TCP_SERVER_PORT);
                //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                while (mRun) {
                    Long message;

                    synchronized (numList){
                        while(numList.isEmpty()){
                            try{
                                numList.wait();
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }

                        message = numList.get(0);
                        numList.remove(0);
                    }

                    out.writeLong(message);
                    out.flush();


                }

            } catch (UnknownHostException e){
                e.printStackTrace();
                close();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            } finally {
                if(s != null){
                    try{
                        s.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(long message){
        synchronized (numList){
            //messageList.add(message + '\0');
            numList.add(message);
            numList.notify();
        }
    }

    public void close(){
        Log.d("CommThread", "Connection Closed");
        mRun = false;
        numList.clear();
    }

    public boolean isRunning(){
        return mRun;
    }
}
