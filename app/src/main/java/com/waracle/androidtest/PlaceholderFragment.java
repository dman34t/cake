package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class PlaceholderFragment extends ListFragment {

    private ListView mListView;

    public PlaceholderFragment() { /**/ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = rootView.findViewById(android.R.id.list);
        return rootView;
    }

    private class DownloadJSONDataOverInternetTask extends AsyncTask<URL, Void, Boolean> {
        JSONArray array; //ensures access onPostExecute function
        protected Boolean doInBackground(URL... urls) {
            try {
                array = loadData();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            MyAdapter mAdapter;
            mAdapter = new MyAdapter();
            mListView.setAdapter(mAdapter);
            mAdapter.setItems(array);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new DownloadJSONDataOverInternetTask().execute();//new Async task on separate thread to main for network tasks
    }

    private JSONArray loadData() throws IOException, JSONException {
        String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
                "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

        URL url = new URL(JSON_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???

            // Also, Do you trust any utils thrown your way????

            byte[] bytes = StreamUtils.readUnknownFully(in);

            // Read in charset of HTTP content.
            String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, charset);

            // Read string as JSON.
            return new JSONArray(jsonText);
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    private static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }

    private class MyAdapter extends BaseAdapter {

        // Can you think of a better way to represent these items???
        private JSONArray mItems;
        private JSONObject object; //globalised to ensure readability in Async class
        private ImageView image; //globalised to ensure readability in Async class

        public MyAdapter() {
            this(new JSONArray());
        }

        public MyAdapter(JSONArray items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return mItems.getJSONObject(position);
            } catch (JSONException e) {
                Log.e("", e.getMessage());
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View root = inflater.inflate(R.layout.list_item_layout, parent, false);
            if (root != null) {
                TextView title = root.findViewById(R.id.title);
                TextView desc = root.findViewById(R.id.desc);
                image = root.findViewById(R.id.image);
                try {
                    object = (JSONObject) getItem(position);
                    title.setText(object.getString("title"));
                    desc.setText(object.getString("desc"));

                    //new Async task on separate thread to main for network tasks, safer code
                    new DownloadImagesOverInternetTask().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return root;
        }

        public void setItems(JSONArray items) {
            mItems = items;
        }

        private class DownloadImagesOverInternetTask extends AsyncTask<URL, Void, Bitmap> {
            protected Bitmap doInBackground(URL... urls) {
                try {
                    return ImageLoader.convertToBitmap(ImageLoader.loadImageData(object.getString("image")));
                } catch (IOException |JSONException  e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                ImageLoader.setImageView(image,result);
            }
        }
    }
}