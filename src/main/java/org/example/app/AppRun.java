package org.example.app;

import org.example.service.RequestHandler;
import org.example.service.ThreadFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppRun {
    private HashMap<Socket,ObjectOutputStream> sockets = new HashMap<>();
    private boolean isServerRunning = true;
    private ServerSocket serverSocket;
    private static AppRun apprun;
    private AppRun(){}
    public static AppRun getInstance(){
        if(apprun == null){
            apprun = new AppRun();
        }
        return apprun;
    }
    public void run(){
        try {
            serverSocket = new ServerSocket(21521);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't listen on port: 21521");
        }
        System.out.println("Server running on port: 21521");
        ThreadFactory threadFactory = ThreadFactory.getInstance();
        while(isServerRunning()){
            try {
                Socket socket=serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                sockets.put(socket,out);
                threadFactory.getThreadPool().execute(()-> RequestHandler.getInstance().checkForRequests(socket,out,in));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't accept connection: ", e);
            }
        }
    }
    public boolean isServerRunning(){
        return isServerRunning;
    }
    public void terminate(){
        isServerRunning = false;
    }
    public HashMap<Socket,ObjectOutputStream> getSockets() {
        return sockets;
    }
}
