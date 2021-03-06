/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import kizmabiraderclient.Client;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.List;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author SAMET
 */
public class GUI extends javax.swing.JFrame {

    /*
    * Bu sınıfta piyon animasyonları yapılmaktadır.
    */
    public class PionAnimationThread implements Runnable {

        int pionNumber, src, dst;

        public PionAnimationThread(int pionNum, int src, int dst) {
            this.pionNumber = pionNum;
            this.src = src;
            this.dst = dst;
        }

        @Override
        public void run() {
            for (int i = 1; i <= dst; i++) {

                updateCoordinates(pionNumber, src, 1);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                src = src + 1;
                updateMap();

            }
            try {
                Thread.currentThread().join(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Message m = new Message(Message.Message_Type.Dice);
            m.pionType = pionList[pionNumber].pionType;
            m.pionIndex = pionList[pionNumber].pionIndex;
            m.pionNumber = pionNumber;
            m.pion_arrived = pionList[pionNumber].pion_arrived;
            Client.Send(m);
            checkWinner();
        }
    }

    /*
    * Bu sınıfta zar animasyonu yapılmaktadır.
    */
    public class DiceAnimation implements Runnable {

        int number;

        public DiceAnimation(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            for (int i = 0; i < number; i++) {
                lbl_dice.setIcon(new javax.swing.ImageIcon(getClass().getResource(dicePaths[i])));
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            int notAvailablePionCount = checkAvailableCoordinates(number);
            lblplayer1.setText(number + "");
            //txt_dice.setText("" + number);

            String dialogResult = "";
            if (pionCount - finished_pions_count == 1 && number != 6) {
                updateMyPion(pionCount - 1, pionList[pionCount - 1].pionIndex, number);
            } else if (pionCount != 4 && number == 6) {
                selectPionDialogMessage = new String[pionCount - finished_pions_count + 1 - notAvailablePionCount];
                selectPionDialogMessage[0] = "New Pion";
                for (int i = 1 + finished_pions_count; i <= pionCount - notAvailablePionCount; i++) {
                    if (availablePions[i - 1]) {
                        selectPionDialogMessage[i - finished_pions_count] = i + ". Pion";
                    }
                }
                dialogResult = (String) JOptionPane.showInputDialog(ThisGame,
                        "Choose an action",
                        "Action",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        selectPionDialogMessage,
                        selectPionDialogMessage[0]);
                updateSelection(dialogResult, number);
            } else if (pionCount - finished_pions_count == 0 && number != 6) {
                //nothing to do
            } else {
                selectPionDialogMessage = new String[pionCount - finished_pions_count - notAvailablePionCount];
                for (int i = finished_pions_count; i < pionCount - notAvailablePionCount; i++) {
                    if (availablePions[i - finished_pions_count]) {
                        selectPionDialogMessage[i - finished_pions_count] = (i + 1) + ". Pion";
                    }
                }
                dialogResult = (String) JOptionPane.showInputDialog(ThisGame,
                        "Choose an action",
                        "Action",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        selectPionDialogMessage,
                        selectPionDialogMessage[0]);
                updateSelection(dialogResult, number);
            }
            if (number != 6) {
                btn_dice.setEnabled(false);
                Message m = new Message(Message.Message_Type.Turn);
                Client.Send(m);
            }
            if (pionCount > 0) {
                System.out.println(pionList[0].pionIndex);

            }
            try {
                Thread.currentThread().join(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Creates new form GUI
     */
    public enum color {
        Green, Blue, Red, Yellow
    }
    public static Pion.color color;
    public static JLabel[] area = new JLabel[48];
    public static JLabel[][] finish_area = new JLabel[4][4];
    public static JLabel[][] inital_pions = new JLabel[4][4];

    public static Pion[] rival_pion_list = {null, null, null, null};
    public static boolean[] availablePions = new boolean[4];
    public static String[] colorPaths = new String[5];
    public static String[] dicePaths = new String[6];
    public static Pion[] pionList = {null, null, null, null};
    public static String[] selectPionDialogMessage;
    public int pionCount = 0;
    public int finished_pions_count = 0;

    //framedeki komponentlere erişim için satatik oyun değişkeni
    public static GUI ThisGame;
    //ekrandaki resim değişimi için timer yerine thread
    public Thread tmr_slider;
    public PionAnimationThread pionAnimation;
    //karşı tarafın seçimi seçim -1 deyse seçilmemiş
    public int RivalSelection = -1;
    //benim seçimim seçim -1 deyse seçilmemiş
    public int myselection = -1;
    public int turn = 0;
    public int myColor;

    public String rivalDice = "";
    public boolean updateRival = false;

    Message message;

    //Random rand;
    public GUI() {
        this.color = color.Blue;
        initComponents();
        loadAreatoArray();

        //Oyuncu piyon renkleri
        colorPaths[0] = "/images/greenColor.png";
        colorPaths[1] = "/images/blueColor.png";
        colorPaths[2] = "/images/redColor.png";
        colorPaths[3] = "/images/yellowColor.png";
        colorPaths[4] = "/images/salmonColor.png";
        
        //Zar resimleri
        for (int i = 0; i < dicePaths.length; i++) {
            dicePaths[i] = "/images/dice" + (i + 1) + ".png";
        }
        ThisGame = this;
 
        switchPanel(mainPanel);

        //Oyunun başlangıç ekran animasyonu burada gerçekleşiyor.
        tmr_slider = new Thread(() -> {
            try {
                //
                lbl_esleme_araniyor.setText("Eşleşme Bulundu");
                lbl_loading.setText("Oyun Başlıyor!");
                Thread.sleep(1000);
                lbl_geri_sayim.setFont(new Font("Serif", Font.PLAIN, 14));
                lbl_geri_sayim.setText("3");
                Thread.sleep(1000);
                lbl_geri_sayim.setFont(new Font("Serif", Font.PLAIN, 16));
                lbl_geri_sayim.setText("2");
                Thread.sleep(1000);
                lbl_geri_sayim.setFont(new Font("Serif", Font.PLAIN, 18));
                lbl_geri_sayim.setText("1");
                Thread.sleep(1000);
                switchPanel(gamePanel);
                Thread.currentThread().join(10);

            } catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

    }

    private void switchPanel(JPanel panel) {
        jLayeredPane1.removeAll();
        jLayeredPane1.add(panel);
        jLayeredPane1.repaint();
        jLayeredPane1.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        profileFrame = new javax.swing.JFrame();
        jFrame1 = new javax.swing.JFrame();
        colorButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        gamePanel = new javax.swing.JPanel();
        lbl_end_text = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lbl01 = new javax.swing.JLabel();
        lbl48 = new javax.swing.JLabel();
        lbl46 = new javax.swing.JLabel();
        lbl45 = new javax.swing.JLabel();
        lbl47 = new javax.swing.JLabel();
        lbl44 = new javax.swing.JLabel();
        lbl40 = new javax.swing.JLabel();
        lbl38 = new javax.swing.JLabel();
        lbl37 = new javax.swing.JLabel();
        lbl39 = new javax.swing.JLabel();
        lbl43 = new javax.swing.JLabel();
        lbl42 = new javax.swing.JLabel();
        lbl41 = new javax.swing.JLabel();
        lbl02 = new javax.swing.JLabel();
        lbl03 = new javax.swing.JLabel();
        lbl04 = new javax.swing.JLabel();
        lbl05 = new javax.swing.JLabel();
        lbl06 = new javax.swing.JLabel();
        lbl08 = new javax.swing.JLabel();
        lbl07 = new javax.swing.JLabel();
        lbl09 = new javax.swing.JLabel();
        lbl36 = new javax.swing.JLabel();
        lbl35 = new javax.swing.JLabel();
        lbl34 = new javax.swing.JLabel();
        lbl33 = new javax.swing.JLabel();
        lbl29 = new javax.swing.JLabel();
        lbl31 = new javax.swing.JLabel();
        lbl32 = new javax.swing.JLabel();
        lbl30 = new javax.swing.JLabel();
        lbl11 = new javax.swing.JLabel();
        lbl12 = new javax.swing.JLabel();
        lbl13 = new javax.swing.JLabel();
        lbl10 = new javax.swing.JLabel();
        lbl17 = new javax.swing.JLabel();
        lbl16 = new javax.swing.JLabel();
        lbl14 = new javax.swing.JLabel();
        lbl15 = new javax.swing.JLabel();
        lbl28 = new javax.swing.JLabel();
        lbl27 = new javax.swing.JLabel();
        lbl26 = new javax.swing.JLabel();
        lbl25 = new javax.swing.JLabel();
        lbl18 = new javax.swing.JLabel();
        lbl19 = new javax.swing.JLabel();
        lbl20 = new javax.swing.JLabel();
        lbl21 = new javax.swing.JLabel();
        lbl23 = new javax.swing.JLabel();
        lbl24 = new javax.swing.JLabel();
        lbl22 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        lbl77 = new javax.swing.JLabel();
        lbl78 = new javax.swing.JLabel();
        lbl79 = new javax.swing.JLabel();
        lbl80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        btn_dice = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        lblplayer1 = new javax.swing.JLabel();
        lblplayer2 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        lbl_dice = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        profilePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jLabel4 = new javax.swing.JLabel();
        txt_name = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        matchingPanel = new javax.swing.JPanel();
        lbl_esleme_araniyor = new javax.swing.JLabel();
        lbl_loading = new javax.swing.JLabel();
        lbl_geri_sayim = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        btn_red_bg = new javax.swing.JToggleButton();
        btn_dark_bg = new javax.swing.JToggleButton();
        btn_blue_bg = new javax.swing.JToggleButton();
        btn_white_bg = new javax.swing.JToggleButton();
        btn_menu = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();

        javax.swing.GroupLayout profileFrameLayout = new javax.swing.GroupLayout(profileFrame.getContentPane());
        profileFrame.getContentPane().setLayout(profileFrameLayout);
        profileFrameLayout.setHorizontalGroup(
            profileFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        );
        profileFrameLayout.setVerticalGroup(
            profileFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        );

        jFrame1.setBackground(new java.awt.Color(255, 204, 0));
        jFrame1.setForeground(new java.awt.Color(153, 255, 102));
        jFrame1.setMinimumSize(new java.awt.Dimension(550, 530));
        jFrame1.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLayeredPane1.setPreferredSize(new java.awt.Dimension(572, 560));
        jLayeredPane1.setLayout(new java.awt.CardLayout());

        gamePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl_end_text.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        lbl_end_text.setForeground(new java.awt.Color(51, 255, 51));
        lbl_end_text.setEnabled(false);
        gamePanel.add(lbl_end_text, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, -90, 520, 200));

        jLabel5.setBackground(new java.awt.Color(37, 81, 238));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blueColor.png"))); // NOI18N
        jLabel5.setText("a");
        gamePanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(51, 11, 35, 35));

        jLabel6.setBackground(new java.awt.Color(37, 81, 238));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blueColor.png"))); // NOI18N
        jLabel6.setText("a");
        gamePanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 35, 35));

        jLabel7.setBackground(new java.awt.Color(37, 81, 238));
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blueColor.png"))); // NOI18N
        jLabel7.setText("a");
        gamePanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(51, 52, 35, 35));

        jLabel8.setBackground(new java.awt.Color(37, 81, 238));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blueColor.png"))); // NOI18N
        jLabel8.setText("a");
        gamePanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 52, 35, 35));

        jLabel9.setBackground(new java.awt.Color(37, 81, 238));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/greenColor.png"))); // NOI18N
        jLabel9.setText("a");
        gamePanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 451, 35, 35));

