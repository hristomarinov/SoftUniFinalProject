package marinov.hristo.softuniproject.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Hristo Marinov (christo_marinov@abv.bg).
 */
public class ImageURLParser implements Html.ImageGetter {
    private Context c;
    private Activity activity;
    private View container;

    /**
     * Construct the ImageURLParser which will execute AsyncTask and refresh the container
     *
     * @param t View
     * @param c Context
     */
    public ImageURLParser(View t, Context c, Activity activity) {
        this.c = c;
        this.container = t;
        this.activity = activity;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();

        // get the actual source
        ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

        asyncTask.execute(source);

        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;
        DisplayMetrics dm = new DisplayMetrics();

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels - 20;
                int height = width * result.getIntrinsicHeight() / result.getIntrinsicWidth();

                urlDrawable.setBounds(0, 0, width, height);
                // change the reference of the current drawable to the result from the HTTP call
                urlDrawable.drawable = result;
                // redraw the image by invalidating the container
                ImageURLParser.this.container.invalidate();
                ((TextView) container).setHeight(container.getHeight() + height - 12);
                ((TextView) container).setEllipsize(null);
            }
        }

        /**
         * Get the Drawable from URL
         *
         * @param urlString URL string
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");

                activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels - 20;
                int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();

                drawable.setBounds(0, 0, width, height);
                return drawable;
            } catch (Throwable e) {
                if (e instanceof OutOfMemoryError)
                    e.printStackTrace();

                return null;
            }
        }

        private InputStream fetch(String urlString) throws IOException {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            return new BufferedInputStream(conn.getInputStream());
        }
    }

    private class URLDrawable extends BitmapDrawable {
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
