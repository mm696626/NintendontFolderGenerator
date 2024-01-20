package ui;

import io.ISOFilePathSaver;
import io.NintendontFolderGenerator;
import io.OldFileCleaner;
import validation.ExtensionValidator;
import validation.MD5ChecksumValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class NintendontFolderGeneratorUI extends JFrame implements ActionListener {


    //where to grab iso or ciso files from
    private String gameFolderPath = "";
    private JButton generateNintendontGamesFolder;

    private ArrayList<File> gameFileList;
    GridBagConstraints gridBagConstraints = null;

    public NintendontFolderGeneratorUI()
    {
        setTitle("Nintendont Folder Generator");

        generateNintendontGamesFolder = new JButton("Select Folder with Games and Generate Nintendont Games Folder");
        generateNintendontGamesFolder.addActionListener(this);

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        add(generateNintendontGamesFolder, gridBagConstraints);

        File md5ChecksumFile = new File("gcn_md5.txt");
        if (!md5ChecksumFile.exists()) {
            JOptionPane.showMessageDialog(this, "The MD5 checksums file does not exist! This tool will not function properly without it!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == generateNintendontGamesFolder) {

            JFileChooser fileChooser = new JFileChooser(getRunningDir());
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
                return;
            }

            File isoFilePaths = new File("isoFilePaths.txt");
            if (isoFilePaths.exists()) {
                NintendontFolderGenerator nintendontFolderGenerator = new NintendontFolderGenerator();
                try {

                    boolean doCopyFiles = false;
                    boolean canceledDialog = false;

                    int copyFilesDialogResult = JOptionPane.showConfirmDialog(this, "Would you like to copy your game files to the generated folder? Pressing No will simply move your files instead.");
                    if (copyFilesDialogResult == JOptionPane.YES_OPTION){
                        doCopyFiles = true;
                    }
                    else if (copyFilesDialogResult == JOptionPane.NO_OPTION) {
                        doCopyFiles = false;
                    }
                    else {
                        canceledDialog = true;
                    }

                    if (!canceledDialog) {

                        String gamesFolderBaseDir = getGamesFolderBaseDir();
                        String gamesFolderPath = gamesFolderBaseDir + "games";
                        File gamesFolder = new File(gamesFolderPath);
                        boolean generateFolder = false;

                        if (gamesFolder.exists()) {
                            int overwriteGamesFolderDialogResult = JOptionPane.showConfirmDialog(this, "A games folder already exists at " + gamesFolder.getAbsolutePath() + ". Would you like to overwrite it?");
                            if (overwriteGamesFolderDialogResult == JOptionPane.YES_OPTION){

                                if (deleteDirectory(gamesFolderPath))  {
                                    generateFolder = true;
                                }
                            }
                        }

                        else {
                            generateFolder = true;
                        }

                        if (generateFolder) {
                            boolean isGenerationSuccessful = nintendontFolderGenerator.generateNintendontFolder(doCopyFiles);

                            if (isGenerationSuccessful) {
                                OldFileCleaner oldFileCleaner = new OldFileCleaner();
                                oldFileCleaner.cleanFiles();
                                int calculateMD5ChecksumsDialogResult = JOptionPane.showConfirmDialog(this, "Folder was successfully generated! Would you like to validate the MD5 checksums of your game files in the generated games folder to ensure they are good .iso dumps?");
                                if (calculateMD5ChecksumsDialogResult == JOptionPane.YES_OPTION){
                                    calculateMD5Checksums();
                                }
                            }
                            else {
                                JOptionPane.showMessageDialog(this, "Folder was not successfully generated!");
                            }
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Something went wrong when generating the games folder");
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "You haven't selected any games!");
            }
        }
    }

    private String getGamesFolderBaseDir() {
        File isoFilePaths = new File("isoFilePaths.txt");
        String isoFilePathsFilePath = isoFilePaths.getAbsolutePath();
        return isoFilePathsFilePath.substring(0, isoFilePathsFilePath.lastIndexOf("isoFilePaths.txt"));
    }

    private String getRunningDir() {
        File md5Checksums = new File("gcn_md5.txt");
        String md5ChecksumsPath = md5Checksums.getAbsolutePath();
        return md5ChecksumsPath.substring(0, md5ChecksumsPath.lastIndexOf("gcn_md5.txt"));
    }

    private void calculateMD5Checksums() {
        File copiedIsoFilePaths = new File("copiedIsoFilePaths.txt");
        if (copiedIsoFilePaths.exists()) {
            try {
                MD5ChecksumValidator md5ChecksumValidator = new MD5ChecksumValidator();
                if (md5ChecksumValidator.validateChecksums()) {
                    JOptionPane.showMessageDialog(this, "All checksums are known good dumps!");
                }
                else {
                    JOptionPane.showMessageDialog(this, "Not all checksums are known good dumps! Please look in the generated log file!");
                }
                OldFileCleaner oldFileCleaner = new OldFileCleaner();
                oldFileCleaner.deleteCopiedISOFilePaths();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Something went wrong when validating checksums!");
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "You haven't generated a games folder!");
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

    private boolean deleteDirectory(String folderPath) {

        File[] folderFileList = new File(folderPath).listFiles();

        //Grab all files and check subfolders if the file is a directory
        for (File file: folderFileList) {
            if (file.isDirectory()) {
                deleteDirectory(file.getAbsolutePath());
            }

            file.delete();
        }

        File folder = new File(folderPath);
        return folder.delete();
    }
}