        jLabel10.setBackground(new java.awt.Color(37, 81, 238));
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/greenColor.png"))); // NOI18N
        jLabel10.setText("a");
        gamePanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 451, 35, 35));

        jLabel11.setBackground(new java.awt.Color(37, 81, 238));
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/greenColor.png"))); // NOI18N
        jLabel11.setText("a");
        gamePanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 491, 35, 35));

        jLabel12.setBackground(new java.awt.Color(37, 81, 238));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/greenColor.png"))); // NOI18N
        jLabel12.setText("a");
        gamePanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 491, 35, 35));

        jLabel13.setBackground(new java.awt.Color(37, 81, 238));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/yellowColor.png"))); // NOI18N
        jLabel13.setText("a");
        gamePanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 451, 35, 35));

        jLabel14.setBackground(new java.awt.Color(37, 81, 238));
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/yellowColor.png"))); // NOI18N
        jLabel14.setText("a");
        gamePanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 451, 35, 35));

        jLabel15.setBackground(new java.awt.Color(37, 81, 238));
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/yellowColor.png"))); // NOI18N
        jLabel15.setText("a");
        gamePanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 491, 35, 35));

        jLabel16.setBackground(new java.awt.Color(37, 81, 238));
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/yellowColor.png"))); // NOI18N
        jLabel16.setText("a");
        gamePanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 491, 35, 35));

        jLabel17.setBackground(new java.awt.Color(37, 81, 238));
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redColor.png"))); // NOI18N
        jLabel17.setText("a");
        gamePanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 10, 35, 35));

        jLabel18.setBackground(new java.awt.Color(37, 81, 238));
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redColor.png"))); // NOI18N
        jLabel18.setText("a");
        gamePanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 10, 35, 35));

        jLabel19.setBackground(new java.awt.Color(37, 81, 238));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redColor.png"))); // NOI18N
        jLabel19.setText("a");
        gamePanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 50, 35, 35));

        jLabel20.setBackground(new java.awt.Color(37, 81, 238));
        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redColor.png"))); // NOI18N
        jLabel20.setText("a");
        gamePanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 50, 35, 35));

        lbl01.setBackground(new java.awt.Color(37, 81, 238));
        lbl01.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl01.setText("a");
        gamePanel.add(lbl01, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 491, 35, 35));

        lbl48.setBackground(new java.awt.Color(37, 81, 238));
        lbl48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl48.setText("a");
        gamePanel.add(lbl48, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 491, 35, 35));

        lbl46.setBackground(new java.awt.Color(37, 81, 238));
        lbl46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl46.setText("a");
        gamePanel.add(lbl46, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 491, 35, 35));

        lbl45.setBackground(new java.awt.Color(37, 81, 238));
        lbl45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl45.setText("a");
        gamePanel.add(lbl45, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 491, 35, 35));

        lbl47.setBackground(new java.awt.Color(37, 81, 238));
        lbl47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl47.setText("a");
        gamePanel.add(lbl47, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 491, 35, 35));

        lbl44.setBackground(new java.awt.Color(37, 81, 238));
        lbl44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl44.setText("a");
        gamePanel.add(lbl44, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 451, 35, 35));

        lbl40.setBackground(new java.awt.Color(250, 128, 114));
        lbl40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl40.setText("a");
        gamePanel.add(lbl40, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 331, 35, 35));

        lbl38.setBackground(new java.awt.Color(250, 128, 114));
        lbl38.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl38.setText("a");
        gamePanel.add(lbl38, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 331, 35, 35));

        lbl37.setBackground(new java.awt.Color(250, 128, 114));
        lbl37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl37.setText("a");
        gamePanel.add(lbl37, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 331, 35, 35));

        lbl39.setBackground(new java.awt.Color(250, 128, 114));
        lbl39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl39.setText("a");
        gamePanel.add(lbl39, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 331, 35, 35));

        lbl43.setBackground(new java.awt.Color(37, 81, 238));
        lbl43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl43.setText("a");
        gamePanel.add(lbl43, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 411, 35, 35));

        lbl42.setBackground(new java.awt.Color(37, 81, 238));
        lbl42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl42.setText("a");
        gamePanel.add(lbl42, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 371, 35, 35));

        lbl41.setBackground(new java.awt.Color(250, 128, 114));
        lbl41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl41.setText("a");
        gamePanel.add(lbl41, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 331, 35, 35));

        lbl02.setBackground(new java.awt.Color(37, 81, 238));
        lbl02.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl02.setText("a");
        gamePanel.add(lbl02, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 451, 35, 35));

        lbl03.setBackground(new java.awt.Color(37, 81, 238));
        lbl03.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl03.setText("a");
        gamePanel.add(lbl03, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 411, 35, 35));

        lbl04.setBackground(new java.awt.Color(37, 81, 238));
        lbl04.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl04.setText("a");
        gamePanel.add(lbl04, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 371, 35, 35));

        lbl05.setBackground(new java.awt.Color(37, 81, 238));
        lbl05.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl05.setText("a");
        gamePanel.add(lbl05, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 331, 35, 35));

        lbl06.setBackground(new java.awt.Color(37, 81, 238));
        lbl06.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl06.setText("a");
        gamePanel.add(lbl06, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 331, 35, 35));

        lbl08.setBackground(new java.awt.Color(37, 81, 238));
        lbl08.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl08.setText("a");
        gamePanel.add(lbl08, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 331, 35, 35));

        lbl07.setBackground(new java.awt.Color(37, 81, 238));
        lbl07.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl07.setText("a");
        gamePanel.add(lbl07, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 331, 35, 35));

        lbl09.setBackground(new java.awt.Color(37, 81, 238));
        lbl09.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl09.setText("a");
        gamePanel.add(lbl09, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 331, 35, 35));

        lbl36.setBackground(new java.awt.Color(250, 128, 114));
        lbl36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl36.setText("a");
        gamePanel.add(lbl36, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 290, 35, 35));

        lbl35.setBackground(new java.awt.Color(250, 128, 114));
        lbl35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl35.setText("a");
        gamePanel.add(lbl35, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 250, 35, 35));

        lbl34.setBackground(new java.awt.Color(250, 128, 114));
        lbl34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl34.setText("a");
        gamePanel.add(lbl34, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 210, 35, 35));

        lbl33.setBackground(new java.awt.Color(250, 128, 114));
        lbl33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl33.setText("a");
        gamePanel.add(lbl33, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 170, 35, 35));

        lbl29.setBackground(new java.awt.Color(250, 128, 114));
        lbl29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl29.setText("a");
        gamePanel.add(lbl29, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 170, 35, 35));

        lbl31.setBackground(new java.awt.Color(250, 128, 114));
        lbl31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl31.setText("a");
        gamePanel.add(lbl31, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 170, 35, 35));

        lbl32.setBackground(new java.awt.Color(250, 128, 114));
        lbl32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl32.setText("a");
        gamePanel.add(lbl32, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 170, 35, 35));

        lbl30.setBackground(new java.awt.Color(250, 128, 114));
        lbl30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl30.setText("a");
        gamePanel.add(lbl30, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 170, 35, 35));

        lbl11.setBackground(new java.awt.Color(37, 81, 238));
        lbl11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl11.setText("a");
        gamePanel.add(lbl11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, 35, 35));

        lbl12.setBackground(new java.awt.Color(37, 81, 238));
        lbl12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl12.setText("a");
        gamePanel.add(lbl12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 35, 35));

        lbl13.setBackground(new java.awt.Color(37, 81, 238));
        lbl13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl13.setText("a");
        gamePanel.add(lbl13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 35, 35));

        lbl10.setBackground(new java.awt.Color(37, 81, 238));
        lbl10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl10.setText("a");
        gamePanel.add(lbl10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 35, 35));

        lbl17.setBackground(new java.awt.Color(37, 81, 238));
        lbl17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl17.setText("a");
        gamePanel.add(lbl17, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 170, 35, 35));

        lbl16.setBackground(new java.awt.Color(37, 81, 238));
        lbl16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl16.setText("a");
        gamePanel.add(lbl16, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 170, 35, 35));

        lbl14.setBackground(new java.awt.Color(37, 81, 238));
        lbl14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl14.setText("a");
        gamePanel.add(lbl14, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 35, 35));

        lbl15.setBackground(new java.awt.Color(37, 81, 238));
        lbl15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl15.setText("a");
        gamePanel.add(lbl15, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 35, 35));

        lbl28.setBackground(new java.awt.Color(37, 81, 238));
        lbl28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl28.setText("a");
        gamePanel.add(lbl28, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 130, 35, 35));

        lbl27.setBackground(new java.awt.Color(37, 81, 238));
        lbl27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl27.setText("a");
        gamePanel.add(lbl27, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, 35, 35));

        lbl26.setBackground(new java.awt.Color(37, 81, 238));
        lbl26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl26.setText("a");
        gamePanel.add(lbl26, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, 35, 35));

        lbl25.setBackground(new java.awt.Color(37, 81, 238));
        lbl25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl25.setText("a");
        gamePanel.add(lbl25, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, 35, 35));

        lbl18.setBackground(new java.awt.Color(37, 81, 238));
        lbl18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl18.setText("a");
        gamePanel.add(lbl18, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 130, 35, 35));

        lbl19.setBackground(new java.awt.Color(37, 81, 238));
        lbl19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl19.setText("a");
        gamePanel.add(lbl19, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 35, 35));

        lbl20.setBackground(new java.awt.Color(37, 81, 238));
        lbl20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl20.setText("a");
        gamePanel.add(lbl20, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 50, 35, 35));

        lbl21.setBackground(new java.awt.Color(37, 81, 238));
        lbl21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl21.setText("a");
        gamePanel.add(lbl21, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, 35, 35));

        lbl23.setBackground(new java.awt.Color(37, 81, 238));
        lbl23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl23.setText("a");
        gamePanel.add(lbl23, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, 35, 35));

        lbl24.setBackground(new java.awt.Color(37, 81, 238));
        lbl24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl24.setText("a");
        gamePanel.add(lbl24, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 10, 35, 35));

        lbl22.setBackground(new java.awt.Color(37, 81, 238));
        lbl22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl22.setText("a");
        gamePanel.add(lbl22, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 35, 35));

        jLabel74.setBackground(new java.awt.Color(250, 128, 114));
        jLabel74.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel74.setText("a");
        gamePanel.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 170, 35, 35));

        jLabel75.setBackground(new java.awt.Color(250, 128, 114));
        jLabel75.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel75.setText("a");
        gamePanel.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 170, 35, 35));

        jLabel76.setBackground(new java.awt.Color(250, 128, 114));
        jLabel76.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel76.setText("a");
        gamePanel.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 170, 35, 35));

        lbl77.setBackground(new java.awt.Color(37, 81, 238));
        lbl77.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl77.setText("a");
        gamePanel.add(lbl77, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 451, 35, 35));

        lbl78.setBackground(new java.awt.Color(37, 81, 238));
        lbl78.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl78.setText("a");
        gamePanel.add(lbl78, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 411, 35, 35));

        lbl79.setBackground(new java.awt.Color(37, 81, 238));
        lbl79.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl79.setText("a");
        gamePanel.add(lbl79, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 371, 35, 35));

        lbl80.setBackground(new java.awt.Color(37, 81, 238));
        lbl80.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        lbl80.setText("a");
        gamePanel.add(lbl80, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 331, 35, 35));

        jLabel81.setBackground(new java.awt.Color(37, 81, 238));
        jLabel81.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel81.setText("a");
        gamePanel.add(jLabel81, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 50, 35, 35));

        jLabel82.setBackground(new java.awt.Color(37, 81, 238));
        jLabel82.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel82.setText("a");
        gamePanel.add(jLabel82, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 90, 35, 35));

        jLabel83.setBackground(new java.awt.Color(37, 81, 238));
        jLabel83.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel83.setText("a");
        gamePanel.add(jLabel83, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 130, 35, 35));

        jLabel84.setBackground(new java.awt.Color(37, 81, 238));
        jLabel84.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel84.setText("a");
        gamePanel.add(jLabel84, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 170, 35, 35));

        jLabel85.setBackground(new java.awt.Color(250, 128, 114));
        jLabel85.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel85.setText("a");
        gamePanel.add(jLabel85, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 250, 35, 35));

        jLabel86.setBackground(new java.awt.Color(250, 128, 114));
        jLabel86.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel86.setText("a");
        gamePanel.add(jLabel86, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 250, 35, 35));

        jLabel87.setBackground(new java.awt.Color(250, 128, 114));
        jLabel87.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel87.setText("a");
        gamePanel.add(jLabel87, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 250, 35, 35));

        jLabel88.setBackground(new java.awt.Color(250, 128, 114));
        jLabel88.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel88.setText("a");
        gamePanel.add(jLabel88, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 250, 35, 35));

        jLabel95.setBackground(new java.awt.Color(37, 81, 238));
        jLabel95.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel95.setText("a");
        gamePanel.add(jLabel95, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 250, 35, 35));

        jLabel96.setBackground(new java.awt.Color(37, 81, 238));
        jLabel96.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel96.setText("a");
        gamePanel.add(jLabel96, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 250, 35, 35));

        jLabel97.setBackground(new java.awt.Color(37, 81, 238));
        jLabel97.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel97.setText("a");
        gamePanel.add(jLabel97, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 250, 35, 35));

        jLabel98.setBackground(new java.awt.Color(37, 81, 238));
        jLabel98.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/salmonColor.png"))); // NOI18N
        jLabel98.setText("a");
        gamePanel.add(jLabel98, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 250, 35, 35));

        btn_dice.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btn_dice.setText("Dice");
        btn_dice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_diceActionPerformed(evt);
            }
        });
        gamePanel.add(btn_dice, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 280, 110, 50));

        jLabel23.setText("You:");
        gamePanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 120, -1, -1));

        jLabel24.setText("Rival:");
        gamePanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 140, -1, -1));

        lblplayer1.setText("-1");
        gamePanel.add(lblplayer1, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 120, -1, -1));

        lblplayer2.setText("-1");
        gamePanel.add(lblplayer2, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 140, -1, -1));

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel25.setText("↑");
        gamePanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 480, 50, 50));

        jLabel26.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel26.setText("→");
        gamePanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 50, 30));

        jLabel27.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel27.setText("↓");
        gamePanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 0, 50, 50));

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel28.setText("←");
        gamePanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 350, 50, 50));

        lbl_dice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gamePanel.add(lbl_dice, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 210, 64, 64));

        jLayeredPane1.add(gamePanel, "card3");

        jLabel2.setFont(new java.awt.Font("Franklin Gothic Medium", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(230, 144, 41));
        jLabel2.setText("Kızma Birader");

        jButton1.setBackground(new java.awt.Color(255, 160, 70));
        jButton1.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 14)); // NOI18N
        jButton1.setText("New Game");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 160, 70));
        jButton2.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 14)); // NOI18N
        jButton2.setText("Profile");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 160, 70));
        jButton3.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 14)); // NOI18N
        jButton3.setText("Settings");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jLabel2))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(220, 220, 220)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(220, 220, 220)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(220, 220, 220)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(169, 169, 169))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(189, Short.MAX_VALUE))
        );

        jLayeredPane1.add(mainPanel, "card11");
        mainPanel.getAccessibleContext().setAccessibleParent(mainPanel);

        profilePanel.setMinimumSize(new java.awt.Dimension(572, 560));
        profilePanel.setName(""); // NOI18N

        jLabel1.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 160, 70));
        jLabel1.setText("Profile");

        jLabel3.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 160, 70));
        jLabel3.setText("Choose Color");

        jToggleButton4.setBackground(new java.awt.Color(240, 248, 51));
        colorButtonGroup.add(jToggleButton4);
        jToggleButton4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton4.setText("Yellow");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jToggleButton5.setBackground(new java.awt.Color(37, 81, 238));
        colorButtonGroup.add(jToggleButton5);
        jToggleButton5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton5.setText("Blue");
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        jToggleButton6.setBackground(new java.awt.Color(66, 229, 30));
        colorButtonGroup.add(jToggleButton6);
        jToggleButton6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton6.setText("Green");
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        jToggleButton7.setBackground(new java.awt.Color(255, 42, 25));
        colorButtonGroup.add(jToggleButton7);
        jToggleButton7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jToggleButton7.setSelected(true);
        jToggleButton7.setText("Red");
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 160, 70));
        jLabel4.setText("Username");

        txt_name.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 18)); // NOI18N
        txt_name.setText("Noname");

        jButton4.setBackground(new java.awt.Color(255, 160, 70));
        jButton4.setFont(new java.awt.Font("Franklin Gothic Medium", 0, 18)); // NOI18N
        jButton4.setText("Main Menu");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout profilePanelLayout = new javax.swing.GroupLayout(profilePanel);
        profilePanel.setLayout(profilePanelLayout);
        profilePanelLayout.setHorizontalGroup(
            profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilePanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(profilePanelLayout.createSequentialGroup()
                        .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txt_name, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4)))
                        .addContainerGap(71, Short.MAX_VALUE))
                    .addGroup(profilePanelLayout.createSequentialGroup()
                        .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(profilePanelLayout.createSequentialGroup()
                                .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        profilePanelLayout.setVerticalGroup(
            profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilePanelLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jLabel1)
                .addGap(52, 52, 52)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(txt_name, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(81, 81, 81))
        );

        jLayeredPane1.add(profilePanel, "card2");

        lbl_esleme_araniyor.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        lbl_esleme_araniyor.setText("Eşleşme Araniyor");

        lbl_loading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_loading.setText("Loading");

        lbl_geri_sayim.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_geri_sayim.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout matchingPanelLayout = new javax.swing.GroupLayout(matchingPanel);
        matchingPanel.setLayout(matchingPanelLayout);
        matchingPanelLayout.setHorizontalGroup(
            matchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(matchingPanelLayout.createSequentialGroup()
                .addGap(150, 150, 150)
                .addGroup(matchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbl_loading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_esleme_araniyor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_geri_sayim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(150, Short.MAX_VALUE))
        );
        matchingPanelLayout.setVerticalGroup(
            matchingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(matchingPanelLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(lbl_esleme_araniyor, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_loading)
                .addGap(77, 77, 77)
                .addComponent(lbl_geri_sayim, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(306, Short.MAX_VALUE))
        );

        jLayeredPane1.add(matchingPanel, "card5");

        settingsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btn_red_bg.setBackground(new java.awt.Color(255, 153, 153));
        buttonGroup1.add(btn_red_bg);
        btn_red_bg.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_red_bg.setText("Red");
        btn_red_bg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_red_bgActionPerformed(evt);
            }
        });
        settingsPanel.add(btn_red_bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 150, 160, 60));

        btn_dark_bg.setBackground(new java.awt.Color(102, 102, 102));
        buttonGroup1.add(btn_dark_bg);
        btn_dark_bg.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_dark_bg.setText("Dark");
        btn_dark_bg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dark_bgActionPerformed(evt);
            }
        });
        settingsPanel.add(btn_dark_bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 160, 60));

        btn_blue_bg.setBackground(new java.awt.Color(102, 153, 255));
        buttonGroup1.add(btn_blue_bg);
        btn_blue_bg.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_blue_bg.setText("Blue");
        btn_blue_bg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_blue_bgActionPerformed(evt);
            }
        });
        settingsPanel.add(btn_blue_bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 220, 160, 60));

        btn_white_bg.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(btn_white_bg);
        btn_white_bg.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_white_bg.setText("White");
        btn_white_bg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_white_bgActionPerformed(evt);
            }
        });
        settingsPanel.add(btn_white_bg, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 220, 160, 60));

        btn_menu.setBackground(new java.awt.Color(255, 153, 0));
        btn_menu.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btn_menu.setText("Main Menu");
        btn_menu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_menuActionPerformed(evt);
            }
        });
        settingsPanel.add(btn_menu, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, -1, 50));

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 153, 51));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Background Color");
        settingsPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 50, 320, -1));

        jLayeredPane1.add(settingsPanel, "card6");

        getContentPane().add(jLayeredPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        myselection = 1;
        switchPanel(matchingPanel);
        Client.Start("127.0.0.1", 2000);


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        switchPanel(mainPanel);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        switchPanel(profilePanel);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btn_diceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_diceActionPerformed
        //Rastgele bir sayı oluşturuluyor ve zar animasyonu başlatılıyor.
        int number = (int) (Math.random() * 6 + 1);
        DiceAnimation diceAnimation = new DiceAnimation(number);
        Thread thread = new Thread(diceAnimation);
        thread.start();

    }//GEN-LAST:event_btn_diceActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Client.Stop();
    }//GEN-LAST:event_formWindowClosing

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        color = color.Red;
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        color = color.Green;
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        color = color.Blue;
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        color = color.Yellow;
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void btn_menuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_menuActionPerformed
       switchPanel(mainPanel);
    }//GEN-LAST:event_btn_menuActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        switchPanel(settingsPanel);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btn_dark_bgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dark_bgActionPerformed
        this.getContentPane().setBackground(Color.DARK_GRAY);
    }//GEN-LAST:event_btn_dark_bgActionPerformed

    private void btn_red_bgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_red_bgActionPerformed
        this.getContentPane().setBackground(Color.RED);
    }//GEN-LAST:event_btn_red_bgActionPerformed

    private void btn_white_bgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_white_bgActionPerformed
       this.getContentPane().setBackground(Color.WHITE);
    }//GEN-LAST:event_btn_white_bgActionPerformed

    private void btn_blue_bgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_blue_bgActionPerformed
        this.getContentPane().setBackground(Color.blue);
    }//GEN-LAST:event_btn_blue_bgActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
    
    /*
    * Oyun içinde eğer yeni piyon oluturulacaksa veya birden
    * fazla piyon olduğunda oynanması istenilen piyon için
    * gösterien seçenek ekranının sonucu bu metot ile değerlendiriliyor.
    */
    private void updateSelection(String s, int number) {
        switch (s) {
            case "New Pion":
                Pion p = new Pion(this.color);
                p.pion_arrived = false;
                p.pionIndex = Pion.start_finish[p.pionType][0];
                p.pionNumber = pionCount;
                pionList[pionCount] = p;
                updateMyPion(pionCount, pionList[pionCount].pionIndex, 0);
                pionCount++;
                break;
            case "1. Pion":
                updateMyPion(0, pionList[0].pionIndex, number);
                break;
            case "2. Pion":
                updateMyPion(1, pionList[1].pionIndex, number);
                break;
            case "3. Pion":
                updateMyPion(2, pionList[2].pionIndex, number);
                break;
            case "4. Pion":
                updateMyPion(3, pionList[3].pionIndex, number);
                break;
            default:
                System.out.println("Error");
                break;
        }
    }

    /*
    * Oyun başlarken ekrandaki label bileşenleri
    * bu metot ile bir diziye atılıyor.
    */
    private void loadAreatoArray() {
        area[0] = lbl01;
        area[1] = lbl02;
        area[2] = lbl03;
        area[3] = lbl04;
        area[4] = lbl05;
        area[5] = lbl06;
        area[6] = lbl07;
        area[7] = lbl08;
        area[8] = lbl09;
        area[9] = lbl10;
        area[10] = lbl11;
        area[11] = lbl12;
        area[12] = lbl13;
        area[13] = lbl14;
        area[14] = lbl15;
        area[15] = lbl16;
        area[16] = lbl17;
        area[17] = lbl18;
        area[18] = lbl19;
        area[19] = lbl20;
        area[20] = lbl21;
        area[21] = lbl22;
        area[22] = lbl23;
        area[23] = lbl24;
        area[24] = lbl25;
        area[25] = lbl26;
        area[26] = lbl27;
        area[27] = lbl28;
        area[28] = lbl29;
        area[29] = lbl30;
        area[30] = lbl31;
        area[31] = lbl32;
        area[32] = lbl33;
        area[33] = lbl34;
        area[34] = lbl35;
        area[35] = lbl36;
        area[36] = lbl37;
        area[37] = lbl38;
        area[38] = lbl39;
        area[39] = lbl40;
        area[40] = lbl41;
        area[41] = lbl42;
        area[42] = lbl43;
        area[43] = lbl44;
        area[44] = lbl45;
        area[45] = lbl46;
        area[46] = lbl47;
        area[47] = lbl48;

        finish_area[0][0] = lbl77;
        finish_area[0][1] = lbl78;
        finish_area[0][2] = lbl79;
        finish_area[0][3] = lbl80;

        finish_area[1][0] = jLabel95;
        finish_area[1][1] = jLabel96;
        finish_area[1][2] = jLabel97;
        finish_area[1][3] = jLabel98;

        finish_area[2][0] = jLabel81;
        finish_area[2][1] = jLabel82;
        finish_area[2][2] = jLabel83;
        finish_area[2][3] = jLabel84;

        finish_area[3][0] = jLabel85;
        finish_area[3][1] = jLabel86;
        finish_area[3][2] = jLabel87;
        finish_area[3][3] = jLabel88;
    }

    /*
    * Oyuncu kendi piyonun olduğu hücreye başka bir piyon
    * koyamaz. Bu kontrolu sağlayan metot
    */
    private int checkAvailableCoordinates(int number) {
        availablePions[0] = true;
        availablePions[1] = true;
        availablePions[2] = true;
        availablePions[3] = true;
        for (int i = 0; i < pionCount; i++) {
            for (int j = 0; j < pionCount; j++) {
                if (pionList[i].pionIndex + number == pionList[j].pionIndex) {
                    availablePions[i] = false;
                }
            }
        }
        int sayac = 0;
        for (int i = 0; i < pionCount; i++) {
            if (!availablePions[i]) {
                sayac++;
            }
        }
        return sayac;
    }

    /*
    * Rakibin piyonlarının konumunu güncelleyen metot
    */
    public void updateRivalPions() {
        for (Pion p : rival_pion_list) {
            System.out.println("rival updating");
            if (p != null && !p.pion_arrived) {
                System.out.println(p.pionIndex);
                area[p.pionIndex % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[p.pionType])));
            }
        }
    }

    /*
    * Oyuncunun piyonlarının koordinatlarını düzelten metot.
    */
    public void updateMyPions() {
        for (Pion p : pionList) {
            if (p != null && !p.pion_arrived) {
                area[p.pionIndex % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[p.pionType])));
            }
        }
    }

    /*
    * Piyonların hareket ettiği bölgeyi güncelleyen metot.
    */
    public void updateMap() {
        for (JLabel area1 : area) {
            area1.setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
        }
        updateMyPions();
        updateRivalPions();
        updateFinishedArea();
    }

    /*
    * Piyon hamlesinden sonra oyunun kazanılıp kazanılmadığını 
    * kontrol eden metot.
    */
    private void checkWinner() {
        int counter = 0;
        for (Pion pionList1 : pionList) {
            if (pionList1 != null && pionList1.pion_arrived) {
                counter++;
            }
        }
        if (counter == 4) {
            Message m = new Message(Message.Message_Type.Bitis);
            Client.Send(m);
            btn_dice.setEnabled(false);
            lbl_end_text.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/kazandin.png")));
            lbl_end_text.setVisible(true);
            Client.Stop();
        }
    }

    /*
    * Bitis bölgesini güncellyen metot.
    */
    private void updateFinishedArea() {
        for (int i = 0; i < finish_area.length; i++) {
            if (pionList[i] != null && pionList[i].pion_arrived) {
                finish_area[pionList[i].pionType][i].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[i].pionType])));
            }
            if (rival_pion_list[i] != null && rival_pion_list[i].pion_arrived) {
                finish_area[rival_pion_list[i].pionType][i].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[rival_pion_list[i].pionType])));
            }
        }

    }

    //Pion Animations
    private void updateMyPion(int pionNumber, int src, int dst) {
        pionAnimation = new PionAnimationThread(pionNumber, src, dst);
        new Thread(pionAnimation).start();
        //updateCoordinates(pionNumber, src, dst);
        //updateMap();

    }

    /*
    * Rakip piyon ile kesişim olup olmadığının kontrolü
    */
    public void checkIntersections() {
        for (int i = 0; i < pionList.length; i++) {
            for (int j = i; j < rival_pion_list.length; j++) {
                if (pionList[i] != null && rival_pion_list[j] != null) {
                    if (pionList[i].pionIndex == rival_pion_list[j].pionIndex) {
                        pionList[i] = null;
                        pionCount--;
                    }
                }
            }
        }

        for (int i = 0; i < pionList.length - 1; i++) {
            if (pionList[i] == null && pionList[i + 1] != null) {
                pionList[i] = pionList[i + 1];
                pionList[i + 1] = null;
            }
        }

        for (int i = 0; i < rival_pion_list.length - 1; i++) {
            if (rival_pion_list[i] == null && rival_pion_list[i + 1] != null) {
                rival_pion_list[i] = rival_pion_list[i + 1];
                rival_pion_list[i + 1] = null;
            }
        }
        updateMap();
    }

    /*
    * Piyonu ilerleten metot
    */
    private void updateCoordinates(int pionNumber, int src, int dst) {
        if (Pion.start_finish[pionList[pionNumber].pionType][1] > Pion.start_finish[pionList[pionNumber].pionType][0]) {
            if (src + dst > Pion.start_finish[pionList[pionNumber].pionType][1]) {
                area[src].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
                finish_area[pionList[pionNumber].pionType][finished_pions_count++].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
                pionList[pionNumber].pion_arrived = true;
            } else {

                area[src].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
                area[src + dst].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
                pionList[pionNumber].pionIndex = src + dst;
            }
        } else {
            if (src % 48 < Pion.start_finish[pionList[pionNumber].pionType][1]) {
                if ((src + dst) % 48 < Pion.start_finish[pionList[pionNumber].pionType][1]) {
                    pionList[pionNumber].pionIndex = src + dst;
                    area[src % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
                    area[(src + dst) % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
                } else {
                    area[src % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
                    finish_area[pionList[pionNumber].pionType][finished_pions_count++].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
                    pionList[pionNumber].pion_arrived = true;
                    //finish_area[pionList[pionNumber].pionType][dst - (Pion.start_finish[pionList[pionNumber].pionType][1] - src % 48)].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
                }

            } else {
                pionList[pionNumber].pionIndex = src + dst;
                area[src % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[4])));
                area[(src + dst) % 48].setIcon(new javax.swing.ImageIcon(getClass().getResource(colorPaths[pionList[pionNumber].pionType])));
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btn_blue_bg;
    private javax.swing.JToggleButton btn_dark_bg;
    public static javax.swing.JButton btn_dice;
    private javax.swing.JButton btn_menu;
    private javax.swing.JToggleButton btn_red_bg;
    private javax.swing.JToggleButton btn_white_bg;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup colorButtonGroup;
    private javax.swing.JPanel gamePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JLabel lbl01;
    private javax.swing.JLabel lbl02;
    private javax.swing.JLabel lbl03;
    private javax.swing.JLabel lbl04;
    private javax.swing.JLabel lbl05;
    private javax.swing.JLabel lbl06;
    private javax.swing.JLabel lbl07;
    private javax.swing.JLabel lbl08;
    private javax.swing.JLabel lbl09;
    private javax.swing.JLabel lbl10;
    private javax.swing.JLabel lbl11;
    private javax.swing.JLabel lbl12;
    private javax.swing.JLabel lbl13;
    private javax.swing.JLabel lbl14;
    private javax.swing.JLabel lbl15;
    private javax.swing.JLabel lbl16;
    private javax.swing.JLabel lbl17;
    private javax.swing.JLabel lbl18;
    private javax.swing.JLabel lbl19;
    private javax.swing.JLabel lbl20;
    private javax.swing.JLabel lbl21;
    private javax.swing.JLabel lbl22;
    private javax.swing.JLabel lbl23;
    private javax.swing.JLabel lbl24;
    private javax.swing.JLabel lbl25;
    private javax.swing.JLabel lbl26;
    private javax.swing.JLabel lbl27;
    private javax.swing.JLabel lbl28;
    private javax.swing.JLabel lbl29;
    private javax.swing.JLabel lbl30;
    private javax.swing.JLabel lbl31;
    private javax.swing.JLabel lbl32;
    private javax.swing.JLabel lbl33;
    private javax.swing.JLabel lbl34;
    private javax.swing.JLabel lbl35;
    private javax.swing.JLabel lbl36;
    private javax.swing.JLabel lbl37;
    private javax.swing.JLabel lbl38;
    private javax.swing.JLabel lbl39;
    private javax.swing.JLabel lbl40;
    private javax.swing.JLabel lbl41;
    private javax.swing.JLabel lbl42;
    private javax.swing.JLabel lbl43;
    private javax.swing.JLabel lbl44;
    private javax.swing.JLabel lbl45;
    private javax.swing.JLabel lbl46;
    private javax.swing.JLabel lbl47;
    private javax.swing.JLabel lbl48;
    private javax.swing.JLabel lbl77;
    private javax.swing.JLabel lbl78;
    private javax.swing.JLabel lbl79;
    private javax.swing.JLabel lbl80;
    private javax.swing.JLabel lbl_dice;
    public static javax.swing.JLabel lbl_end_text;
    private javax.swing.JLabel lbl_esleme_araniyor;
    private javax.swing.JLabel lbl_geri_sayim;
    private javax.swing.JLabel lbl_loading;
    public static javax.swing.JLabel lblplayer1;
    public static javax.swing.JLabel lblplayer2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel matchingPanel;
    private javax.swing.JFrame profileFrame;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JPanel settingsPanel;
    public static javax.swing.JTextField txt_name;
    // End of variables declaration//GEN-END:variables
}
