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
        String modFolderName = getModFolderName();
        String modBaseDir = getModBaseDir();
        String filePathSeparator = modBaseDir.substring(modBaseDir.length()-1);
        String modFolderPath = modBaseDir + modFolderName;

        File modFolder = new File(modFolderPath);

        if (modFolder.mkdirs()) {

            //copy mod.json
            File modJSON = new File("mod.json");
            String copiedModJSONPath = modFolder.getAbsolutePath() + filePathSeparator + "mod.json";
            File copiedModJSON = new File(copiedModJSONPath);
            Files.copy(modJSON.toPath(), copiedModJSON.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String audioFolderPath = modFolderPath + filePathSeparator + "audio";

            File audioFolder = new File(audioFolderPath);
            if (audioFolder.mkdirs()) {

                //copy audio_replacements.json
                File audioReplacementsJSON = new File("audio_replacements.json");
                String copiedAudioReplacementsJSONPath = audioFolder.getAbsolutePath() + filePathSeparator + "audio_replacements.json";
                File copiedAudioReplacementsJSON = new File(copiedAudioReplacementsJSONPath);
                Files.copy(audioReplacementsJSON.toPath(), copiedAudioReplacementsJSON.toPath(), StandardCopyOption.REPLACE_EXISTING);

                copyAudioFiles(files, audioFolder.getAbsolutePath() + filePathSeparator, filePathSeparator);
            }
        }
    }

    private String getModBaseDir() {
        File modJSON = new File("mod.json");
        String modJSONPath = modJSON.getAbsolutePath();
        return modJSONPath.substring(0, modJSONPath.lastIndexOf("mod.json"));
    }

    private ArrayList<File> getFilePathsFromFile() {
        ArrayList<File> files = new ArrayList<>();

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("oggFilePaths.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("OGG File Paths file does not exist");
            return null;
        }

        while (inputStream.hasNextLine()) {
            files.add(new File(inputStream.nextLine()));
        }

        inputStream.close();
        return files;
    }

    private String getModFolderName() {
        String modFolderName = "";

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("mod.json"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Mod JSON File does not exist");
            return null;
        }

        String line = "";
        while (inputStream.hasNextLine()) {
            line = inputStream.nextLine();
            if (line.contains("\"Name\"")) {
                modFolderName = line.substring(line.indexOf(":") + 3, line.lastIndexOf("\""));
                break;
            }
        }

        modFolderName = modFolderName.replaceAll("[^a-zA-Z0-9]", "");
        inputStream.close();
        return modFolderName;
    }

    private void copyAudioFiles(ArrayList<File> files, String destinationPath, String filePathSeparator) throws IOException {

        for (int i=0; i<files.size(); i++) {
            String audioFilePath = files.get(i).getAbsolutePath();
            String copiedAudioFilePath = destinationPath + audioFilePath.substring(audioFilePath.lastIndexOf(filePathSeparator)+1);
            File copiedAudioFile = new File(copiedAudioFilePath);

            if (isAudioFileUsed(copiedAudioFile)) {
                Files.copy(files.get(i).toPath(), copiedAudioFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private boolean isAudioFileUsed(File audioFile) {

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("audio_replacements.json"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Audio Replacements File does not exist");
            return false;
        }

        String line = "";
        while (inputStream.hasNextLine()) {
            line = inputStream.nextLine();
            if (line.contains(audioFile.getName())) {
                inputStream.close();
                return true;
            }
        }

        inputStream.close();
        return false;
    }
}
