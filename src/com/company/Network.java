package com.company;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by azkei on 16/03/2017.
 */
public abstract class Network extends Thread {

    //ObjectStreams are good for sending class objects rather than primitive types
    protected ObjectOutputStream dos;
    protected ObjectInputStream dis;
    //Socket for connections
    protected Socket socket;
    protected Object receivedMessage;

    Network(final String name){
        super(name);
    }

    //thread run method
    public abstract void run();

    public void getStreams() throws IOException{
        dos = new ObjectOutputStream(socket.getOutputStream());
        dos.flush();
        dis = new ObjectInputStream(socket.getInputStream());
    }

    public void closeConnection(){
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

    public void sendData(final Object obj_send){
        try{
            dos.writeObject(obj_send);
            dos.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }





}
