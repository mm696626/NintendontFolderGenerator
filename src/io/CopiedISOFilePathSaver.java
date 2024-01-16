package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;

public class CopiedISOFilePathSaver {

    public void writeCopiedISOFilePathsToFile(ArrayList<Path> filePaths) {
        PrintWriter outputStream = null;

        try {
            outputStream = new PrintWriter( new FileOutputStream("copiedIsoFilePaths.txt"));
        }
        catch (FileNotFoundException f) {
            System.out.println("File does not exist");
            System.exit(0);
        }

        for (int i=0; i<filePaths.size(); i++) {
            outputStream.println(filePaths.get(i));
        }
        outputStream.close();
    }
}
