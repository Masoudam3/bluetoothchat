package com.example.offlinemassage.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.offlinemassage.ui.activity.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChatController {

    public static  final int STATE_NONE = 0;
    public static  final int STATE_LISTEN = 1;
    public static  final int STATE_CONNECTING = 2;
    public static  final int STATE_CONNECTED = 3;


    private static final String APP_NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ReadWritheThread readWritheThread;
    private int state;
    BluetoothAdapter bluetoothAdapter;
    Handler handler;






    public ChatController(Handler handler){

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        state = STATE_NONE;

    }






    public void  write(byte [] out){
        ReadWritheThread rw;
        synchronized (this){
            if (state != STATE_CONNECTED){
                return;
            }
            rw = readWritheThread;

        }
        rw.write(out);
    }




    public int getState() {
        return state;

    }






    public void setState(int state) {
        this.state = state;

        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }





    public synchronized void  connect(BluetoothDevice device){
        if (state == STATE_CONNECTING){
            if (connectThread != null){
                connectThread.cancel();
                connectThread = null;
            }
        }

        if (readWritheThread != null){
            readWritheThread.cancel();
            readWritheThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);


    }







    public synchronized void connectionFailed() {

        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putSerializable("toast", "Unable to connect to device.");
        message.setData(bundle);
        handler.sendMessage(message);


        ChatController.this.start();
    }






    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }

        if (readWritheThread != null){
            readWritheThread.cancel();
            readWritheThread = null;
        }

        if (acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }

        readWritheThread = new ReadWritheThread(socket);
        readWritheThread.start();


        Message message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_OBJECT);
        Bundle bundle = new Bundle();
        bundle.putParcelable("device", device);
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_CONNECTED);


    }




    public synchronized void connectionLost() {

        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putSerializable("toast", "device Connection Lost");
        message.setData(bundle);
        handler.sendMessage(message);

        ChatController.this.start();
    }






    public synchronized void start() {

        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if (readWritheThread != null){
            readWritheThread.cancel();
            readWritheThread = null;
        }

        setState(STATE_LISTEN);
        if (acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

    }






    public synchronized void stop() {

        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }

        if (readWritheThread != null){
            readWritheThread.cancel();
            readWritheThread = null;
        }

        if (acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);
    }







        public class AcceptThread extends Thread {

         final BluetoothServerSocket serverSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try {

                 tmp =
                        bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }


        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;

            while (state != STATE_CONNECTED){
                try {

                    socket = serverSocket.accept();


                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (socket != null){
                    synchronized (ChatController.this){
                        switch (state){

                            case STATE_CONNECTED:
                            case STATE_CONNECTING:

                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                break;

                            case STATE_NONE:
                            case STATE_LISTEN:
                                // connect
                                try {
                                    socket.connect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:break;
                        }
                    }
                }
            }
        }



        public void cancel(){

            try {
                serverSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }








        public  class ConnectThread extends Thread {

            BluetoothSocket socket;
            BluetoothDevice device;

            public ConnectThread(BluetoothDevice device){
                this.device = device;
                BluetoothSocket tmp = null;
                try {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = tmp;
            }


            @Override
            public void run() {

                setName("ConnectThread");
                try {
                    socket.connect();
                } catch (IOException e) {
                    e.printStackTrace();

                try {
                    socket.close();
                } catch (IOException el) {
                    el.printStackTrace();
                }

                connectionFailed();
                return;

                }

                synchronized (ChatController.this){
                    connectThread = null;
                }

                connected(socket, device);
            }




            public void cancel(){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }








        public  class ReadWritheThread  extends Thread {

             BluetoothSocket socket;
             InputStream stream;
             OutputStream outputStream;

            public ReadWritheThread(BluetoothSocket socket){
                this.socket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stream = tmpIn;
                outputStream = tmpOut;
            }

            @Override
            public void run() {
                byte [] buffer = new  byte[1024];
                int len;

                while (true){
                    try {
                        len = stream.read(buffer);

                        handler.obtainMessage(MainActivity.MESSAGE_READ, len, -1, buffer);

                    } catch (IOException e) {
                        e.printStackTrace();
                        connectionLost();
                        ChatController.this.start();
                    }
                }
            }




        public void write(byte[] buffer){
            try {
                outputStream.write(buffer);
                handler.obtainMessage(MainActivity.MESSAGE_WRITE, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}
