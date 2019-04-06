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

    public static enum Message_Type {
        None, Name, Disconnect, RivalConnected, Text, Selected, Bitis, Start, Dice, Turn
    }

    public Message_Type type;
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
