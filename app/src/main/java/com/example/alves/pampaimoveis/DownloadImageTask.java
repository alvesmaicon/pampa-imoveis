package com.example.alves.pampaimoveis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;


public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;


    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap image = null;
        if(urldisplay != null) {
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
        if(image == null){
            //TODO SET IMAGE STATIC FROM IC_NO_IMAGE
        }

        return image;
    }



    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }

}
