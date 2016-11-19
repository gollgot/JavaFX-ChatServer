/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Loic
 */
public class User {
    
    private String userName;
    private Socket socket;
    private InetAddress ip;

    public User(String userName, Socket socket, InetAddress ip) {
        this.userName = userName;
        this.socket = socket;
        this.ip = ip;
    }
    
    
    /* GETTERS */
    public String getUserName() {
        return userName;
    }

    public Socket getSocket() {
        return socket;
    }
    
    
    /* SETTERS */
    public InetAddress getIp() {
        return ip;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
    
    
    
    
    
}
