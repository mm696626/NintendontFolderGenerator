package ui;

import validation.ExtensionValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ExternalStorageMoverUI extends JFrame implements ActionListener {

    public static final int BYTES_IN_GB = 1000000000;

    //where to grab iso or ciso files from
    private String gameFolderPath = "";
    private JButton moveToExternalDrive;

    private JComboBox driveDropdown;
    private ArrayList<File> gameFileList;

    private JLabel gameFolderSize;
    private long gameFolderTotalSize;
    private JLabel externalDriveFreeSpace;
    private double externalDriveFreeSpaceInGB;
    private double gameFolderTotalSizeInGB;
    private boolean canMoveToDrive = false;

    public ExternalStorageMoverUI() throws IOException {
        setTitle("Move to External Storage");
        gameFolderPath = getGamesFolderBaseDir() + "games";
        gameFileList = getGameFileList(gameFolderPath);
        gameFolderTotalSize = calculateGameFolderSize();
        generateUI();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == moveToExternalDrive) {

            String destinationPath = String.valueOf(driveDropdown.getSelectedItem());

            int prepareToMoveFilesDialogResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to move the games folder to drive " + destinationPath + "?");
            if (prepareToMoveFilesDialogResult == JOptionPane.YES_OPTION && canMoveToDrive) {

                String gamesFolderBaseDir = getGamesFolderBaseDir();
                String filePathSeparator = gamesFolderBaseDir.substring(gamesFolderBaseDir.length()-1);
                String OS = System.getProperty("os.name").toLowerCase();
                if (OS.contains("windows")) {
                    destinationPath = destinationPath.substring(0, destinationPath.indexOf(filePathSeparator));
                }
                try {
                    moveGameFilesToExternalDrive(getDirectoryList(gameFolderPath), destinationPath + filePathSeparator + "games" + filePathSeparator, filePathSeparator);
                    JOptionPane.showMessageDialog(this, "Games folder successfully moved!");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if (prepareToMoveFilesDialogResult == JOptionPane.YES_OPTION && !canMoveToDrive) {
                JOptionPane.showMessageDialog(this, "There is not enough space on the selected drive!");
            }
        }
    }

    private void generateUI() throws IOException {

        JPanel jPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 2);
        jPanel.setLayout(gridLayout);

        String OS = System.getProperty("os.name").toLowerCase();
        File[] roots;
        if (OS.contains("windows")) {
            roots = File.listRoots();
        }
        else if (OS.contains("mac")) {
            roots = new File("/Volumes").listFiles();
        }
        else {
            JOptionPane.showMessageDialog(this, "Unfortunately, I don't have anything other than a Windows or Mac, so I can't test on other operating systems");
            setVisible(false);
            return;
        }

        if (roots.length > 0) {
            driveDropdown = new JComboBox<>(roots);
        }

        driveDropdown.setSelectedIndex(0);
        driveDropdown.addActionListener (e -> {
            try {
                getDriveFreeSpace(roots[driveDropdown.getSelectedIndex()]);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        getDriveFreeSpace(roots[0]);

        gameFolderTotalSizeInGB = (double)gameFolderTotalSize / BYTES_IN_GB;
        gameFolderTotalSizeInGB = Math.round(gameFolderTotalSizeInGB * 100.0);
        gameFolderTotalSizeInGB = gameFolderTotalSizeInGB/100.0;

        gameFolderSize = new JLabel("Game Folder Size: " + gameFolderTotalSizeInGB + " GB");

        moveToExternalDrive = new JButton("Move to this Drive");
        moveToExternalDrive.addActionListener(this);

        jPanel.add(driveDropdown);
        jPanel.add(externalDriveFreeSpace);
        jPanel.add(moveToExternalDrive);
        jPanel.add(gameFolderSize);

        add(jPanel);
    }

    private String getGamesFolderBaseDir() {
        File isoFilePaths = new File("isoFilePaths.txt");
        String isoFilePathsFilePath = isoFilePaths.getAbsolutePath();
        return isoFilePathsFilePath.substring(0, isoFilePathsFilePath.lastIndexOf("isoFilePaths.txt"));
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

    private ArrayList<File> getDirectoryList(String gameFolderPath) {

        File[] gameFolderFileList = new File(gameFolderPath).listFiles();
        ArrayList<File> directories = new ArrayList<>();


        //Grab all directories if the file is a directory
        for (File file: gameFolderFileList) {
            if (file.isDirectory()) {
                directories.add(file);
                directories.addAll(getDirectoryList(file.getAbsolutePath()));
            }
        }

        return directories;
    }

    private long calculateGameFolderSize() {
        long folderSize = 0;
        for (int i=0; i<gameFileList.size(); i++) {
            folderSize += gameFileList.get(i).length();
        }
        return folderSize;
    }

    private void getDriveFreeSpace(File root) throws IOException {
        FileStore fileStore = Files.getFileStore(root.toPath());
        externalDriveFreeSpaceInGB = (double)fileStore.getUsableSpace() / BYTES_IN_GB;
        externalDriveFreeSpaceInGB = Math.round(externalDriveFreeSpaceInGB * 100.0);
        externalDriveFreeSpaceInGB = externalDriveFreeSpaceInGB/100.0;
        if (externalDriveFreeSpace != null) {
            externalDriveFreeSpace.setText("Drive Free Space: " + externalDriveFreeSpaceInGB + " GB");
        }
        else {
            externalDriveFreeSpace = new JLabel("Drive Free Space: " + externalDriveFreeSpaceInGB + " GB");
        }

        if (gameFolderTotalSizeInGB < externalDriveFreeSpaceInGB) {
            canMoveToDrive = true;
        }
        else {
            canMoveToDrive = false;
        }
    }

    private void moveGameFilesToExternalDrive(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {
        File destinationFolder = new File(destinationPath);
        if (destinationFolder.exists()) {
            destinationFolder.delete();
        }

        destinationFolder.mkdirs();

        for (int i=0; i<files.size(); i++) {
            String isoDirectory = files.get(i).getAbsolutePath();
            File isoFolder = new File(isoDirectory);
            File[] isoFiles = isoFolder.listFiles();

            String isoFileName = isoDirectory.substring(isoDirectory.indexOf("games")+6); //reason 6 is being added is for the games folder name and the separator. Like "games/"
            File externalDriveGameFolder = new File(destinationPath + isoFileName);

            if (externalDriveGameFolder.exists()) {
                externalDriveGameFolder.delete();
            }

            externalDriveGameFolder.mkdirs();


            for (int fileNum=0; fileNum<isoFiles.length; fileNum++) {
                File isoFile = isoFiles[fileNum];

                String isoFilePath = isoFile.getAbsolutePath();
                String isoName = isoFilePath.substring(isoFilePath.lastIndexOf(filePathSeparator)+1, isoFilePath.lastIndexOf("."));
                String fileExtension = isoFilePath.substring(isoFilePath.lastIndexOf("."));
                String copiedIsoFolderPath = externalDriveGameFolder + filePathSeparator + isoName + fileExtension;
                File copiedIsoFile = new File(copiedIsoFolderPath);
                Files.move(isoFiles[fileNum].toPath(), copiedIsoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
