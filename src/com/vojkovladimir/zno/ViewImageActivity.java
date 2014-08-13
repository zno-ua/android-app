package com.vojkovladimir.zno;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ViewImageActivity extends Activity implements OnClickListener {

	FileManager fm;
	ImageView img;

	String source;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		source = getIntent().getStringExtra(
				ZNOApplication.ExtrasKeys.IMG_SOURCE);

		fm = new FileManager(this);
		img = (ImageView) findViewById(R.id.view_image_img);
		img.setOnClickListener(this);
		
		try {
			Drawable imgDrawable = fm.openDrawable(source);
			imgDrawable.setBounds(0, 0, imgDrawable.getMinimumWidth(), imgDrawable.getIntrinsicHeight());
			img.setImageDrawable(imgDrawable);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		finish();
	}

}
