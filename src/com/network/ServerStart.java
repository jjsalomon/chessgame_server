package com.network;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import com.gui.Server;

/**
 * Created by Francis on 4/12/2017.
 */
public class ServerStart implements Runnable
{
    ArrayList clientOutputStreams;

    ArrayList onlineStreams;
    Server servergui;

    @Override
    public void run()
    {
        clientOutputStreams = new ArrayList();
        onlineStreams = new ArrayList();


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