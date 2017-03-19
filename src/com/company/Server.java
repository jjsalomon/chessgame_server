package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by azkei on 16/03/2017.
 */
public class Server extends Thread implements Runnable{

    private ObjectOutputStream dos;
    private ObjectInputStream dis;
    private String hostName;
    private Thread thread;
    private ServerSocket serverSocket;
    private final int listenPort;
    private Socket socket;

    private boolean connection = false;

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

        //keep listening for connections
          while(true){
              listenForConnection();
          }




    }

    private void listenForConnection(){
        try {
            socket = serverSocket.accept();
            System.out.println("SERVER: Connection received from: " + socket.getInetAddress().getHostName());
            dos = new ObjectOutputStream(socket.getOutputStream());
            dos.flush();
            dis = new ObjectInputStream(socket.getInputStream());
            //message client saying thanks
            sendData("Thanks for connecting");
            connection = true;
            if(connection == true){
                try {
                    Object message = dis.readObject();
                    System.out.println(message);
                    connection = false;
                }catch(IOException e){
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void closeConnection(){
        try{
            if(dos != null){
                dos.close();
            }
            if(dis != null){
                dis.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendData(final Object obj_send){
        try{
            dos.writeObject(obj_send);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


}
