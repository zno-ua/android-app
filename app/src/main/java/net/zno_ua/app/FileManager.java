package net.zno_ua.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private final Context mContext;
    private final String FILES_PATH;

    public FileManager(Context context) {
        mContext = context;
        FILES_PATH = context.getFilesDir().getAbsolutePath();
    }

    public boolean saveBitmap(String path, String name, Bitmap bitmap) throws IOException {
        boolean success;
        File dir = new File(FILES_PATH + path);
        success = dir.exists() || dir.mkdirs();

        File file = new File(dir, name);
        success |= file.exists() || file.createNewFile();

        if (success) {
            Bitmap.CompressFormat format;
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            if (name.contains("jpg")) {
                format = Bitmap.CompressFormat.JPEG;
            } else if (name.contains("png")) {
                format = Bitmap.CompressFormat.PNG;
            } else {
                fileOutputStream.close();
                throw new IOException("Can't save image: wrong format.");
            }

            success = bitmap.compress(format, 100, fileOutputStream);
            fileOutputStream.close();
        } else //noinspection ResultOfMethodCallIgnored
            file.delete();

        return success;
    }

    public Drawable openDrawable(String path) throws FileNotFoundException {
        return new BitmapDrawable(mContext.getResources(), openBitmap(path));
    }

    public Bitmap openBitmap(String path) throws FileNotFoundException {
        return BitmapFactory.decodeFile(FILES_PATH + path);
    }

    public boolean isFileExists(String path, String name) {
        return new File(FILES_PATH + path, name).exists();
    }

    @Deprecated
    public boolean isFileExists(String fileName) {
        return new File(FILES_PATH, fileName).exists();
    }

    public boolean deleteFile(String path, String name) {
        return new File(FILES_PATH + path, name).delete();
    }
}
