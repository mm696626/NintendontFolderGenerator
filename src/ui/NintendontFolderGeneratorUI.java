package ui;

import io.ISOFilePathSaver;
import io.NintendontFolderGenerator;
import io.OldFileCleaner;
import validation.ExtensionValidator;
import validation.MD5HashValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class NintendontFolderGeneratorUI extends JFrame implements ActionListener {


    //where to grab iso or ciso files from
    private String gameFolderPath = "";
    private JButton generateNintendontGamesFolder, pickGamesFolder, validateGameFilesMD5;

    private ArrayList<File> gameFileList;
    GridBagConstraints gridBagConstraints = null;

    public NintendontFolderGeneratorUI()
    {
        setTitle("Nintendont Folder Generator");

        pickGamesFolder = new JButton("Select Folder with Games");
        pickGamesFolder.addActionListener(this);

        generateNintendontGamesFolder = new JButton("Generate Nintendont Games Folder");
        generateNintendontGamesFolder.addActionListener(this);

        validateGameFilesMD5 = new JButton("Validate MD5 Hashes in Games Folder");
        validateGameFilesMD5.addActionListener(this);

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        add(pickGamesFolder, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        add(generateNintendontGamesFolder, gridBagConstraints);

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridy=0;
        add(validateGameFilesMD5, gridBagConstraints);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == pickGamesFolder) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int response = fileChooser.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                gameFolderPath = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }

            gameFileList = getGameFileList(gameFolderPath);

            if (gameFileList.size() > 0) {
                ISOFilePathSaver isoFilePathSaver = new ISOFilePathSaver();
                isoFilePathSaver.writeISOFilePathsToFile(gameFileList);
            }
            else {
                JOptionPane.showMessageDialog(this, "There are no games in this folder");
            }
        }

        if (e.getSource() == generateNintendontGamesFolder) {
            File isoFilePaths = new File("isoFilePaths.txt");
            if (isoFilePaths.exists()) {
                NintendontFolderGenerator nintendontFolderGenerator = new NintendontFolderGenerator();
                try {
                    boolean isGenerationSuccessful = nintendontFolderGenerator.generateNintendontFolder();

                    if (isGenerationSuccessful) {
                        OldFileCleaner oldFileCleaner = new OldFileCleaner();
                        oldFileCleaner.cleanFiles();
                        JOptionPane.showMessageDialog(this, "Folder successfully generated!");
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "Folder was not successfully generated!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Something went wrong when generating the games folder");
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "You haven't selected any games!");
            }
        }

        if (e.getSource() == validateGameFilesMD5) {
            File copiedIsoFilePaths = new File("copiedIsoFilePaths.txt");
            if (copiedIsoFilePaths.exists()) {
                try {
                    MD5HashValidator md5HashValidator = new MD5HashValidator();
                    md5HashValidator.validateHashes();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Something went wrong when validating hashes!");
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "You haven't generated a games folder!");
            }
        }
    }

    private ArrayList<File> getGameFileList(String gameFolderPath) {

        File[] gameFolderFileList = new File(gameFolderPath).listFiles();
        ExtensionValidator extensionValidator = new ExtensionValidator();

        ArrayList<File> isoFileList = new ArrayList<>();

        //Grab all .iso or .ciso files and check subfolders if the file is a directory
        for (File file: gameFolderFileList) {
            String fileName = file.getName();
            if (extensionValidator.isExtensionValid(fileName) && !file.isDirectory()) {
                isoFileList.add(file);
            }
            else if (file.isDirectory()) {
                isoFileList.addAll(getGameFileList(file.getAbsolutePath()));
            }
        }

        return isoFileList;
    }
}