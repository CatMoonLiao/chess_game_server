package com.example.socketserver;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    ServerSocket server;
    Thread thread;
    String Address = "10.115.50.134";
    int port = 8080;
    TextView tv;
    static ArrayList<Socket> socketlist = new ArrayList<Socket>();
    Thread serverThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.connectnotice);
        try {
            server = new ServerSocket(port);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText("server IP:"+getLocalIpAddress());
                }
            });
            Toast.makeText(this,"Server on",Toast.LENGTH_LONG).show();
            serverThread=new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = server.accept();
                            if(socket.isConnected())
                                Log.e("server","connect success!!!");
                            createNewThread(socket);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(socketlist.size()+" user connect");
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            serverThread.start();
        } catch (Exception e) {
            System.out.println("QQQQQ");
            e.printStackTrace();
        }
    }

    public static void createNewThread(final Socket socket) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketlist.add(socket);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter bw;
                    String temp;

                    while (socket.isConnected()) {
                        temp = br.readLine();
                        if (temp != null) {
                            Log.e("server","receive");
                            int a=Integer.valueOf(temp);
                            int nowsocket=socketlist.indexOf(socket);

                            if(a<0){//usertype
                                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                bw.write(String.valueOf(nowsocket%2)+"\n");
                                bw.flush();
                            }
                            else if(a==100){
                                Socket target;
                                if(nowsocket%2==0)
                                    target=socketlist.get(nowsocket+1);
                                else
                                    target=socketlist.get(nowsocket-1);

                                bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream()));
                                bw.write("1000\n");
                                bw.flush();
                                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                bw.write("-1000\n");
                                bw.flush();

                                socketlist.remove(target);
                                socketlist.remove(socket);
                                target.close();
                                socket.close();

                                break;
                            }
                            else{//position
                                Socket target;
                                if(nowsocket%2==0)
                                    target=socketlist.get(nowsocket+1);
                                else
                                    target=socketlist.get(nowsocket-1);
                                bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream()));
                                bw.write(String.valueOf(a)+"\n");
                                bw.flush();
                                Log.e("server","send");
                            }
                        } else {
                            socketlist.remove(socket);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreferenceIpAddress", ex.toString());
        }
        return null;
    }
}
