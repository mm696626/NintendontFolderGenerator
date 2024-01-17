package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

public class NintendontFolderGenerator {

    private ArrayList<Path> copiedFilePaths = new ArrayList<>(); //the paths of the copied .iso and .ciso files (will be used if the user wants to validate MD5 checksums)

    public boolean generateNintendontFolder() throws IOException {

        ArrayList<File> files = getFilePathsFromFile();
        ArrayList<File> multiDiscFiles = getMultiDiscFilePathsFromFile();
        String gamesFolderBaseDir = getGamesFolderBaseDir();
        String filePathSeparator = gamesFolderBaseDir.substring(gamesFolderBaseDir.length()-1);
        String gamesFolderPath = gamesFolderBaseDir + "games";
        File gamesFolder = new File(gamesFolderPath);

        boolean isGameFilesCopied = false;
        boolean ismultiDiscGameFilesCopied = false;

        if (gamesFolder.mkdirs()) {
            isGameFilesCopied = copyGameFiles(files, gamesFolder.getAbsolutePath() + filePathSeparator, filePathSeparator);
            ismultiDiscGameFilesCopied = copyMultiDiscGameFiles(multiDiscFiles, gamesFolder.getAbsolutePath() + filePathSeparator, filePathSeparator);
        }

        CopiedISOFilePathSaver copiedISOFilePathSaver = new CopiedISOFilePathSaver();
        copiedISOFilePathSaver.writeCopiedISOFilePathsToFile(copiedFilePaths);

        return isGameFilesCopied && ismultiDiscGameFilesCopied;
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

    private boolean copyGameFiles(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {

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
                Files.copy(files.get(i).toPath(), copiedIsoFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                copiedFilePaths.add(copiedIsoFile.toPath());
            }
        }

        return true;
    }

    private boolean copyMultiDiscGameFiles(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {

        boolean successfullyCopied = true;

        for (int i=0; i<files.size(); i++) {
            String isoFilePath = files.get(i).getAbsolutePath();
            String fileExtension = isoFilePath.substring(isoFilePath.lastIndexOf("."));
            String isoFileName = isoFilePath.substring(isoFilePath.lastIndexOf(filePathSeparator)+1, isoFilePath.lastIndexOf("."));
            isoFileName = isoFileName.substring(0, isoFileName.lastIndexOf("(Disc ")).trim();

            //validate the file name isn't completely gone from trimming the disc notation (naming a file (Disc 1) would break this code)
            if (isoFileName.length() > 0) {
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
                    Files.copy(files.get(i).toPath(), copiedIsoFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    copiedFilePaths.add(copiedIsoFile.toPath());
                }
            }

            else {
                successfullyCopied = false;
            }
        }

        return successfullyCopied;
    }
}
