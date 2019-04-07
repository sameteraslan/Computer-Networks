/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

/**
 *
 * @author SAMET
 */
public class Pion implements java.io.Serializable {

    public enum color {Green, Blue, Red, Yellow}
    
    Pion(color c) {
        color = c;
        switch(color) {
            case Green:
                pionType = 0;
                break;
            case Blue:
                pionType = 1;
                break;
            case Red:
                pionType = 2;
                break;
            case Yellow:
                pionType = 3;
                break;
            default:
                System.out.println("Something wrong with pionType");
                break;
        }
    }
    
    public Pion(int c, int index, int number, boolean isArrived) {
        this.pionIndex = index;
        this.pionNumber = number;
        this.pionType = c;
        this.pion_arrived = isArrived;
        switch(pionType) {
            case 0:
                color = color.Green;
                break;
            case 1:
                color = color.Blue;
                break;
            case 2:
                color = color.Red;
                break;
            case 3:
                color = color.Yellow;
                break;
            default:
                System.out.println("Something wrong with pionType");
                break;
        }
}
    
    public color color;
    public static int[][] start_finish = {{0, 46}, {12, 10}, {24, 22}, {36, 34}};
    public int pionIndex;
    public int pionNumber;
    boolean pion_arrived, isAlive;
    public int pionType;
    
}
