package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;


/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {

    protected static byte[] loadImageData(String url) throws IOException {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(); //would use HttpsURLConnection,
        // more secure on unsecured networks as suggested by Google but would require only HTTPS urls, no access to edit JSON

        if (connection.getResponseCode() == 404) //page not found
        {
            throw new IOException(connection.getResponseMessage());
        }

        if (connection.getResponseCode() != 200) //200 is the returned HTTP header message that is ok
        {
            throw new IOException(connection.getResponseMessage());
        }

        InputStream inputStream = null;
        try {
            try {
                // Read data from workstation
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = connection.getErrorStream();
            }

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            return StreamUtils.readUnknownFully(inputStream);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            connection.disconnect();
        }
    }

    protected static Bitmap convertToBitmap(byte[] data) {

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    protected static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
