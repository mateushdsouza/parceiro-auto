package br.com.parceiroauto.view.swing;

import javax.swing.*;

public class HomeFrame extends JFrame {

    public HomeFrame() {
        setSize(900, 600);
        JLabel lblWelcome = new JLabel("Seja bem vindo");

        add(lblWelcome);
        setVisible(true);
    }
}
