package com.company;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by azkei on 13/04/2017.
 * This class is a singleton that holds all the users online
 */
public final class OnlineListSingleton {


    private static OnlineListSingleton instance = null;
    private HashMap<String, PrintWriter> map = new HashMap<>();

    static boolean firstThread = true;
    private OnlineListSingleton(){}

    //lazy instantiation
    public static OnlineListSingleton getInstance(){
        if(instance == null){
            //This is here to test what happens if threads
            //try to create instances of this class
            if(firstThread){
                firstThread = false;
                try{
                    Thread.currentThread();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Here we use synchronized when
                //the first object is created
                synchronized (OnlineListSingleton.class){
                    //if the first instance isnt needed it isnt created
                    if(instance == null){
                        instance = new OnlineListSingleton();
                    }
                }
            }
        }
        //Under either circumstance this returns the instance
        return instance;
    }

    //Functions to add, remove, fetch
    public void addOnlinePair(String username, PrintWriter writer){
        map.put(username,writer);
        System.out.println("Successfully paired: "+username+" with Writer: "+ writer);
    }

    public PrintWriter fetchSocket(String username){
        return map.get(username);
    }

    public void deletePair(String username){

    }



}
