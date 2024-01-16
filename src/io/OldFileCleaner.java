package io;

import java.io.File;

public class OldFileCleaner {

    public void cleanFiles() {

        File isoFilePathsFile = new File("isoFilePaths.txt");
        File isoFilePathsMultiDiscFile = new File("isoFilePathsMultiDisc.txt");

        if (!isoFilePathsFile.delete()) {
            System.out.println("ISO Paths File could not be deleted");
        }

        if (!isoFilePathsMultiDiscFile.delete()) {
            System.out.println("ISO Multidisc Paths File could not be deleted");
        }
    }

    public void deleteCopiedISOFilePaths() {
        File copiedISOFilePathsFile = new File("copiedIsoFilePaths.txt");

        if (!copiedISOFilePathsFile.delete()) {
            System.out.println("ISO Paths File could not be deleted");
        }
    }
}
