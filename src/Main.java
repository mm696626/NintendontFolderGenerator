// Sonic 3 AIR Music Mod Generator by Matt McCullough
// This is to automate and streamline music mod creation for Sonic 3 AIR

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