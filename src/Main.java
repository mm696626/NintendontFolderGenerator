// Nintendont Folder Generator by Matt McCullough
// This is to automate creating the folder structure for Nintendont

import ui.NintendontFolderGeneratorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        NintendontFolderGeneratorUI nintendontFolderGeneratorUI = new NintendontFolderGeneratorUI();
        nintendontFolderGeneratorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nintendontFolderGeneratorUI.pack();
        nintendontFolderGeneratorUI.setVisible(true);
    }
}