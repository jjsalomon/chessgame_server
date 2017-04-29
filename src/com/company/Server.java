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
    OnlineListSingleton singleton;

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

            singleton = OnlineListSingleton.getInstance();

            String message,
                    connect = "Connect", disconnect = "Disconnect",
                    chat = "Chat", register = "Register", login = "Login",
                    challenge = "Challenge", accept ="Accept", decline = "Decline",
                    move = "Move", interrupt = "Interrupt";

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
                        //pair username to writer
                        singleton.addOnlinePair(data[1],client);
                    } else if(data[2].equals(challenge)){
                        //this should return the socket information
                        //thats tied to the username
                        //@PARAM: Writer of challenged:challenged:challenger
                        //Data coming in: challenged:challenger:Challenge
                        sendClientInvite(singleton.fetchSocket(data[0]),data[0],data[1]);
                        System.out.println(data[0]+" is being challenged by "+data[1]);
                        System.out.println(data[0]+" Socket Info: "+ singleton.fetchSocket(data[0]));
                    }else if(data[0].equals(accept)){
                        String challenger = data[1];
                        String challenged = data[2];

                        PrintWriter challengerWriter = singleton.fetchSocket(challenger);
                        PrintWriter challengedWriter = singleton.fetchSocket(challenged);

                        challengerWriter.println("START"+":"+challenger+":"+challenged);
                        challengerWriter.flush();
                        challengedWriter.println("START"+":"+challenger+":"+challenged);
                        challengedWriter.flush();

                        ta_chat.append("Match between "+challenger+" and "+challenged+" \n");


                    }else if(data[0].equals(decline)){
                        String challenger = data[1];
                        String challenged = data[2];

                        PrintWriter challengerWriter = singleton.fetchSocket(challenger);
                        PrintWriter challengedWriter = singleton.fetchSocket(challenged);

                        challengerWriter.println("DECLINED"+":"+challenger+":"+challenged);
                        challengedWriter.println("DECLINED"+":"+challenger+":"+challenged);

                        ta_chat.append("DECLINED between "+challenger+" and "+challenged+" \n");
                    }else if(data[0].equals(move)){
                        //data coming in into readable strings
                        //challenger
                        String fromClient = data[1];
                        //challenged
                        String toClient = data[2];
                        String sourceTile = data[3];
                        String destinationTile = data[4];

                        //sending to the other client
                        PrintWriter toClientWriter = singleton.fetchSocket(toClient);
                        toClientWriter.println("Move"+":"+fromClient+":"+toClient+":"+sourceTile+":"+destinationTile);
                        toClientWriter.flush();

                    }else if(data[0].equals(interrupt)){

                        //set challenger challenged
                        String disconnectUser = data[1];
                        String client1 = data[2];
                        String client2 = data[3];

                        //remove the disconnected client - send new list to every client
                        userRemove(disconnectUser);

                        //writers
                        PrintWriter clientwriter1 = singleton.fetchSocket(client1);
                        clientwriter1.println("Interrupt:Interrupt:Interrupt");
                        clientwriter1.flush();
                        PrintWriter clientwriter2 = singleton.fetchSocket(client2);
                        clientwriter2.println("Interrupt:Interrupt:Interrupt");
                        clientwriter2.flush();
                    }else{
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

                //Listening mode
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

    //this is to send a specific client an invitation
    public  void sendClientInvite(PrintWriter clientInfo, String challenged, String challenger){
        clientInfo.println("Invite"+":"+challenged+":"+challenger);
        clientInfo.flush();
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
        boolean isLogin = false; //if false, the user is not log in,

        String[] data;
        data = message.split(":");

        String username = data[0];
        String password = data[1];

        //Checks if user is already login
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        for (String token:tempList)
        {
            //Checks if user is already login
           if(token.equals(username)){
               isLogin = true; //if true, user is already login
               ta_chat.append(username+" is already log in");
               client.println(data[0] + ":CheckLogin");
               client.flush();
               break;
           }
        }

        //if user is not log in, validates account and then log in
        if (!isLogin) {
            valid = connect.Login(username,password);
            if(valid){
                //adding logged in users to the online array list.
                userAdd(data[0]);
                getProfile(username, client);
            }else{
                ta_chat.append(data[0]+" has failed to log in, Invalid Credentials");
                client.println(data[0] + ":CheckLogin");
                client.flush();
            }
        }
    }

    public void getProfile(String username,PrintWriter client){
        mySQLDB connect = new mySQLDB();
        //accDetails holds account details (username,rank,win,loss,coins,skins respectively)
        String[] accDetails;
        accDetails = connect.viewProfile(username);
        ta_chat.append(username+" has successfully logged in");
        client.println(username+" You have logged in, Welcome !" + ":Login"+ ":"+ accDetails[0] + ":"+ accDetails[1] + ":"+ accDetails[2] + ":"+ accDetails[3] + ":"+ accDetails[4]);
        client.flush();
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
            ta_chat.append(data[0]+" has been registered");
            client.println(data[0]+" has been registered."+":Message");
            client.flush();
        } else {
            //If insert failed
            System.out.println("Register fail");
            ta_chat.append(data[0]+" has failed to register");
            client.println(data[0]+" has failed to register."+":CheckRegister");
            client.flush();
        }
    }
}