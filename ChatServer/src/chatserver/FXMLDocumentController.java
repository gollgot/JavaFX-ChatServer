/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
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
    private TextArea textAreaContent;
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
            textAreaContent.setText("");
            btnDisconnection.setVisible(false);
            btnConnection.setVisible(true);
        } catch (IOException ex) {
            System.out.println("Erreur (btn deconnecter) : "+ex.getMessage());
        }
    }
    
    
    
    private boolean waitingConnection() {
        
        int port = 23002;
        
        try {
            // initialisation of the socketServer, listening on the n port
            serverSocket = new ServerSocket(port);
            
            // Display the state of the server
            textAreaContent.setText(" Le serveur écoute sur le port : "+serverSocket.getLocalPort()+" ...\n\n");
            
            // Thread creation for for waiting all connections (start at the end of the method)
            Thread threadWaitingConnexion = new Thread(new Runnable() {
                @Override
                public void run() {
                    // When the serverSocket is closed (Clic on the disconnection button), we stop the thread
                    while(!serverSocket.isClosed()){
                        try {
                            // We're waiting a connection and display an info 
                            socketClient = serverSocket.accept();
                            textAreaContent.setText(textAreaContent.getText() + " " + socketClient.getInetAddress() + " veut se connecter, authentification en cours ..."+"\n\n");
                            
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
            threadWaitingConnexion.start();
            return true;

        } catch (IOException ex) {
            System.out.println("Erreur : Port "+port+" déjà utilisé, ou mal fermé.");
            textAreaContent.setText(textAreaContent.getText()+"Erreur : Port "+port+" déjà utilisé, ou mal fermé.\n\n");
            
            return false;
            
        }
    }

}
