package com.company.trycode;

import com.company.trycode.gui.Server;
import com.company.trycode.resource.mySQLDB;

/**
 * Created by azkei on 14/04/2017.
 */
public class Main {

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
}
