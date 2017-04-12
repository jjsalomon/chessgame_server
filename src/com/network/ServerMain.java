package com.network;


import com.db.mySQLDB;
import com.gui.Server;

/**
 * Created by Francis on 4/12/2017.
 */
public class ServerMain {
    public static void main(String args[])
    {
        //instantiate SQL, and create tables
        mySQLDB connect = new mySQLDB();
        connect.createTable();

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                Server server = new Server();
                server.setVisible(true);
//                new Server().setVisible(true);
            }
        });

    }
}
