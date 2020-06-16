/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Set;

import static com.example.android.bluetoothchat.DeviceListActivity.EXTRA_DEVICE_ADDRESS;
import static java.lang.Thread.sleep;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link Fragment} which can display a view.
 * <p>
 * on other devices it's visibility is controlled by an item on the Action Bar.
 *
 * */
public class MainActivity extends SampleActivityBase {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;



    //////////////////////////////////////////////////////////////////
    // UDP Communication - dmkim (test ip)
    public static final String sIP = "118.221.46.185";
    public static final int sPORT = 5000;

    // SERVER Communication - kuloeh (dApp ip)
    public static final String wasIP ="125.131.9.88";
    public static final int wasPORT = 8082;

    // class for data send
    public SendData mSendData = null;
    public ReceiveData mReceiveData = null;

    private Button mStartService;

    private TextView tAccessCode_1;
    private TextView tAccessCode_2;
    private TextView tPairingAuthStatus;

    private Button mBtnAck_1;
    private Button mBtnAck_2;
    private Button mBtnPairingAck;

    private Button mBtnPairAuthTest;

    private Button mBtnServerTest_1;
    public RequestQueue requestQueue;

    static byte[] sendPacket = new byte[32];

    //UDP 통신용 소켓 생성
    static DatagramSocket socket;
    //서버 주소 변수
    static InetAddress serverAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }


        try {
            socket = new DatagramSocket(5000);
            serverAddr = InetAddress.getByName(sIP);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // [dmkim] Start Service
        mStartService = (Button) findViewById(R.id.btnStartService);
        mStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //create SendData class
                    mSendData = new SendData((byte) 0x31, (byte) 0x32, (byte) 0xD0);
                    // start send message
                    mSendData.start();
                }
        });

        // [dmkim] Ack 1
        mBtnAck_1 = (Button) findViewById(R.id.btnACK_1);
        mBtnAck_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create SendData class
                mSendData = new SendData((byte) 0x31, (byte) 0x32, (byte) 0xD1);
                // start send message
                mSendData.start();
            }
        });

        // [dmkim] Pairing Ack
        mBtnPairingAck = (Button) findViewById(R.id.btnPairingACK);
        mBtnPairingAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create SendData class
                mSendData = new SendData((byte) 0x31, (byte) 0x32, (byte) 0x80);
                // start send message
                mSendData.start();
            }
        });

        // [dmkim] mBtnPairAuthTest
        mBtnPairAuthTest = (Button) findViewById(R.id.btnRecvPairingAuth);
        mBtnPairAuthTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create SendData class
                mSendData = new SendData((byte) 0x31, (byte) 0x32, (byte) 0xAB);
                // start send message
                mSendData.start();
            }
        });


        // [dmkim] Ack 2
        mBtnAck_2 = (Button) findViewById(R.id.btnACK_2);
        mBtnAck_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create SendData class
                mSendData = new SendData((byte) 0x31, (byte) 0x32, (byte) 0xD3);
                // start send message
                mSendData.start();
            }
        });


        tAccessCode_1 = (TextView) findViewById(R.id.txtAccessCode_1);
        tAccessCode_2 = (TextView) findViewById(R.id.txtAccessCode_2);

        tPairingAuthStatus = (TextView) findViewById(R.id.txtPairingStatus);

        mReceiveData = new ReceiveData();
        mReceiveData.start();

        // [kuloeh] server test 1
        mBtnServerTest_1 = (Button) findViewById(R.id.btnServerTest_1);
        mBtnServerTest_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String server_url = wasIP+":"+wasPORT+"/api/mobileTest";
                //String server_url = "http://125.131.9.88:8082/api/mobileTest";
                //String server_url = "http://125.131.9.88:8082/api/getDeviceList";
                String server_url = wasIP+":"+wasPORT+"/api/getDeviceList";
                Toast.makeText(getApplicationContext(), server_url, Toast.LENGTH_SHORT).show();

                try {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(),
                                    "Response :" + response.toString(), Toast.LENGTH_LONG).show(); //display the response on screen
                            requestQueue.stop();
                            System.out.println("Response :" + response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG,"Error :" + error.toString());
                        }
                    });
                    requestQueue.add(stringRequest);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // UDP
    //데이터 보내는 쓰레드 클래스
    static class SendData extends Thread {

        private byte destId = 0x30;
        private byte sourceId = 0x30;
        private byte serviceCode = 0x30;

        public SendData(byte argDestId, byte argSourceId, byte argServiceCode){
            this.destId = argDestId;
            this.sourceId = argSourceId;
            this.serviceCode = argServiceCode;
        }

        public void run() {
            try {

                // Creation send message
                //byte[] buf = ("H").getBytes();
                for(int i=0; i<32; i++){
                    sendPacket[i] = 0x00;
                }
                sendPacket[0] = 'S';
                sendPacket[1] = this.destId;
                sendPacket[2] = this.sourceId;
                sendPacket[3] = this.serviceCode;
                sendPacket[31] = 'E';

                // Convert packet
                DatagramPacket packet = new DatagramPacket(sendPacket, sendPacket.length, serverAddr, sPORT);

                // Send Packet.
                socket.send(packet);

            } catch (Exception e) {

            }
        }
    }


    // UDP
    //데이터 수신 쓰레드 클래스
    class ReceiveData extends Thread {

        byte[] receivePacket = new byte[32];

        public void run() {
            try {
                //UDP 통신용 소켓 생성
                DatagramPacket recvPacket = new DatagramPacket(this.receivePacket, this.receivePacket.length);

                while(true) {
                    // Waiting receive message
                    socket.receive(recvPacket);

                    // Convert message type (byte[]) to String.
                    String msg = new String(recvPacket.getData());
                   // Log.i(TAG, msg);

                    // [2] 1's Auth
                    if(recvPacket.getData()[3] == (byte) 0xA0){
                        tAccessCode_1.setText(msg.toCharArray(), 4, 6);
                    } else if(recvPacket.getData()[3] == (byte) 0xA1) {
                        tAccessCode_2.setText(msg.toCharArray(), 4, 6);
                    } else if(recvPacket.getData()[3] == (byte) 0xAB) {
                        tPairingAuthStatus.setText("Pairing Auto: Yes");
                    }
                }

            } catch (Exception e) {

            }
        }
    }
}
