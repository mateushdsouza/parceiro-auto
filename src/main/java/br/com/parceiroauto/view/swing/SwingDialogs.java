package br.com.parceiroauto.view.swing;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public final class SwingDialogs {
    private SwingDialogs() {
    }

    public static void configurePortugueseDefaults() {
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, defaultFont);
            }
        }
        UIManager.put("Button.font", buttonFont);
        UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 16));
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", buttonFont);

        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");

        UIManager.put("FileChooser.saveButtonText", "Salvar");
        UIManager.put("FileChooser.saveButtonToolTipText", "Salvar arquivo");
        UIManager.put("FileChooser.openButtonText", "Abrir");
        UIManager.put("FileChooser.openButtonToolTipText", "Abrir arquivo");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Cancelar");
        UIManager.put("FileChooser.updateButtonText", "Atualizar");
        UIManager.put("FileChooser.acceptAllFileFilterText", "Todos os arquivos");
        UIManager.put("FileChooser.lookInLabelText", "Procurar em:");
        UIManager.put("FileChooser.fileNameLabelText", "Nome do arquivo:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Tipo de arquivo:");
        UIManager.put("FileChooser.upFolderToolTipText", "Subir um nível");
        UIManager.put("FileChooser.homeFolderToolTipText", "Início");
        UIManager.put("FileChooser.newFolderToolTipText", "Nova pasta");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Lista");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Detalhes");
    }

    static boolean confirmYesNo(Component parent, String message, String title) {
        Object[] options = {"Sim", "Não"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );
        return choice == JOptionPane.YES_OPTION;
    }

    static boolean confirmOkCancel(Component parent, Object message, String title) {
        Object[] options = {"Confirmar", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                message,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        return choice == JOptionPane.OK_OPTION;
    }
}
