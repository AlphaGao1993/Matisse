package com.zhihu.matisse.sample;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zhihu.matisse.engine.ImageEngine;

/**
 * Created by alpha on 2017/12/8.
 */

public class PicassoLoader implements ImageEngine {
    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        Picasso.with(context).load(uri).placeholder(placeholder)
                .resize(resize, resize)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        loadThumbnail(context, resize, placeholder, imageView, uri);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Picasso.with(context).load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
                .centerInside().into(imageView);

    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        loadImage(context, resizeX, resizeY, imageView, uri);
    }

    @Override
    public boolean supportAnimatedGif() {
        return false;
    }
}
