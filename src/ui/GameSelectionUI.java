package ui;

import io.ISOFilePathSaver;
import io.NintendontFolderGenerator;
import io.OldFileCleaner;
import validation.MD5ChecksumValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GameSelectionUI extends JFrame implements ActionListener {

    private ArrayList<JCheckBox> gameSelectedCheckBoxes;
    private ArrayList<JLabel> isoFilePaths;
    private ArrayList<File> isoFiles;
    private JButton generateNintendontGamesFolder;
    private Container container;

    public GameSelectionUI(ArrayList<File> isoFiles) {
        this.isoFiles = isoFiles;
        setTitle("Select Games for Games Folder");
        this.container = getContentPane();
        container.setLayout(new BorderLayout());
        generateUI();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateNintendontGamesFolder) {
            ArrayList<File> selectedGameFileList = getSelectedGames();
            ISOFilePathSaver isoFilePathSaver = new ISOFilePathSaver();
            isoFilePathSaver.writeISOFilePathsToFile(selectedGameFileList);
            moveToGamesFolder();
        }

    }

    private void generateUI() {
        JPanel jPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(isoFiles.size()+1, 2);
        jPanel.setLayout(gridLayout);

        gameSelectedCheckBoxes = new ArrayList<>();
        isoFilePaths = new ArrayList<>();

        for (int i=0; i<isoFiles.size(); i++) {
            JCheckBox jCheckBox = new JCheckBox();
            gameSelectedCheckBoxes.add(jCheckBox);
            jPanel.add(gameSelectedCheckBoxes.get(i));
            JLabel jLabel = new JLabel(isoFiles.get(i).getAbsolutePath());
            isoFilePaths.add(jLabel);
            jPanel.add(isoFilePaths.get(i));
        }

        generateNintendontGamesFolder = new JButton("Generate Nintendont Games Folder");
        generateNintendontGamesFolder.addActionListener(this);
        jPanel.add(generateNintendontGamesFolder);
        add(jPanel);

        JScrollPane jScrollPane = new JScrollPane(jPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.add(jScrollPane);
    }

    private ArrayList<File> getSelectedGames() {
        ArrayList<File> selectedGames = new ArrayList<>();
        for (int i=0; i<isoFiles.size(); i++) {
            if (gameSelectedCheckBoxes.get(i).isSelected()) {
                selectedGames.add(isoFiles.get(i));
            }
        }

        return selectedGames;
    }

    private void moveToGamesFolder() {
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
                            int calculateMD5ChecksumsDialogResult = JOptionPane.showConfirmDialog(this, "Folder was successfully generated! Would you like to validate the MD5 checksums of your game files in the generated games folder to ensure they are good .iso dumps?");
                            if (calculateMD5ChecksumsDialogResult == JOptionPane.YES_OPTION){
                                calculateMD5Checksums();
                            }
                            int moveToExternalStorageDialogResult = JOptionPane.showConfirmDialog(this, "<html>Would you like to move the generated folder to the root of your SD Card/USB? <b>(This feature is only supported on Windows and Mac)</b><br>Please be patient once Yes is selected. It'll take a bit depending on how many games you have, so just wait for the confirmation that it's done</html>");
                            if (moveToExternalStorageDialogResult == JOptionPane.YES_OPTION){
                                moveToExternalStorage();
                            }

                            OldFileCleaner oldFileCleaner = new OldFileCleaner();
                            oldFileCleaner.cleanFiles();
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
        ExternalStorageMoverUI externalStorageMoverUI = new ExternalStorageMoverUI();
        externalStorageMoverUI.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        externalStorageMoverUI.pack();
        externalStorageMoverUI.setVisible(true);
    }
}
