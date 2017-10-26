package net.mosajon.fruits;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView homeWebView;
    private String url="http://bbs.gfan.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeWebView = (WebView) findViewById(R.id.homeWebView);
        homeWebView.setWebChromeClient(new WebChromeClient());
        homeWebView.setWebViewClient(new WebViewClient());
        homeWebView.getSettings().setJavaScriptEnabled(true);
        homeWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (!homeWebView.getOriginalUrl().equals(url)) {
                homeWebView.goBack();
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private ValueCallback<Uri> mUploadFile;
    /**拍照/选择文件请求码*/
    private static final int REQUEST_UPLOAD_FILE_CODE = 12343;
    private void setWebChromeClient()
    {
        if (null != homeWebView)
        {
            homeWebView.setWebChromeClient(new WebChromeClient()
            {
                // Andorid 4.1+
                public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture)
                {
                    openFileChooser(uploadFile);
                }

                // Andorid 3.0 +
                public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType)
                {
                    openFileChooser(uploadFile);
                }

                // Android 3.0
                public void openFileChooser(ValueCallback<Uri> uploadFile)
                {
                    // Toast.makeText(WebviewActivity.this, "上传文件/图片",Toast.LENGTH_SHORT).show();
                    mUploadFile = uploadFile;
                    startActivityForResult(Intent.createChooser(createCameraIntent(), "Image Browser"), REQUEST_UPLOAD_FILE_CODE);
                }
            });
        }
    }

    private Intent createCameraIntent()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//拍照
                Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);//选择图片文件
        imageIntent.setType("image/*");
        return cameraIntent;
    }

    //最后在OnActivityResult中接受返回的结果
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_UPLOAD_FILE_CODE && resultCode == RESULT_OK)
        {
            if (null == mUploadFile)
            {
                return;
            }
            Uri result = (null == data) ? null : data.getData();
            if (null != result)
            {
                ContentResolver resolver = this.getContentResolver();
                String[] columns = { MediaStore.Images.Media.DATA };
                Cursor cursor = resolver.query(result, columns, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(columns[0]);
                String imgPath = cursor.getString(columnIndex);
                System.out.println("imgPath = " + imgPath);
                if (null == imgPath)
                {
                    return;
                }
                File file = new File(imgPath);
                //将图片处理成大小符合要求的文件
                result = Uri.fromFile(handleFile(file));
                mUploadFile.onReceiveValue(result);
                mUploadFile = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**处理拍照/选择的文件*/
    private File handleFile(File file)
    {
        DisplayMetrics dMetrics = getResources().getDisplayMetrics();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        System.out.println("  imageWidth = " + imageWidth + " imageHeight = " + imageHeight);
        int widthSample = (int) (imageWidth / (dMetrics.density * 90));
        int heightSample = (int) (imageHeight / (dMetrics.density * 90));
        System.out.println("widthSample = " + widthSample + " heightSample = " + heightSample);
        options.inSampleSize = widthSample < heightSample ? heightSample : widthSample;
        options.inJustDecodeBounds = false;
        Bitmap newBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        System.out.println("newBitmap.size = " + newBitmap.getRowBytes() * newBitmap.getHeight());
        File handleFile = new File(file.getParentFile(), "upload.png");
        try
        {
            if (newBitmap.compress(Bitmap.CompressFormat.PNG, 50, new FileOutputStream(handleFile)))
            {
                System.out.println("保存图片成功");
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return handleFile;

    }
}
