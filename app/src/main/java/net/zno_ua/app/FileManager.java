package net.zno_ua.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    private final Context mContext;
    private final String FILES_PATH;

    public FileManager(Context context) {
        mContext = context;
        FILES_PATH = context.getFilesDir().getAbsolutePath();
    }

    public boolean saveBitmap(String path, String name, InputStream bitmapInputStream)
            throws IOException {
        return saveBitmap(path, name, BitmapFactory.decodeStream(bitmapInputStream));
    }

    public boolean saveBitmap(String path, String name, Bitmap bitmap) throws IOException {
        boolean success;
        final File dir = new File(FILES_PATH + path);
        success = dir.exists() || dir.mkdirs();

        final File file = new File(dir, name);
        success |= file.exists() || file.createNewFile();

        FileOutputStream fileOutputStream = null;
        try {
            if (success) {
                fileOutputStream = new FileOutputStream(file);
                final Bitmap.CompressFormat format;

                if (name.contains("jpg")) {
                    format = Bitmap.CompressFormat.JPEG;
                } else if (name.contains("png")) {
                    format = Bitmap.CompressFormat.PNG;
                } else {
                    fileOutputStream.close();
                    throw new IOException("Can't save image: wrong format.");
                }

                success = bitmap.compress(format, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } else {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }

        return success;
    }

    public Bitmap openBitmap(String path) throws FileNotFoundException {
        return BitmapFactory.decodeFile(FILES_PATH + path);
    }

    public boolean isFileExists(String path, String name) {
        return new File(FILES_PATH + path, name).exists();
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

    public boolean deleteTestDirectory(long testId) {
        return deleteQuietly(new File(FILES_PATH + "/" + testId));
    }
}
