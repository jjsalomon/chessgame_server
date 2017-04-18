package com.company;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by azkei on 16/03/2017.
 */

public class Server extends javax.swing.JFrame {

    ArrayList clientOutputStreams;
    ArrayList<String> users;
    ArrayList onlineStreams;


    public class ClientHandler implements Runnable {

        BufferedReader reader;
        Socket socket;
        PrintWriter client;
        ObjectOutputStream dos;


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
        public void run(){

            String message,
                    connect = "Connect", disconnect = "Disconnect",
                    chat = "Chat", register = "Register", login = "Login", refresh = "Refresh",
                    challenge="Challenge";

            String[] data;
            try {
                while ((message = reader.readLine()) != null) {
                    ta_chat.append("Received: " + message + "\n");
                    data = message.split(":");

                    for (String token : data) {
                        ta_chat.append(token + "\n");
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
                    }else if(data[2].equals(challenge)){
                        //add function here for to accept challenge then play a game/ or decline challenge
                        System.out.println("\"someone\" wants to challenge you " + data[0]);
                    } else{
                        ta_chat.append("No conditions were met. \n");
                    }
                }
            }catch(Exception e){
                ta_chat.append("Lost a connection. \n");
                e.printStackTrace();
                clientOutputStreams.remove(client);
            }
        }
    }

    public Server(){
        initializeComponents();
    }


    //GUI Components
    private javax.swing.JButton b_clear;
    private javax.swing.JButton b_end;
    private javax.swing.JButton b_start;
    private javax.swing.JButton b_users;
    private javax.swing.JScrollPane jScrollPane ;
    private javax.swing.JLabel lb_name;
    private javax.swing.JTextArea ta_chat;

    private void initializeComponents(){

        jScrollPane = new javax.swing.JScrollPane();
        ta_chat = new javax.swing.JTextArea();
        b_start = new javax.swing.JButton();
        b_end = new javax.swing.JButton();
        b_users = new javax.swing.JButton();
        b_clear = new javax.swing.JButton();
        lb_name = new javax.swing.JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server Frame");
        setName("Server");
        setResizable(false);

        ta_chat.setColumns(20);
        ta_chat.setRows(5);
        jScrollPane.setViewportView(ta_chat);

        b_start.setText("START");
        b_start.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){

                b_startActionPerformed(evt);
            }
        });

        b_end.setText("END");
        b_end.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_endActionPerformed(evt);
            }
        });

        b_users.setText("Online Users");
        b_users.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_usersActionPerformed(evt);
            }
        });

        b_clear.setText("Clear");
        b_clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b_clearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(b_end, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(b_start, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 291, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(b_clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(b_users, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))))
                                .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lb_name)
                                .addGap(209, 209, 209))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(b_start)
                                        .addComponent(b_users))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(b_clear)
                                        .addComponent(b_end))
                                .addGap(4, 4, 4)
                                .addComponent(lb_name))
        );
        pack();
    }



    private void b_endActionPerformed(java.awt.event.ActionEvent evt) {
        try
        {
            Thread.sleep(5000);//5000 milliseconds is five second.
        }
        catch(InterruptedException ex) {Thread.currentThread().interrupt();}

        tellEveryone("Server:is stopping and all users will be disconnected.\n:Chat");
        ta_chat.append("Server stopping... \n");

        ta_chat.setText("");
    }

    private void b_startActionPerformed(java.awt.event.ActionEvent evt) {
        Thread starter = new Thread(new ServerStart());
        starter.start();

        ta_chat.append("Server started...\n");
    }

    private void b_usersActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.append("\n Online users : \n");
        for (String current_user : users)
        {
            ta_chat.append(current_user);
            ta_chat.append("\n");
        }

    }

    private void b_clearActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.setText("");
    }

    public static void main(String args[])
    {
        //instantiate SQL, and create tables
        mySQLDB connect = new mySQLDB();
        connect.createTable();

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                new Server().setVisible(true);
            }
        });

    }

    public class ServerStart implements Runnable
    {
        @Override
        public void run()
        {
            clientOutputStreams = new ArrayList();
            onlineStreams = new ArrayList();
            users = new ArrayList();

            try
            {
                ServerSocket serverSock = new ServerSocket(2222);

                while (true)
                {
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    ta_chat.append("Got a connection. \n");
                }
            }
            catch (Exception ex)
            {
                ta_chat.append("Error making a connection. \n");
            }
        }
    }




    public void userRemove (String data)
    {
        String message, add = ": :Connect", done = "Done", name = data;
        users.remove(name);
        ta_chat.append("After " + name + " remove from server. \n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        message = ("Remove:"+name);
        sendOnlineList(message);
    }

    public void userAdd(String data)
    {
        String name = data, message,done = "Done:Server";
        ta_chat.append("Before " + name + " added. \n");
        users.add(name);
        ta_chat.append("After " + name + " added. \n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

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
        Iterator it = clientOutputStreams.iterator();

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
                ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    //Sending data function
    public void tellEveryone(String message)
    {
        Iterator it = clientOutputStreams.iterator();

        while (it.hasNext())
        {
            try
            {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                System.out.println(message);
                ta_chat.append("Sending: " + message + "\n");
                writer.flush();
                ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
            }
            catch (Exception ex)
            {
                ta_chat.append("Error telling everyone. \n");
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
            ta_chat.append(data[0]+" has successfully logged in");
            client.println(data[0]+" You have logged in, Welcome !" + ":Login"+ ":"+ accDetails[0] + ":"+ accDetails[1] + ":"+ accDetails[2] + ":"+ accDetails[3] + ":"+ accDetails[4]+ ":"+ accDetails[5]);
            client.flush();
        }else{
            ta_chat.append(data[0]+" has failed to log in, Invalid Credentials");
            client.println(data[0]+" You have failed to log in, Invalid Credentials!" + ":Login");
            client.flush();
        }
    }

    public void
    registerUser(String message, PrintWriter client){
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
            ta_chat.append(data[0]+" has been registered");
            client.println(data[0]+" has been registered."+":Message");
            client.flush();
        } else {
            //If insert failed
            System.out.println("Register fail");
            ta_chat.append(data[0]+" has failed to register");
            client.println(data[0]+" has failed to register."+":Message");
            client.flush();
        }
    }
}