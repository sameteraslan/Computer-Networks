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
public class Pion {

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
    
    color color;
    public static int[][] start_finish = {{0, 46}, {12, 10}, {24, 22}, {36, 34}};
    public int pionIndex;
    boolean pion_arrived, isAlive;
    int pionType;
    
}
