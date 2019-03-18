/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mybasicserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author INSECT
 */
public class MyBasicServerGui extends javax.swing.JFrame {

    /**
     * Creates new form MyBasicServerGui
     */
    MyBasicServer myserver;
    public static DefaultListModel lmodel= new DefaultListModel();
    
    public MyBasicServerGui() {
        
        initComponents();
        lst_receivedmessage.setModel(MyBasicServerGui.lmodel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt_PortNumber = new javax.swing.JTextField();
        btn_SetServer = new javax.swing.JButton();
        btn_StartServer = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txta_message = new javax.swing.JTextArea();
        btn_SendBroadcast = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lst_receivedmessage = new javax.swing.JList<>();
        btn_StopServer = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btn_SetServer.setText("Set Server");
        btn_SetServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SetServerActionPerformed(evt);
            }
        });

        btn_StartServer.setText("Start Server");
        btn_StartServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_StartServerActionPerformed(evt);
            }
        });

        txta_message.setColumns(20);
        txta_message.setRows(5);
        jScrollPane1.setViewportView(txta_message);

        btn_SendBroadcast.setText("Send Broadcast");
        btn_SendBroadcast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SendBroadcastActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(lst_receivedmessage);

        btn_StopServer.setText("Stop Server");
        btn_StopServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_StopServerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_SendBroadcast)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txt_PortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_SetServer)
                            .addComponent(btn_StartServer)
                            .addComponent(btn_StopServer))))
                .addGap(44, 44, 44))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_PortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_SetServer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_StartServer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_StopServer)))
                .addGap(28, 28, 28)
                .addComponent(btn_SendBroadcast)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_SetServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SetServerActionPerformed
        // TODO add your handling code here:
        
        int port = Integer.parseInt(txt_PortNumber.getText());
        this.myserver= new MyBasicServer(port);
    }//GEN-LAST:event_btn_SetServerActionPerformed

    private void btn_StartServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_StartServerActionPerformed
        try {
            // TODO add your handling code here:
            this.myserver.StartServer();
        } catch (IOException ex) {
            Logger.getLogger(MyBasicServerGui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_StartServerActionPerformed

    private void btn_StopServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_StopServerActionPerformed
        try {
            // TODO add your handling code here:

            this.myserver.StopServer();
        } catch (IOException ex) {
            Logger.getLogger(MyBasicServerGui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_StopServerActionPerformed

    private void btn_SendBroadcastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SendBroadcastActionPerformed
        try {
            // TODO add your handling code here:

            myserver.SendBroadcastMessade(txta_message.getText());
        } catch (IOException ex) {
            Logger.getLogger(MyBasicServerGui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn_SendBroadcastActionPerformed

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
            java.util.logging.Logger.getLogger(MyBasicServerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MyBasicServerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyBasicServerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyBasicServerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MyBasicServerGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_SendBroadcast;
    private javax.swing.JButton btn_SetServer;
    private javax.swing.JButton btn_StartServer;
    private javax.swing.JButton btn_StopServer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> lst_receivedmessage;
    private javax.swing.JTextField txt_PortNumber;
    private javax.swing.JTextArea txta_message;
    // End of variables declaration//GEN-END:variables
}
