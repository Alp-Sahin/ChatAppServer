package org.example;

import org.example.dto.RequestDTO;
import org.example.service.process.MessageService;
import org.example.service.process.UserService;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Api {
    private static Api api;
    private Api(){}
    public static Api getInstance(){
        if(api == null){
            api = new Api();
        }
        return api;
    }
    public void handle(RequestDTO requestDTO, ObjectOutputStream out, Socket socket){
        UserService userService = UserService.getInstance();
        MessageService messageService = MessageService.getInstance();
        try {
            switch (requestDTO.getRequestType()) {
                case LOGIN -> userService.login(requestDTO, out);
                case REGISTER -> userService.register(requestDTO, out);
                case MESSAGE -> messageService.newMessage(requestDTO);
                case LOGOUT -> userService.logout(requestDTO, socket);
                case HISTORY -> userService.history(requestDTO, out);
            }
        }catch (Exception e){
            System.out.println("Data handling error: " + e.getMessage());
        }
    }
}
