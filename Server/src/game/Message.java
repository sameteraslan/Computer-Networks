/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

/**
 *
 * @author INSECT
 */
public class Message implements java.io.Serializable {

    //mesaj tipleri enum 
    public static enum Message_Type {
        None, Name, Disconnect, RivalConnected, Text, Selected, Bitis, Start, Dice, Turn
    }
    //mesajın tipi
    public Message_Type type;
    //mesajın içeriği obje tipinde ki istenilen tip içerik yüklenebilsin
    public Object content;
    public int pionNumber, pionIndex;
    public int pionType;
    public boolean pion_arrived;

    public Message(Message_Type t) {
        this.type = t;
    }

    public Message(Message_Type t, Pion[] p) {
        this.type = t;
    }

    public Message(Message_Type t, Pion p, int index) {
        this.type = t;
        this.content = index;
    }

}
