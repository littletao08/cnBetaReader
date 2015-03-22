package com.ywwxhz.activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.ywwxhz.cnbetareader.R;
import com.ywwxhz.lib.kits.FileCacheKit;
import com.ywwxhz.lib.kits.FileKit;
import com.ywwxhz.lib.kits.NetKit;
import com.ywwxhz.widget.TranslucentStatus.TranslucentStatusHelper;

import org.apache.http.Header;

import java.io.File;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Created by Sam on 14-4-15.
 */
public class ImageViewActivity extends Activity {
    public static final String IMAGE_URL = "image_url";

    private PhotoView photoView;

    private ProgressWheel progressWheel;
    private PhotoViewAttacher attacher;
    private com.melnykov.fab.FloatingActionButton action;
    private File image;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras().containsKey(IMAGE_URL)) {
            TranslucentStatusHelper.setTranslucentStatus(this,TranslucentStatusHelper.TranslucentProxy.STATUS_BAR);
            setContentView(R.layout.activity_imageview);
            this.action = (FloatingActionButton) findViewById(R.id.action);
            this.progressWheel = (ProgressWheel) findViewById(R.id.progressWheel);
            this.photoView = (PhotoView) findViewById(R.id.photoView);
            this.attacher = new PhotoViewAttacher(photoView);
            this.image = new File(FileCacheKit.getInstance().getCacheDir().getAbsolutePath() + "/" + getIntent().getExtras().getString(IMAGE_URL).hashCode());
            this.action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Cnbeta Reader").toString()
                            + "/" + Uri.parse(getIntent().getExtras().getString(IMAGE_URL)).getLastPathSegment();
                    FileKit.copyFile(image.getAbsolutePath(),
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Cnbeta Reader"),
                            Uri.parse(getIntent().getExtras().getString(IMAGE_URL)).getLastPathSegment());
                    Toast.makeText(ImageViewActivity.this, String.format(Locale.CHINA, "保存成功 文件路径：%s", path), Toast.LENGTH_LONG).show();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                }
            });

            if (image.exists()) {
                loadImage(image);
            } else {
                makeRequest();
            }

        }
    }

    private void makeRequest() {
        final File temp = new File(FileCacheKit.getInstance().getCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis());
        NetKit.getInstance().getClient().get(getIntent().getExtras().getString(IMAGE_URL), new FileAsyncHttpResponseHandler(temp) {
            @Override
            public void onStart() {
                photoView.setImageDrawable(null);
                photoView.setOnClickListener(null);
                attacher.setOnViewTapListener(null);
                attacher.setZoomable(true);
                attacher.update();
                progressWheel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                file.delete();
                photoView.setImageResource(R.drawable.imagehoder_error);
                attacher.setZoomable(false);
                attacher.update();
                photoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeRequest();
                    }
                });
                progressWheel.setVisibility(View.GONE);
                Crouton.makeText(ImageViewActivity.this, "图片下载失败", Style.ALERT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                file.renameTo(image);
                loadImage(image);
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                progressWheel.setProgress((float) bytesWritten / totalSize);
            }

            @Override
            public void onCancel() {
                temp.delete();
            }
        });
    }

    private void loadImage(File file) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(false)
                .considerExifParams(true).build();
        ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString()
                , photoView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressWheel.setProgress(1);
                progressWheel.setVisibility(View.GONE);
                attacher.update();
                action.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        action.setVisibility(View.VISIBLE);
                        action.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    }
                }, 200);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                loadFail();
                Crouton.makeText(ImageViewActivity.this, "图片加载失败", Style.ALERT).show();
            }
        });
    }

    private void loadFail() {
        photoView.setImageResource(R.drawable.imagehoder_error);
        attacher.setZoomable(false);
        attacher.update();
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });
        progressWheel.setVisibility(View.GONE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        NetKit.getInstance().getClient().cancelAllRequests(true);
        super.onDestroy();
    }
}
