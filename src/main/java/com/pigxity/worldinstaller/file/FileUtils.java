package com.pigxity.worldinstaller.file;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FileUtils {
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName().replaceAll("[:]",""));

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void zipDirectory(Path sourceDirectory, Path zipFile) throws IOException {
        Files.createDirectories(zipFile.getParent());

        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zipFile)); Stream<Path> paths = Files.walk(sourceDirectory)) {
            Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                Path relativePath = sourceDirectory.relativize(path);
                if (relativePath.toString().isEmpty()) {
                    continue;
                }

                String entryName = relativePath.toString().replace(File.separatorChar, '/');
                if (Files.isDirectory(path)) {
                    entryName += "/";
                }

                output.putNextEntry(new ZipEntry(entryName));
                if (Files.isRegularFile(path)) {
                    Files.copy(path, output);
                }
                output.closeEntry();
            }
        }
    }

    public static void unzipFile(String fileZip, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);

            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    public static List<File> getWorldFiles(File directory) {
        File[] files = directory.listFiles(File::isFile);

        if (files == null || files.length == 0) return List.of();

        return Arrays.stream(files)
                .filter(file -> getFileExtension(file).equals(".zip"))
                .filter(file -> zipfileContains(file, "level.dat"))
                .sorted(Comparator.comparingLong(File::lastModified).reversed()) //sort by modification date
                .toList();
    }

    public static boolean fileNotInRootDir(File zip, String targetFile) {
        return zipfileContains(zip, "/" + targetFile);
    }

    public static List<String> listContents(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            List<String> fileContent = zipFile.stream()
                    .map(ZipEntry::getName)
                    .collect(Collectors.toList());

            zipFile.close();
            return fileContent;
        }
        catch (IOException ioException) {
            System.out.println("Error opening zip file" + ioException);
        }
        return null;
    }

    public static boolean zipfileContains(@NotNull File zipfile, String targetFile) {
        return Objects.requireNonNull(listContents(zipfile))
                .stream()
                .anyMatch(file -> file.contains(targetFile));
    }

}
