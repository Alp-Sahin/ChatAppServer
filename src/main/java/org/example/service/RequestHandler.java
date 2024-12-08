package org.example.service;

import org.example.Api;
import org.example.app.AppRun;
import org.example.dto.RequestDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class RequestHandler {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private static RequestHandler requestHandler;
    private RequestHandler(){}
    public static RequestHandler getInstance(){
        if(requestHandler == null){
            requestHandler = new RequestHandler();
        }
        return requestHandler;
    }
    public void checkForRequests(Socket socket,ObjectOutputStream out,ObjectInputStream in){
        try {
            this.in = in;
            this.out = out;
            while(AppRun.getInstance().isServerRunning() && !socket.isClosed()){
                RequestDTO requestDTO = (RequestDTO) in.readObject();
                if(requestDTO != null) {
                    Api.getInstance().handle(requestDTO, out, socket);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket closing for : " + socket);
            try {
                AppRun.getInstance().getSockets().remove(socket);
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException("Socket refused to close for : " + socket);
            }
        }
    }

}
