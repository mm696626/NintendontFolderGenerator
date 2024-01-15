package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ISOFilePathSaver {

    public void writeISOFilePathsToFile(ArrayList<File> files) {
        PrintWriter outputStream = null;

        try {
            outputStream = new PrintWriter( new FileOutputStream("isoFilePaths.txt"));
        }
        catch (FileNotFoundException f) {
            System.out.println("File does not exist");
            System.exit(0);
        }

        for (int i=0; i<files.size(); i++) {
            outputStream.println(files.get(i).getAbsolutePath());
        }

        outputStream.close();
    }
}
