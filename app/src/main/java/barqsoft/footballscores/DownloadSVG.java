package barqsoft.footballscores;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Phil Robertson on 10/19/2015.
 *
 *     NOTE: class is not used at this point as many of the SVG images returned in the link were
 *         not parsable by sgv android...if a better parser is available, this class can be used
 *         as a basis to fetch, parse and cache images
 */
public class DownloadSVG extends AsyncTask<String, Void, Drawable> {
    static final String LOG_TAG = DownloadSVG.class.getSimpleName();

    ImageView mTargetImage;

    public DownloadSVG (ImageView targetImage) {
        mTargetImage = targetImage;
    }

    protected Drawable doInBackground (String... urls) {
        Drawable result   = null;
        String   crestUrl = fetchCrestUrl(urls[0]);
        if (crestUrl != null) {
            try {
                final URL imageUrl = new URL(crestUrl);
                //"https://upload.wikimedia.org/wikipedia/de/e/e1/FCO_Dijon.svg");
                //"http://upload.wikimedia.org/wikipedia/de/d/d7/RC_Lens_Logo.svg");//urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) imageUrl.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                // SVG svg = SVGParser.getSVGFromInputStream(inputStream);
                // result = svg.createPictureDrawable();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return result;
    }

    protected void onPostExecute (Drawable result) {
        // this leaves the default image in place if the image can't be fetched
        if (result != null) {
            mTargetImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mTargetImage.setImageDrawable (result);
        }
    }

    private String fetchCrestUrl (String teamUrl) {
        final String LOG_TAG_M = LOG_TAG + ".fetchCrestUrl()";

        HttpURLConnection connection = null;
        BufferedReader    reader     = null;
        String            result     = null;


        //Opening Connection
        try {
            URL fetch = new URL(teamUrl);
            connection = (HttpURLConnection) fetch.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
                buffer.append("\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            result = buffer.toString();
            Log.d(LOG_TAG_M, "http response ==> " + result);
            JSONObject teamJSON = new JSONObject (result);
            result = teamJSON.getString("crestUrl");
            Log.d(LOG_TAG_M, "crestUrl ==> " + result);
            return result;
        } catch (Exception e) {
            Log.e(LOG_TAG_M, "exception ==> " + e.getMessage());
        } finally {
            if(connection != null)  {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e (LOG_TAG_M, "exception ==> " + e.getMessage());
                }
            }
        }

    return result;
    }
}