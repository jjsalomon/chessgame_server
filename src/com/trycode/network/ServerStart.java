package com.company.trycode.network;

import com.company.trycode.gui.Server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by azkei on 14/04/2017.
 */
public class ServerStart implements Runnable
{
    ArrayList clientOutputStreams;
    ArrayList<String> users;
    ArrayList onlineStreams;
    Server serverSingleton;

    public ServerStart(ArrayList clientOutputStreams, ArrayList<String> users, ArrayList onlineStreams) {
        serverSingleton = Server.getFirstInstance();
        this.clientOutputStreams = clientOutputStreams;
        this.users = users;
        this.onlineStreams = onlineStreams;
    }

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
                System.out.println("Got a connection");
                Thread listener = new Thread(new ClientHandler(clientSock, writer));
                listener.start();
                serverSingleton.appendChat("Got a connection. \n");
            }
        }
        catch (Exception ex)
        {
            serverSingleton.ta_chat.append("Error making a connection. \n");
        }
    }
}