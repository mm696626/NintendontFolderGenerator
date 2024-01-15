package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

public class NintendontFolderGenerator {

    public void generateNintendontFolder() throws IOException {

        ArrayList<File> files = getFilePathsFromFile();
        ArrayList<File> multiDiscFiles = getMultiDiscFilePathsFromFile();
        String gamesFolderBaseDir = getGamesFolderBaseDir();
        String filePathSeparator = gamesFolderBaseDir.substring(gamesFolderBaseDir.length()-1);
        String gamesFolderPath = gamesFolderBaseDir + "games";
        File gamesFolder = new File(gamesFolderPath);

        if (gamesFolder.mkdirs()) {
            copyGameFiles(files, gamesFolder.getAbsolutePath() + filePathSeparator, filePathSeparator);
            copyMultiDiscGameFiles(multiDiscFiles, gamesFolder.getAbsolutePath() + filePathSeparator, filePathSeparator);
        }
    }

    private String getGamesFolderBaseDir() {
        File isoFilePaths = new File("isoFilePaths.txt");
        String isoFilePathsFilePath = isoFilePaths.getAbsolutePath();
        return isoFilePathsFilePath.substring(0, isoFilePathsFilePath.lastIndexOf("isoFilePaths.txt"));
    }

    private ArrayList<File> getFilePathsFromFile() {
        ArrayList<File> files = new ArrayList<>();

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("isoFilePaths.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("ISO File Paths file does not exist");
            return null;
        }

        while (inputStream.hasNextLine()) {
            files.add(new File(inputStream.nextLine()));
        }

        inputStream.close();
        return files;
    }

    private ArrayList<File> getMultiDiscFilePathsFromFile() {
        ArrayList<File> files = new ArrayList<>();

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("isoFilePathsMultiDisc.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("ISO File Paths file does not exist");
            return null;
        }

        while (inputStream.hasNextLine()) {
            files.add(new File(inputStream.nextLine()));
        }

        inputStream.close();
        return files;
    }

    private void copyGameFiles(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {

        for (int i=0; i<files.size(); i++) {
            String isoFilePath = files.get(i).getAbsolutePath();
            String fileExtension = isoFilePath.substring(isoFilePath.lastIndexOf("."));
            String isoFileName = isoFilePath.substring(isoFilePath.lastIndexOf(filePathSeparator)+1, isoFilePath.lastIndexOf("."));
            String copiedIsoFolderPath = destinationPath + isoFileName;

            File gameFolder = new File(copiedIsoFolderPath);

            if (gameFolder.mkdirs()) {
                String gameFolderPath = gameFolder.getAbsolutePath();
                String copiedISOFilePath = gameFolderPath + filePathSeparator + "game" + fileExtension;
                File copiedIsoFile = new File(copiedISOFilePath);
                Files.copy(files.get(i).toPath(), copiedIsoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void copyMultiDiscGameFiles(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {

        for (int i=0; i<files.size(); i++) {
            String isoFilePath = files.get(i).getAbsolutePath();
            String fileExtension = isoFilePath.substring(isoFilePath.lastIndexOf("."));
            String isoFileName = isoFilePath.substring(isoFilePath.lastIndexOf(filePathSeparator)+1, isoFilePath.lastIndexOf("."));
            isoFileName = isoFileName.substring(0, isoFileName.lastIndexOf("(Disc ")).trim();
            String copiedIsoFolderPath = destinationPath + isoFileName;

            File gameFolder = new File(copiedIsoFolderPath);

            if (gameFolder.mkdirs() || gameFolder.exists()) {
                String gameFolderPath = gameFolder.getAbsolutePath();

                String copiedISOFilePath = "";
                if (isoFilePath.contains("(Disc 1)")) {
                    copiedISOFilePath = gameFolderPath + filePathSeparator + "game" + fileExtension;
                }
                else {
                    copiedISOFilePath = gameFolderPath + filePathSeparator + "disc2" + fileExtension;
                }
                File copiedIsoFile = new File(copiedISOFilePath);
                Files.copy(files.get(i).toPath(), copiedIsoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
