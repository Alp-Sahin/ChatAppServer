package org.example.service.process;

import org.example.Config;
import org.example.app.AppRun;
import org.example.dto.Message;
import org.example.dto.RequestDTO;
import org.example.repository.DataBaseAccess;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageService {
    private static MessageService messageService;
    private MessageService() {}
    public static MessageService getInstance() {
        if (messageService == null) {
            messageService = new MessageService();
        }
        return messageService;
    }
    public void newMessage(RequestDTO requestDTO) throws NoSuchAlgorithmException {
        MessageDigest digest=MessageDigest.getInstance("SHA-512");
        digest.reset();
        digest.update(Config.getTokenKey());
        byte[] token = digest.digest(requestDTO.getUsername().getBytes());
        String authToken = new String(token, StandardCharsets.UTF_8);
        if(authToken.equals(requestDTO.getAuthToken())) {
            Message message = requestDTO.getMessage();
            DataBaseAccess.getInstance().newMessage(message.username(), message.message(), message.date());
            AppRun.getInstance().getSockets().forEach((socket, objectOutputStream) -> {
                try {
                    objectOutputStream.writeObject(new RequestDTO(message));
                } catch (IOException e) {
                    throw new RuntimeException("Broadcasting failed for socket " + socket);
                }
            });
        }else{
            System.out.println("Auth token does not match: "+requestDTO.getUsername());
        }

    }
}

