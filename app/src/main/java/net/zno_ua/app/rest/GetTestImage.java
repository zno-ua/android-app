package net.zno_ua.app.rest;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;

/**
 * @author Vojko Vladimir
 */
public class GetTestImage extends RESTMethod<Bitmap> {

    private String path;
    private String name;

    public GetTestImage(String path, String name) {
        this.path = path;
        this.name = name;
    }

    @Override
    protected Request<Bitmap> getRequest(RequestFuture<Bitmap> requestFuture) {
        return new ImageRequest(RESTClient.getImageUrl(path, name),
                requestFuture,
                0,
                0,
                ImageView.ScaleType.CENTER_INSIDE,
                Bitmap.Config.RGB_565, requestFuture);
    }
}
