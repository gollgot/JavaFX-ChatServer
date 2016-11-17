/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
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
    
    
    @FXML
    private TextArea taContent;
    @FXML
    private Button btnConnection;
    @FXML
    private Button btnDisconnection;
    @FXML
    private Label label;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hide Disconnection button when we display the window
        btnDisconnection.setVisible(false);
    }    

    @FXML
    private void btnConnectionActionPerformed(ActionEvent event) {
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
            // We close the main socket (server listening)
            // And we return to the initial state 
            serverSocket.close();
            taContent.setText("");
            btnDisconnection.setVisible(false);
            btnConnection.setVisible(true);
        } catch (IOException ex) {
            System.out.println("Erreur (btn deconnecter) : "+ex.getMessage());
        }
    }
    
    // Place the "cursor" of the text area at the botom of it
    private void goToTheEndOfTheTextAreaContent(){
        taContent.positionCaret(taContent.getLength());
    }
    
    private boolean waitingConnection() {
        
        int port = 23002;
        Thread waitingConnectionThread;
        
        try {
            // initialisation of the socketServer, listening on the n port
            serverSocket = new ServerSocket(port);
            
            // Display the state of the server
            taContent.setText(taContent.getText()+"Le serveur écoute sur le port : "+serverSocket.getLocalPort()+" ...\n\n");
            goToTheEndOfTheTextAreaContent();
            
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
                            taContent.setText(taContent.getText() + " " + socketClient.getInetAddress() + " veut se connecter, authentification en cours ..."+"\n\n");
                            goToTheEndOfTheTextAreaContent();
                            
                            userIdentification(socketClient);
                            
                            // WIP -> on ajoute les adresses ip / pseudo etc... dans l'arraylist "test" defini au debut de la classe (pour etre dans tous les scopes)
                            /*test.add(socketClient.getInetAddress().getHostAddress());
                            for (int i = 0; i < test.size(); i++) {
                                // On affiche toutes les personnes connectés a chaques fois
                                System.out.println("Personne : "+test.get(i));
                            }*/
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
            taContent.setText(taContent.getText()+"Erreur : Port "+port+" déjà utilisé, ou mal fermé.\n\n");
            goToTheEndOfTheTextAreaContent();
            
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
                    taContent.setText(taContent.getText()+username+" s'est connecté\n");
                    goToTheEndOfTheTextAreaContent();
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
                        if(messageReceived.equals("quit")){
                            socketClient.close();
                            taContent.setText(taContent.getText()+username+" c'est déconnecté\n");
                            goToTheEndOfTheTextAreaContent();
                            
                        }
                        // Else, we display the message
                        else{
                            taContent.setText(taContent.getText()+username+" : "+messageReceived+"\n");
                            goToTheEndOfTheTextAreaContent();
                       }


                    }
                } catch (IOException ex) {
                    // If the socket is closed (certainly a bad close)
                    taContent.setText(taContent.getText()+username+" c'est déconnecté\n");
                    goToTheEndOfTheTextAreaContent();
                }
                
            }
        });
        
        chattingThread.start();
                
    }

}
