package org.example.service.process;

import org.example.Config;
import org.example.app.AppRun;
import org.example.dto.Message;
import org.example.dto.RequestDTO;
import org.example.repository.DataBaseAccess;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {
    private DataBaseAccess dataBaseAccess;
    private MessageDigest digest;
    private static UserService userService;

    private UserService() {
        try {
            digest=MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("User service initialization failed", e);
        }
        dataBaseAccess = DataBaseAccess.getInstance();
    }

    public static UserService getInstance() {
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }

    public void login(RequestDTO requestDTO, ObjectOutputStream out) throws IOException {
        digest.reset();
        digest.update(Config.getHashKey());
        byte[] hash = digest.digest(requestDTO.getLogin().password().getBytes());
        if(dataBaseAccess.searchForPassword(hash) && dataBaseAccess.searchForUsername(requestDTO.getLogin().username())) {
            digest.reset();
            digest.update(Config.getTokenKey());
            byte[] token = digest.digest(requestDTO.getLogin().username().getBytes());
            String authToken = new String(token, StandardCharsets.UTF_8);
            RequestDTO loginRequestDTO = new RequestDTO();
            loginRequestDTO.setAuthToken(authToken);
            loginRequestDTO.setRequestType(requestDTO.getRequestType());
            out.writeObject(loginRequestDTO);
            System.out.println("Client authenticated: " + requestDTO.getLogin().username());
        }
        else {
            RequestDTO cancelRequestDTO = new RequestDTO();
            out.writeObject(cancelRequestDTO);
        }
    }
    public void register(RequestDTO requestDTO, ObjectOutputStream out) throws IOException {
        digest.reset();
        digest.update(Config.getHashKey());
        byte[] hash = digest.digest(requestDTO.getRegister().password().getBytes());
        if(!(dataBaseAccess.searchForPassword(hash) && dataBaseAccess.searchForUsername(requestDTO.getRegister().username()))) {
            digest.reset();
            digest.update(Config.getTokenKey());
            byte[] token = digest.digest(requestDTO.getRegister().username().getBytes());
            String authToken = new String(token, StandardCharsets.UTF_8);
            RequestDTO registerRequestDTO = new RequestDTO();
            registerRequestDTO.setAuthToken(authToken);
            registerRequestDTO.setRequestType(requestDTO.getRequestType());
            out.writeObject(registerRequestDTO);
            dataBaseAccess.newUser(requestDTO.getRegister().username(), hash);
            System.out.println("Client registered: " + requestDTO.getRegister().username());
        }
        else {
            RequestDTO cancelRequestDTO = new RequestDTO();
            out.writeObject(cancelRequestDTO);
        }
    }
    public void logout(RequestDTO requestDTO,Socket socket){
        digest.reset();
        digest.update(Config.getTokenKey());
        byte[] token = digest.digest(requestDTO.getUsername().getBytes());
        String authToken = new String(token, StandardCharsets.UTF_8);
        if(requestDTO.getAuthToken().equals(authToken)) {
            try {
                socket.close();
                AppRun.getInstance().getSockets().remove(socket);
            } catch (IOException e) {
                throw new RuntimeException("Socket refused to close connection" + e.getMessage());
            }
        }

    }
    public void history(RequestDTO requestDTO, ObjectOutputStream out){
        digest.reset();
        digest.update(Config.getTokenKey());
        byte[] token = digest.digest(requestDTO.getUsername().getBytes());
        String authToken = new String(token, StandardCharsets.UTF_8);
        if(requestDTO.getAuthToken().equals(authToken)) {
            RequestDTO historyRequestDTO = new RequestDTO();
            historyRequestDTO.setHistory(dataBaseAccess.messageHistory());
            historyRequestDTO.setRequestType(requestDTO.getRequestType());
            try {
                out.writeObject(historyRequestDTO);
            } catch (IOException e) {
                throw new RuntimeException("Message history transportation failed : " + e.getMessage());
            }
        }
        else{
            System.out.println("Client not authenticated: " + requestDTO.getUsername());
        }
    }
}
