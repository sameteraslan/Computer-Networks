/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mybasicserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author INSECT
 */
public class MyBasicServer {

    public class ListenServerThread extends Thread {

        private MyBasicServer server;

        public ListenServerThread(MyBasicServer gmyserver) {
            this.server = gmyserver;

        }

        // client dinleme fonksiyonum
        @Override
        public void run() {
            try {
                while (!this.server.sSocket.isClosed()) {
                    Socket cSocket = this.server.sSocket.accept();// client kabul eder ve client soketini döndürür
                    sClient newClient = new sClient(cSocket);
                    this.server.clientList.add(newClient);
                }
            } catch (IOException ex) {
                Logger.getLogger(sClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * @param args the command line arguments
     */
    public ServerSocket sSocket;

    public ArrayList<sClient> clientList;
    public ListenServerThread ServerThread;
    public MyBasicServer(int port) {
        try {

            // TODO code application logic here
            sSocket = new ServerSocket(port);
            clientList = new ArrayList<sClient>();

        } catch (IOException ex) {
            Logger.getLogger(MyBasicServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void StartServer() throws IOException {
        //while (!sSocket.isClosed()) {
        this.ServerThread= new ListenServerThread(this);
        this.ServerThread.start();
        //   }
    }

    public void StopServer() throws IOException {
        sSocket.close();
    }

    public void SendBroadcastMessade(Object message) throws IOException {
        for (sClient gclient : clientList) {
            gclient.sendMessage(message);
        }

    }

    public void SendMessade(Object message, sClient gclient) throws IOException {
        gclient.sendMessage(message);
    }

}
