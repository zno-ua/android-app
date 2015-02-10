package net.zno_ua.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private final Context context;
    private final String FILES_PATH;

    public FileManager(Context context) {
        this.context = context;
        FILES_PATH = context.getFilesDir().getAbsolutePath();
    }

    public void saveBitmap(String path, String name, Bitmap bitmap) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            createFolder(path);
        }

        File file = new File(FILES_PATH + dir, name);
        file.createNewFile();
        Bitmap.CompressFormat format = null;
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        if (name.contains("jpg")) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (name.contains("png")) {
            format = Bitmap.CompressFormat.PNG;
        } else if (name.contains("gif")) {
            fileOutputStream.close();
            throw new IOException("Can't save image: wrong format.");
        }

        boolean compressed = bitmap.compress(format, 100, fileOutputStream);
        if (!compressed) {
            throw new IOException("Can't save file: " + path + name);
        }
        fileOutputStream.close();
    }

    public Drawable openDrawable(String path) throws FileNotFoundException {
        File drawable = new File(FILES_PATH + path);
        FileInputStream fis = new FileInputStream(drawable);
        return new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(fis));
    }

    public void createFolder(String pathName) {
        File folder;
        String[] subFolders = pathName.split("/");

        if (subFolders.length > 1) {
            String fullPath = "";

            for (String folderName : subFolders) {
                fullPath += folderName;
                folder = new File(FILES_PATH + fullPath);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                fullPath += "/";
            }
        }
    }

    public void clearAllFiles() {
        for (String fileName : context.fileList()) {
            deleteFile(new File(FILES_PATH, fileName));
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            if (file.listFiles() != null) {
                for (File subFile : file.listFiles()) {
                    deleteFile(subFile);
                }
            }
        }
        file.delete();
    }

    public boolean isFileExists(String fileName) {
        return new File(FILES_PATH, fileName).exists();
    }

}