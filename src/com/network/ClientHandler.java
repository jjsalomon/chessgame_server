package com.network;

import com.db.mySQLDB;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Francis on 4/12/2017.
 */
public class ClientHandler implements Runnable {

    BufferedReader reader;
    Socket socket;
    PrintWriter client;
    ObjectOutputStream dos;

    ArrayList<String> users;

    public ClientHandler(Socket clientSocket, PrintWriter user) {
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
    public void run() {
        users = new ArrayList();

        String message,
                connect = "Connect", disconnect = "Disconnect",
                chat = "Chat", register = "Register", login = "Login";

        String[] data;
        try {
            while ((message = reader.readLine()) != null) {
//                ta_chat.append("Received: " + message + "\n");
                data = message.split(":");

                for (String token : data) {
//                    ta_chat.append(token + "\n");
                }
                //if user is valid connection
                if (data[2].equals(connect)) {
                    tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                    userAdd(data[0]);
                } else if (data[2].equals(connect)) {
                    tellEveryone((data[0] + ": has disconnected." + ":" + chat));
                    userRemove(data[0]);
                } else if (data[2].equals(chat)) {
                    tellEveryone(message);
                } else if (data[2].equals(register)) {
                    registerUser(data[0] + ":" + data[1], client);
                } else if (data[2].equals(login)) {
                    loginUser(data[0] + ":" + data[1], client);
                } else {
//                    ta_chat.append("No conditions were met. \n");
                }
            }
        } catch (Exception e) {
//            ta_chat.append("Lost a connection. \n");
            e.printStackTrace();
            clientOutputStreams.remove(client);
        }
    }


    public void userAdd(String data) {
        String name = data, message, done = "Done:Server";
//        ta_chat.append("Before " + name + " added. \n");
        users.add(name);
//        ta_chat.append("After " + name + " added. \n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        for (String token : tempList) {
            message = ("Add:" + token);
            sendOnlineList(message);
        }
        sendOnlineList(done);

    }

    public void userRemove(String data) {
        String message, add = ": :Connect", done = "Done", name = data;
        users.remove(name);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        for (String token : tempList) {
            message = ("Remove:" + token);
            sendOnlineList(message);
        }
        sendOnlineList(done);
    }

    //this function broadcasts the online users in the server to every client
    public void sendOnlineList(String data) {
        Iterator it = clientOutputStreams.iterator();

        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(data);
                System.out.println("Sending:" + data);
                writer.flush();
            } catch (Exception ex) {
//                ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    //Sending data function
    public void tellEveryone(String message) {
        Iterator it = clientOutputStreams.iterator();

        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                System.out.println(message);
//                ta_chat.append("Sending: " + message + "\n");
                writer.flush();
//                ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
            } catch (Exception ex) {
//                ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    public void loginUser(String message, PrintWriter client) {
        mySQLDB connect = new mySQLDB();
        boolean valid;

        String[] data;
        data = message.split(":");

        String username = data[0];
        String password = data[1];

        valid = connect.Login(username, password);
        if (valid) {
            //adding logged in users to the online array list.
            userAdd(data[0]);
            //accDetails holds account details (username,rank,win,loss,coins,skins respectively)
            String[] accDetails;
            accDetails = connect.viewProfile(username);
//            ta_chat.append(data[0]+" has successfully logged in");
            client.println(data[0] + " You have logged in, Welcome !" + ":Login" + ":" + accDetails[0] + ":" + accDetails[1] + ":" + accDetails[2] + ":" + accDetails[3] + ":" + accDetails[4] + ":" + accDetails[5]);
            client.flush();
        } else {
//            ta_chat.append(data[0]+" has failed to log in, Invalid Credentials");
            client.println(data[0] + " You have failed to log in, Invalid Credentials!" + ":Login");
            client.flush();
        }
    }

    public void
    registerUser(String message, PrintWriter client) {
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
//            ta_chat.append(data[0]+" has been registered");
            client.println(data[0] + " has been registered." + ":Message");
            client.flush();
        } else {
            //If insert failed
            System.out.println("Register fail");
//            ta_chat.append(data[0]+" has failed to register");
            client.println(data[0] + " has failed to register." + ":Message");
            client.flush();
        }
    }
}


