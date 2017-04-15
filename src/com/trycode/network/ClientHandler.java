package com.company.trycode.network;

import com.company.trycode.gui.Server;
import com.company.trycode.resource.OnlineListSingleton;
import com.company.trycode.resource.mySQLDB;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;

/**
 * Created by azkei on 14/04/2017.
 */
public class ClientHandler implements Runnable {

    BufferedReader reader;
    Socket socket;
    PrintWriter client;
    ObjectOutputStream dos;
    Server serverSingleton;
    OnlineListSingleton dataSingleton;


    public ClientHandler(Socket clientSocket, PrintWriter user) {
        dataSingleton = OnlineListSingleton.getInstance();
        serverSingleton = Server.getFirstInstance();
        client = user;
        try {
            socket = clientSocket;
            InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(isReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run(){
        String message,
                connect = "Connect", disconnect = "Disconnect",
                chat = "Chat", register = "Register", login = "Login",
                play = "Play", invite = "Invite";

        String[] data;
        try {
            while ((message = reader.readLine()) != null) {
                serverSingleton.ta_chat.append("Received: " + message + "\n");
                data = message.split(":");

                for (String token : data) {
                    serverSingleton.ta_chat.append(token + "\n");
                }
                //if user is valid connection
                if (data[2].equals(connect)) { //not use
                    System.out.println(data[2]);
                    tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                    userAdd(data[0]);
                } else if (data[2].equals(disconnect)) {
                    userRemove(data[0]);
                } else if (data[2].equals(chat)) {
                    tellEveryone(message);
                } else if(data[2].equals(register)){
                    registerUser(data[0] + ":" + data[1],client);
                } else if(data[2].equals(login)){
                    loginUser(data[0] + ":" + data[1],client);
                    //pair username to writer
                    dataSingleton.addOnlinePair(data[1],client);
                }else if(data[3].equals(play)){
                    //function that sends data to the client
                    //System.out.println(data[0],data[1],data[2]);
                }else if(data[1].equals(invite)){
                    //this should return the socket information
                    //thats tied to the username
                    //sendClientInvite(singleton.fetchSocket(data[0]));
                }else{
                    serverSingleton.ta_chat.append("No conditions were met. \n");
                }
            }
        }catch(Exception e){
            serverSingleton.ta_chat.append("Lost a connection. \n");
            e.printStackTrace();
            serverSingleton.clientOutputStreams.remove(client);
        }
    }


    //this is to send a specific client an invitation
    /*public  void sendClientInvite(PrintWriter clientInfo){
        try{
            PrintWriter writer = new PrintWriter(clientInfo.getOutputStream());
            writer.println("Hi client, i want to play you");
            writer.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

    }*/

    public void userRemove (String data)
    {
        String message, add = ": :Connect", done = "Done", name = data;
        serverSingleton.users.remove(name);
        serverSingleton.ta_chat.append("After " + name + " remove from server. \n");
        String[] tempList = new String[(serverSingleton.users.size())];
        serverSingleton.users.toArray(tempList);

        message = ("Remove:"+name);
        sendOnlineList(message);
    }

    public void userAdd(String data)
    {
        String name = data, message,done = "Done:Server";
        serverSingleton.ta_chat.append("Before " + name + " added. \n");
        serverSingleton.users.add(name);
        serverSingleton.ta_chat.append("After " + name + " added. \n");
        String[] tempList = new String[(serverSingleton.users.size())];
        serverSingleton.users.toArray(tempList);

        //send message to clear client online user buffers before sending
        sendOnlineList("Sending: Server");
        for (String token:tempList)
        {
            message = ("Add:"+token);
            sendOnlineList(message);
        }

    }

    //this function broadcasts the online users in the server to every client
    public void sendOnlineList(String data){
        Iterator it = serverSingleton.clientOutputStreams.iterator();

        while(it.hasNext()){
            try
            {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(data);
                System.out.println("Sending:"+ data);
                writer.flush();
            }
            catch (Exception ex)
            {
                serverSingleton.ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    //Sending data function
    public void tellEveryone(String message)
    {
        Iterator it = serverSingleton.clientOutputStreams.iterator();

        while (it.hasNext())
        {
            try
            {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                System.out.println(message);
                serverSingleton.ta_chat.append("Sending: " + message + "\n");
                writer.flush();
                serverSingleton.ta_chat.setCaretPosition(serverSingleton.ta_chat.getDocument().getLength());
            }
            catch (Exception ex)
            {
                serverSingleton.ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    public void loginUser(String message, PrintWriter client){

        mySQLDB connect = new mySQLDB();
        boolean valid;

        String[] data;
        data = message.split(":");

        String username = data[0];
        String password = data[1];



        valid = connect.Login(username,password);
        if(valid){
            //adding logged in users to the online array list.
            userAdd(data[0]);
            //accDetails holds account details (username,rank,win,loss,coins,skins respectively)
            String[] accDetails;
            accDetails = connect.viewProfile(username);
            serverSingleton.ta_chat.append(data[0]+" has successfully logged in");
            client.println(data[0]+" You have logged in, Welcome !" + ":Login"+ ":"+ accDetails[0] + ":"+ accDetails[1] + ":"+ accDetails[2] + ":"+ accDetails[3] + ":"+ accDetails[4]+ ":"+ accDetails[5]);
            client.flush();
        }else{
            serverSingleton.ta_chat.append(data[0]+" has failed to log in, Invalid Credentials");
            client.println(data[0]+" You have failed to log in, Invalid Credentials!" + ":Login");
            client.flush();
        }
    }

    public void registerUser(String message, PrintWriter client){
        mySQLDB connect = new mySQLDB();
        boolean valid;
        String[] data;
        data = message.split(":");
        String username = data[0];
        String password = data[1];
        //returns valid true if insert in to database  of false
        valid = connect.insertData(username, password);
        //if the SQL insert, inserted properly...
        if (valid) {
            serverSingleton.ta_chat.append(data[0]+" has been registered");
            client.println(data[0]+" has been registered."+":Message");
            client.flush();
        } else {
            //If insert failed
            System.out.println("Register fail");
            serverSingleton.ta_chat.append(data[0]+" has failed to register");
            client.println(data[0]+" has failed to register."+":Message");
            client.flush();
        }
    }
}
