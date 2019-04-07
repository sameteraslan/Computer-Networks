/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jankenponclient;

import game.GUI;
import static game.GUI.btn_dice;
import static game.GUI.lbl_end_text;
import game.Message;
import game.Pion;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jankenponclient.Client.sInput;

/**
 *
 * @author INSECT
 */
// serverdan gelecek mesajları dinleyen thread
class Listen extends Thread {

    public void run() {
        //soket bağlı olduğu sürece dön
        while (Client.socket.isConnected()) {
            try {
                //mesaj gelmesini bloking olarak dinyelen komut
                Message received = (Message) (sInput.readObject());
                //mesaj gelirse bu satıra geçer
                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.type) {
                    case Name:
                        break;
                    case RivalConnected:
                        String name = received.content.toString();
                        
                        System.out.println("baglandi");
                        GUI.ThisGame.tmr_slider.start();
                        break;
                    case Disconnect:
                        break;
                    case Dice:
                        //GUI.ThisGame.rivalDice = received.content + "";
                        //GUI.ThisGame.lblplayer2.setText(received.content + "");
                        GUI.rival_pion_list[received.pionNumber] = new Pion(received.pionType, received.pionIndex, received.pionNumber, received.pion_arrived);
                        GUI.ThisGame.checkIntersections();
                        System.out.println("pion index " + GUI.rival_pion_list[0].pionIndex);
                        System.out.println("pion number " + GUI.rival_pion_list[0].pionNumber);
                        
                        //GUI.ThisGame.updateRivalPions();
                        GUI.ThisGame.updateMap();
                        GUI.ThisGame.updateRival = true;
                        break;
                    case Turn:
                        GUI.btn_dice.setEnabled(true);
                        //GUI.lbl_ingame_name.setText(GUI.txt_name.getText());
                    case Text:
                        //GUI.ThisGame.txt_receive.setText(received.content.toString());
                        break;
                    case Selected:
                        GUI.ThisGame.RivalSelection = (int) received.content;

                        break;

                    case Bitis:
                        GUI.btn_dice.setEnabled(false);
                        GUI.lbl_end_text.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/kaybettin.png")));
                        GUI.lbl_end_text.setVisible(true);
                        Client.Stop();
                        break;

                }

            } catch (IOException ex) {

                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            }
        }

    }
}

public class Client {

    //her clientın bir soketi olmalı
    public static Socket socket;

    //verileri almak için gerekli nesne
    public static ObjectInputStream sInput;
    //verileri göndermek için gerekli nesne
    public static ObjectOutputStream sOutput;
    //serverı dinleme thredi 
    public static Listen listenMe;

    public static void Start(String ip, int port) {
        try {
            // Client Soket nesnesi
            Client.socket = new Socket(ip, port);
            Client.Display("Servera bağlandı");
            // input stream
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            // output stream
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();
            
            //ilk mesaj olarak isim gönderiyorum
            Message msg = new Message(Message.Message_Type.Name);
            msg.content = GUI.txt_name.getText();
            Client.Send(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client durdurma fonksiyonu
    public static void Stop() {
        try {
            if (Client.socket != null) {
                Client.listenMe.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();

                Client.sInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void Display(String msg) {

        System.out.println(msg);

    }

    //mesaj gönderme fonksiyonu
    public static void Send(Message msg) {
        try {
            Client.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
