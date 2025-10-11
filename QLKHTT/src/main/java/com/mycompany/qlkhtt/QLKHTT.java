/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.qlkhtt;

import GUI.frm_Main;
import javax.swing.JFrame;

/**
 *
 * @author HAO
 */
public class QLKHTT {
    public static void main(String[] args) {
        frm_Main abc = new frm_Main();

        // Can giua form tren man hinh
        abc.setLocationRelativeTo(null);


        // Dat che do dong form
        abc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Hien thi form
        abc.setVisible(true);
    }
}
