/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mybasicclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author INSECT
 */
public class MyBasicClient {

    public class ListenClientThread extends Thread {

        private MyBasicClient client;

        public ListenClientThread(MyBasicClient myclient) {
            this.client = myclient;
        }

        // client dinleme fonksiyonum
        @Override
        public void run() {
            while (cSocket.isConnected()) {
                try {
                    GUI.dlm.addElement(client.cInStream.readObject().toString());
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(MyBasicClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    private Socket cSocket;
    private String ip;
    private int port;
    private ObjectOutputStream cOutStream;
    private ObjectInputStream cInStream;

    public MyBasicClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void startClient() throws IOException {
        cSocket = new Socket(this.ip, this.port);
        cOutStream = new ObjectOutputStream(cSocket.getOutputStream());
        cInStream = new ObjectInputStream(cSocket.getInputStream());
        ListenClientThread listenThread = new ListenClientThread(this);
        listenThread.start();
                
    }

    public void closeClient() throws IOException {
        cSocket.close();
    }
    
    public void sendMessage(Object message) throws IOException{
        cOutStream.writeObject(message);
    }
    
}
