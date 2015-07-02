package com.vi.adapter;

import java.io.InputStream;
import java.net.URL;

import com.vi.common.ContentBean;
import com.vi.parser.JSoupHelperAuthen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TableRow;

public class DownloadImagesTask extends AsyncTask<Object, Void, Bitmap> {

    ImageView imageView = null;
    JSoupHelperAuthen jsoupAuthen = null;
    ContentBean c = null;

    @Override
    protected Bitmap doInBackground(Object... params) {
        this.imageView = (ImageView)params[0];
        this.jsoupAuthen = (JSoupHelperAuthen)params[1];
        this.c = (ContentBean)params[2];

        return download_Image();
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        imageView.setImageBitmap(result);
        imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
    }

    private Bitmap download_Image() {
        Bitmap bmp = null;
        try{
            if(c.getImageUrl().indexOf("thaivi") != -1){
                try{
                    InputStream in = JSoupHelperAuthen.authen.getImages(c.getImageUrl());
                    if(in != null){
                        bmp = BitmapFactory.decodeStream(in);
                        //imageView.setImageBitmap(bmp);
                    }
                }catch(Exception e){}
            }else{
                URL newurl = new URL(c.getImageUrl());
                bmp = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
                //imageView.setImageBitmap(bmp);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return bmp;
    }
}