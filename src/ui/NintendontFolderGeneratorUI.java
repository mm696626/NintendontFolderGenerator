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
import java.io.IOException;
import java.util.ArrayList;

public class NintendontFolderGeneratorUI extends JFrame implements ActionListener {


    //where to grab iso or ciso files from
    private String gameFolderPath = "";
    private JButton generateNintendontGamesFolder, validateMD5Checksums, moveToExternalStorage;

    private ArrayList<File> gameFileList;

    public NintendontFolderGeneratorUI()
    {
        setTitle("Nintendont Folder Generator");
        generateUI();
    }

    private void generateUI() {
        JPanel mainMenuPanel = new JPanel();
        GridLayout mainMenuGridLayout = new GridLayout(1, 1);
        mainMenuPanel.setLayout(mainMenuGridLayout);

        JPanel generatorToolsPanel = new JPanel();
        GridLayout generatorToolsGridLayout = new GridLayout(2,1);
        generatorToolsPanel.setLayout(generatorToolsGridLayout);

        generateNintendontGamesFolder = new JButton("Select Folder with Games");
        generateNintendontGamesFolder.addActionListener(this);
        mainMenuPanel.add(generateNintendontGamesFolder);

        validateMD5Checksums = new JButton("Validate MD5 Checksums of Games Folder");
        validateMD5Checksums.addActionListener(this);
        generatorToolsPanel.add(validateMD5Checksums);

        moveToExternalStorage = new JButton("Move Games Folder to External Storage");
        moveToExternalStorage.addActionListener(this);
        generatorToolsPanel.add(moveToExternalStorage);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Generate Folder", mainMenuPanel);
        tabbedPane.add("Generator Tools", generatorToolsPanel);
        add(tabbedPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == generateNintendontGamesFolder) {

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
                return;
            }

            int moveAllGamesDialogResult = JOptionPane.showConfirmDialog(this, "Would you like to move all the games in the selected folder? Pressing No will allow you to choose specific games to move");
            if (moveAllGamesDialogResult != JOptionPane.YES_OPTION){
                GameSelectionUI gameSelectionUI = new GameSelectionUI(gameFileList);
                gameSelectionUI.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                gameSelectionUI.pack();
                gameSelectionUI.setVisible(true);
                return;
            }

            File isoFilePaths = new File("isoFilePaths.txt");
            if (isoFilePaths.exists()) {
                NintendontFolderGenerator nintendontFolderGenerator = new NintendontFolderGenerator();
                try {

                    boolean doCopyFiles = false;
                    boolean canceledDialog = false;

                    int copyFilesDialogResult = JOptionPane.showConfirmDialog(this, "<html>Would you like to copy your game files to the generated folder? Pressing No will simply move your files instead.<br>Please be patient once Yes or No is selected. It'll take a bit depending on how many games you have, so just wait for the confirmation that it's done</html>");
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
                                JOptionPane.showMessageDialog(this, "Folder was successfully generated!");
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

        if (e.getSource() == validateMD5Checksums) {
            calculateMD5Checksums();
        }

        if (e.getSource() == moveToExternalStorage) {
            try {
                moveToExternalStorage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String getGamesFolderBaseDir() {
        File isoFilePaths = new File("isoFilePaths.txt");
        String isoFilePathsFilePath = isoFilePaths.getAbsolutePath();
        return isoFilePathsFilePath.substring(0, isoFilePathsFilePath.lastIndexOf("isoFilePaths.txt"));
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

    private void moveToExternalStorage() throws IOException {
        String gamesFolderBaseDir = getGamesFolderBaseDir();
        String gamesFolderPath = gamesFolderBaseDir + "games";
        File gamesFolder = new File(gamesFolderPath);

        if (gamesFolder.exists()) {
            ExternalStorageMoverUI externalStorageMoverUI = new ExternalStorageMoverUI();
            externalStorageMoverUI.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            externalStorageMoverUI.pack();
            externalStorageMoverUI.setVisible(true);
        }
        else {
            JOptionPane.showMessageDialog(this, "You haven't generated a games folder!");
        }
    }
}