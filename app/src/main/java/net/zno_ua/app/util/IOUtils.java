package net.zno_ua.app.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import net.zno_ua.app.ZNOApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    @WorkerThread
    public static boolean saveFile(String path, String name, InputStream fileInputStream)
            throws IOException {
        boolean isFileSaved = false;
        final File dir = new File(ZNOApplication.getInstance().getFilesDir(), path);
        final File file = new File(dir, name);
        final boolean isFileCreated = dir.exists() || dir.mkdirs() || file.exists()
                || file.createNewFile();

        OutputStream fileOutputStream = null;
        try {
            if (isFileCreated) {
                fileOutputStream = new FileOutputStream(file);

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.flush();
                isFileSaved = true;
            } else {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        return isFileSaved;
    }

    public static void cleanOldImagesDir(@NonNull Context context) {
        final File dir = new File(context.getFilesDir(), "images");
        if (dir.exists()) {
            deleteQuietly(dir);
        }
    }

    public static boolean isFileExists(String path, String name) {
        return new File(ZNOApplication.getInstance().getFilesDir() + path, name).exists();
    }

    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception ignored) {
        }
        try {
            return file.delete();
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }
        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (null != exception) {
            throw exception;
        }
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }
        if (!directory.delete()) {
            throw new IOException("Unable to delete directory " + directory + ".");
        }
    }

    private static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        File fileInCanonicalDir;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    public static boolean deleteTestDirectory(long testId) {
        return deleteQuietly(new File(ZNOApplication.getInstance().getFilesDir(), "" + testId));
    }
}
