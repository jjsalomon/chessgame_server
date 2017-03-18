package com.company;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Created by azkei on 16/03/2017.
 */
public class Server extends Network implements Runnable{

    private String hostName;
    private Thread thread;
    private ServerSocket serverSocket;
    private final int listenPort;

    public Server(final String host,final int listen_port) {
        super("SERVER");
        hostName = host;
        listenPort = listen_port;
        initializeServer();
        thread = new Thread(this,"Server Thread");
        thread.start();
    }

    private void initializeServer() {
        try{
            serverSocket = new ServerSocket(listenPort,8, InetAddress.getByName(hostName));
            System.out.println("SERVER: Server Initialized....waiting for connections.");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("SERVER: Thread communication working");
        try{
            listenForConnection();
            getStreams();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void listenForConnection(){
        try {
            socket = serverSocket.accept();
            System.out.println("SERVER: Connection received from: " + socket.getInetAddress().getHostName());
        }catch(IOException e){
            e.printStackTrace();
        }
    }


}
