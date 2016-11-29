/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 *
 * @author loic.dessaules
 */
public class FXMLDocumentController implements Initializable {
    
    private ServerSocket serverSocket;
    private Socket socketClient;
    private ArrayList<User> listUsers = new ArrayList();
    
    
    @FXML
    private TextArea taContent;
    @FXML
    private Button btnConnection;
    @FXML
    private Button btnDisconnection;
    @FXML
    private Label label;
    @FXML
    private TextArea taOnlineUsers;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hide Disconnection button when we display the window
        btnDisconnection.setVisible(false);
    }    

    @FXML
    private void btnConnectionActionPerformed(ActionEvent event) {
        taContent.setText("");
        // We waiting a connection
        // -> if there is a connection (true) (all it's right) we do the connection
        // -> False is returned when : If the server has a problem, in the method "waitingConnection", in the "Catch" 
        // We display the error (port already used etc.) and return false
        if(waitingConnection()){
            btnConnection.setVisible(false);
            btnDisconnection.setVisible(true);
        }
    }
    
    @FXML
    private void btnDisconnectionActionPerformed(ActionEvent event) {
        try {
            sendToAll("[ServerDisconnected]", "[noExept]");
            // Close All socket of user online
            for (int i = 0; i < listUsers.size(); i++) {
                listUsers.get(i).getSocket().close();
            }
            // We close the main socket (server listening)
            // And we return to the initial state 
            serverSocket.close();
            taContent.setText("");
            
            taOnlineUsers.setText("");
            listUsers.clear();
            
            btnDisconnection.setVisible(false);
            btnConnection.setVisible(true);
        } catch (IOException ex) {
            System.out.println("Erreur (btn deconnecter) : "+ex.getMessage());
        }
    }
    
    // Place the "cursor" of the text area at the botom of it
    private void goToTheEndOfTheTextArea(String textArea){
        if(textArea == "content"){
            taContent.positionCaret(taContent.getLength());
        }
    }
    
    private void updateOnlineUserTextArea(){
        String userConnectedWithIp = "";
        String userConnected = "";
        for (int i = 0; i < listUsers.size(); i++) {
            if(listUsers.get(i).getSocket().isConnected()){
                userConnectedWithIp += listUsers.get(i).getUserName()+" ("+listUsers.get(i).getIp()+")"+"\n";
                userConnected += listUsers.get(i).getUserName()+";";
            }
        }
        taOnlineUsers.setText("");
        taOnlineUsers.setText(userConnectedWithIp);
        sendToAll("[OnlineUsers];"+userConnected, "[noExept]");
    }
    
    // get the time when you call the method
    private String getTimeFormated(){
        String time = "";
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minutes = Calendar.getInstance().get(Calendar.MINUTE);
        int seconds = Calendar.getInstance().get(Calendar.SECOND);
        if(seconds < 10){
            time = hours+":"+minutes+":0"+seconds+" ";
        }else{
            time = hours+":"+minutes+":"+seconds+" ";
        } 
        
        return time;
    }
    
    private void disconnectUser(String username){
        for (int i = 0; i < listUsers.size(); i++) {
            if(listUsers.get(i).getUserName() == username){
                try {
                    listUsers.get(i).getSocket().close();
                    listUsers.remove(i);
                } catch (IOException ex) {
                    System.out.println("Error on method 'disconnectUser' ex:"+ex.getMessage().toString());
                }
            }
        }
        updateOnlineUserTextArea();
    }
    
    private boolean waitingConnection() {
        
        int port = 23002;
        Thread waitingConnectionThread;
        
        try {
            // initialisation of the socketServer, listening on the n port
            serverSocket = new ServerSocket(port);
            
            // Display the state of the server
            taContent.setText(taContent.getText()+getTimeFormated()+"Le serveur écoute sur le port : "+serverSocket.getLocalPort()+" ...\n");
            goToTheEndOfTheTextArea("content");
            
            // Thread creation for for waiting all connections (start at the end of the method)
            waitingConnectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int n = 0;
                    // When the serverSocket is closed (Clic on the disconnection button), we stop the thread
                    while(!serverSocket.isClosed()){
                        try {
                            // We're waiting a connection and display an info 
                            socketClient = serverSocket.accept();
                            taContent.setText(taContent.getText()+getTimeFormated()+socketClient.getInetAddress() + " veut se connecter, authentification en cours ..."+"\n");
                            goToTheEndOfTheTextArea("content");
                            
                            userIdentification(socketClient);

                        } catch (IOException ex) {
                            System.out.println("Erreur Serveur : "+ ex.getMessage());
                        }
                    }
                    
                    
                    // Si on sort de la boucle -> serveur coupé => fermer les socket clients


                }

            });
            waitingConnectionThread.start();
            return true;

        } catch (IOException ex) {
            System.out.println("Erreur : Port "+port+" déjà utilisé, ou mal fermé.");
            taContent.setText(taContent.getText()+getTimeFormated()+"Erreur : Port "+port+" déjà utilisé, ou mal fermé.\n");
            goToTheEndOfTheTextArea("content");
            
            return false;
            
        }
    }
    
    // Get the username of the new user
    private void userIdentification(Socket socketClient) {
        Thread identificationThread;  
        
        identificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Directly after the socket connection, the client sends his username, so we get it
                    BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                    String username = in.readLine();
                    taContent.setText(taContent.getText()+getTimeFormated()+username+" s'est connecté\n");
                    goToTheEndOfTheTextArea("content");
                    
                    // We create a User object and we add the user on the list and update the taOnlineUser
                    User user = new User(username, socketClient, socketClient.getInetAddress());
                    listUsers.add(user);
                    //updateTextAreaOnlineUser();
                    updateOnlineUserTextArea();
                    sendToAll(username+" s'est connecté", username);
                    // Launch the Chat with client - server
                    chatting(socketClient, username);
                } catch (IOException ex) {
                    System.out.println("ERROR in method : userIdentification() ex = "+ex.getMessage().toString());
                }
                
            }
        });
        
        identificationThread.start();
    }
    
    // Get message from client
    private void chatting(Socket socketClient, String username){
        Thread chattingThread;
        
        chattingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String messageReceived;
                BufferedReader in;
                
                try {
                    
                    // While there is a connection with the client
                    while(!socketClient.isClosed()){
                        // We get the messageReceived
                        in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                        messageReceived = in.readLine();
                        
                        // If it's quit, we disconnected the client (close the socket)
                        if(messageReceived.equals("/quit")){
                            socketClient.close();
                            taContent.setText(taContent.getText()+getTimeFormated()+username+" c'est déconnecté\n");
                            goToTheEndOfTheTextArea("content");
                            
                            sendToAll(username+" s'est déconnecté", username);
                            
                            disconnectUser(username);
                        }
                        // Else, we display the message on the content text area and we send to all other users the message
                        else{
                            taContent.setText(taContent.getText()+getTimeFormated()+username+" : "+messageReceived+"\n");
                            goToTheEndOfTheTextArea("content");
                            // Send to all, exept the user who send the message
                            messageReceived = getTimeFormated()+username+" : "+messageReceived;
                            sendToAll(messageReceived, username);
                       }


                    }
                } catch (IOException ex) {
                    // If the socket is closed (certainly a bad close)
                    taContent.setText(taContent.getText()+getTimeFormated()+username+" c'est déconnecté\n");
                    goToTheEndOfTheTextArea("content");
                    disconnectUser(username);
                }
                
            }
        });
        
        chattingThread.start();
                
    }
    
    private void sendToAll(String message, String exeptUser){
        System.out.println(message);
        for (int i = 0; i < listUsers.size(); i++) {
            if(listUsers.get(i).getUserName() != exeptUser){
                try {
                    PrintWriter out = new PrintWriter(listUsers.get(i).getSocket().getOutputStream());
                    out.println(message);
                    out.flush();
                } catch (IOException ex) {
                    System.out.println("ERROR in method : sendToAll ex = "+ex.getMessage().toString());
                }
            }
        }
    }

}
