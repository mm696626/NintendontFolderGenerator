package validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class MD5ChecksumValidator {

    public boolean validateChecksums() throws IOException, NoSuchAlgorithmException {
        ArrayList<String> copiedFilePaths = getFilePathsFromCopiedISOFiles();
        ExtensionValidator extensionValidator = new ExtensionValidator();

        boolean successfulValidation = true;

        PrintWriter outputStream = null;

        try {
            outputStream = new PrintWriter( new FileOutputStream("MD5ChecksumResults.txt"));
        }
        catch (FileNotFoundException f) {
            System.out.println("File does not exist");
            System.exit(0);
        }

        for (int i=0; i<copiedFilePaths.size(); i++) {
            String copiedFilePath = copiedFilePaths.get(i);
            if (extensionValidator.isISOFile(copiedFilePath)) {
                File currentISOFile = new File(copiedFilePath);
                long fileSizeInBytes = currentISOFile.length();

                //1,459,978,240 bytes is the number of bytes for all unmodified, untrimmed GameCube .iso files
                //This is why the file extension was validated as an .iso earlier
                if (fileSizeInBytes != 1459978240) {
                    outputStream.println(copiedFilePath + " was not the correct size for a good .iso dump.");
                    successfulValidation = false;
                }
                else {
                    String checksum = calculateMD5(copiedFilePath);
                    if (validateMD5(checksum)) {
                        String[] gameInfo = getGameInfoOfValidChecksum(checksum);
                        String gameInfoLog = generateGameInfoLog(gameInfo);
                        outputStream.println(copiedFilePath + " has a checksum of " + checksum + gameInfoLog);
                    }
                    else {
                        outputStream.println(copiedFilePath + " has a checksum of " + checksum + " which is not a known checksum for a GameCube game .iso dump.");
                        successfulValidation = false;
                    }
                }
            }
            else {
                outputStream.println(copiedFilePath + " was ignored since it's a .ciso file");
            }
        }

        outputStream.close();
        return successfulValidation;
    }

    private ArrayList<String> getFilePathsFromCopiedISOFiles() {
        ArrayList<String> copiedFilePaths = new ArrayList<>();

        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("copiedIsoFilePaths.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Copied ISO File Paths file does not exist");
            return null;
        }

        while (inputStream.hasNextLine()) {
            copiedFilePaths.add(inputStream.nextLine());
        }

        inputStream.close();
        return copiedFilePaths;
    }

    private String calculateMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        //calculate MD5
        byte[] isoFile = Files.readAllBytes(Paths.get(filePath));
        byte[] hash = MessageDigest.getInstance("MD5").digest(isoFile);
        String checksum = new BigInteger(1, hash).toString(16);
        return checksum;
    }

    private boolean validateMD5(String checksum) {
        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("gcn_md5.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("MD5 Validation File does not exist");
            return false;
        }

        while (inputStream.hasNextLine()) {
            if (inputStream.nextLine().contains(checksum)) {
                inputStream.close();
                return true;
            }
        }

        inputStream.close();
        return false;
    }

    private String[] getGameInfoOfValidChecksum(String checksum) {
        Scanner inputStream = null;

        try {
            inputStream = new Scanner (new FileInputStream("gcn_md5.txt"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("MD5 Validation File does not exist");
            return null;
        }

        while (inputStream.hasNextLine()) {
            String line = inputStream.nextLine();
            if (line.contains(checksum)) {
                inputStream.close();
                return line.split("\\|");
            }
        }

        inputStream.close();
        return null; //should never reach here given what this method does
    }

    private String getGameRegion(String gameID) {
        if (gameID.charAt(3) == 'E') {
            return "US";
        }

        else if (gameID.charAt(3) == 'P') {
            return "PAL";
        }

        else if (gameID.charAt(3) == 'J') {
            return "Japanese";
        }

        else {
            return "Some other region I don't know";
        }
    }

    private String generateGameInfoLog(String[] gameInfo) {
        String gameID = gameInfo[1];
        String gameRegion = getGameRegion(gameID);

        String revision = gameInfo[2];
        String discNum = gameInfo[3];
        String gameTitle = gameInfo[4];

        String discNumLog = "";
        String revisionLog = "";
        if (!discNum.equals("0")) {
            discNumLog = " Disc " + discNum;
        }

        if (!revision.equals("00")) {
            revisionLog = " Revision " + revision;
        }

        return " which is a clean dump of the " + gameRegion + " version of " + gameTitle + discNumLog + revisionLog;
    }
}
