package com.vojkovladimir.zno;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class FileManager {

	private final Context context;
	private final String FILES_PATH;

	public FileManager(Context context) {
		this.context = context;
		FILES_PATH = context.getFilesDir().getAbsolutePath();
	}

	public void saveDrawable(String path, String name, Drawable drawable) {
		File dir = new File(path);
		if (!dir.exists()) {
			createFolder(path);
		}

		File file = new File(FILES_PATH + dir, name);
		Bitmap.CompressFormat format = null;
		try {
			file.createNewFile();
			FileOutputStream fout = new FileOutputStream(file);
			Bitmap bd = ((BitmapDrawable) drawable).getBitmap();

			if (name.contains("jpg")) {
				format = Bitmap.CompressFormat.JPEG;
			}
			if (name.contains("png")) {
				format = Bitmap.CompressFormat.PNG;
			}
			
			bd.compress(format, 100, fout);
		} catch (IOException e) {
			Log.e("MyLogs", file.getAbsolutePath() + " can't create!");
			Log.e("MyLogs", e.toString());
			e.printStackTrace();
		}

	}

	public Drawable openDrawable(String path) throws FileNotFoundException {
		File drawable = new File(FILES_PATH + path);
		if (drawable.exists()) {
			FileInputStream fis = new FileInputStream(drawable);
			return Drawable.createFromStream(fis, null);
		} else {
			return null;
		}
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
				Log.i("MyLogs", folder.getAbsolutePath()
						+ ((folder.exists()) ? " created" : " don't created"));
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

}
