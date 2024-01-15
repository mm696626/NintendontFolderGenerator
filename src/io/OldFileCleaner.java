package io;

import java.io.File;

public class OldFileCleaner {

    public void cleanFiles() {

        File isoFilePathsFile = new File("isoFilePaths.txt");

        if (!isoFilePathsFile.delete()) {
            System.out.println("OGG Paths File could not be deleted");
        }
    }
}
