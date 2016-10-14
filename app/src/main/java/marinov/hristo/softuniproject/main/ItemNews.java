package marinov.hristo.softuniproject.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.utils.ImageURLParser;
import marinov.hristo.softuniproject.utils.RoundedImageView;

/**
 * @author Hristo Marinov (christo_marinov@abv.bg).
 */
public class ItemNews extends AppCompatActivity {

    float scale;
    Context context;
    String getID, text = "";
    TextView title_news, item_description_news, copyright_news, date_news, source_news, item_text_news, item_caption_news;
    ImageView item_image_news;
    LinearLayout authorLayout, item_text_news_layout;
    ArrayList<Data> datas = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>(),
            images = new ArrayList<>(),
            bios = new ArrayList<>();
    ShareButton shareButton;
    private ArticleData articleData;
    private CallbackManager callbackManager;
    public ImageLoader imageLoader;
    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_NAME = "name";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_COPYRIGHT = "copyright";
    private static final String TAG_BIO = "bio";
    private static final String TAG_AUTHORS = "authors";
    private static final String TAG_SOURCES = "source";
    private static final String TAG_PUBLISHED_AT = "publishedAt";
    private static final String TAG_PHOTO = "photo";
    private static final String TAG_SUMMARY = "summary";
    private static final String TAG_TEXT = "text";
    private static final String TAG_CAPTION = "caption";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Bundle extras = getIntent().getExtras();
        getID = extras.getString("getID", "unknown");

        title_news = (TextView) findViewById(R.id.title_news);
        item_caption_news = (TextView) findViewById(R.id.item_caption_news);
        item_description_news = (TextView) findViewById(R.id.item_description_news);
        copyright_news = (TextView) findViewById(R.id.copyright_news);
        date_news = (TextView) findViewById(R.id.date_news);
        source_news = (TextView) findViewById(R.id.source_news);
        item_text_news = (TextView) findViewById(R.id.item_text_news);
        item_image_news = (ImageView) findViewById(R.id.item_image_news);
        authorLayout = (LinearLayout) findViewById(R.id.authorLayout);
        shareButton = (ShareButton) findViewById(R.id.shareFb);
        item_text_news_layout = (LinearLayout) findViewById(R.id.item_text_news_layout);

        context = getApplicationContext();
        scale = getResources().getDisplayMetrics().density;
        datas = new ArrayList<>();

        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(ItemNews.this));
        }

        if (!getID.equals(getString(R.string.unknown))) {
            new getArticle().execute();
        } else {
            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }

        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        //this loginManager helps you eliminate adding a LoginButton to your UI
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithPublishPermissions(this, permissionNeeds);
        loginManager.registerCallback(callbackManager, facebookCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        reloadArticleText();
    }

    public void reloadArticleText() {
        item_text_news_layout.removeAllViews();

        item_text_news = new TextView(context);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        item_text_news.setLayoutParams(llp);
        item_text_news.setLineSpacing(1.2f, 1f);
        item_text_news.setPadding(8, 6, 8, 0);
        item_text_news.setTextSize(14);
        item_text_news.setTextColor(getResources().getColor(R.color.black));
        item_text_news_layout.addView(item_text_news);

        setTextViewHTML(item_text_news, text);
    }

    private class getArticle extends AsyncTask<Void, Void, Void> {
        int HttpResponse;
        String title = "", photo = "", copyright = "", publishedAt = "", summary = "", caption = "", sources = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            articleData = new ArticleData();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString;
                        try {
                            URL url = new URL("http://www.haveglobe.com/api/article/" + getID + ".json");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");

                            HttpResponse = conn.getResponseCode();
                            if (HttpResponse == 200) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                String inputLine;
                                StringBuilder response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();
                                dataString = response.toString();

                                if (!dataString.equals("")) {
                                    try {
                                        JSONObject jsonObj = new JSONObject(dataString);

                                        title = jsonObj.optString(TAG_TITLE);
                                        copyright = jsonObj.optString(TAG_COPYRIGHT);

                                        //Get Authors
                                        JSONArray authors = jsonObj.optJSONArray(TAG_AUTHORS);
                                        if (authors != null) {
                                            for (int i = 0; i < authors.length(); i++) {
                                                JSONObject obj = authors.getJSONObject(i);
                                                String name = obj.optString(TAG_NAME);
                                                String image = obj.optString(TAG_IMAGE);
                                                String bio = obj.optString(TAG_BIO);

                                                names.add(name);
                                                images.add(image);
                                                bios.add(bio);
                                            }
                                        }

                                        //Get Sources
                                        sources = jsonObj.optString(TAG_SOURCES);
                                        if (sources.length() > 0) {
                                            sources = "Sources: " + sources;
                                        }
                                        publishedAt = jsonObj.optString(TAG_PUBLISHED_AT);
                                        summary = jsonObj.optString(TAG_SUMMARY);
                                        text = jsonObj.optString(TAG_TEXT);
                                        caption = jsonObj.optString(TAG_CAPTION);
                                        photo = jsonObj.optString(TAG_PHOTO);

                                        articleData.title = title;
                                        articleData.imageUrl = photo;
                                        articleData.caption = caption;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("HTTPCLIENT" + e.getLocalizedMessage());
                        }
                    }
                });
                thread.start();
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ViewGroup.LayoutParams params = date_news.getLayoutParams();
            params.height = (int) (scale * 30);
            date_news.setLayoutParams(params);

            //Set circle author pictures with names
            if (names.size() > 0) {
                createAuthorView(authorLayout);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                title_news.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
                item_caption_news.setText(Html.fromHtml(caption, Html.FROM_HTML_MODE_LEGACY));
                item_description_news.setText(Html.fromHtml(summary, Html.FROM_HTML_MODE_LEGACY));
                copyright_news.setText(Html.fromHtml(copyright, Html.FROM_HTML_MODE_LEGACY));
                date_news.setText(Html.fromHtml(publishedAt, Html.FROM_HTML_MODE_LEGACY));
            } else {
                title_news.setText(Html.fromHtml(title));
                item_caption_news.setText(Html.fromHtml(caption));
                item_description_news.setText(Html.fromHtml(summary));
                copyright_news.setText(Html.fromHtml(copyright));
                date_news.setText(Html.fromHtml(publishedAt));
            }
            if (copyright.length() == 0) {
                copyright_news.setVisibility(View.GONE);
            }

            source_news.setText(Html.fromHtml(sources));

            setTextViewHTML(item_text_news, text);

            // Prevent not fully showing of the article content
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    item_text_news.setMaxLines(Integer.MAX_VALUE);
                }
            }, 2500);

            imageLoader.displayImage(photo, item_image_news);
            sharePhotoToFacebook();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    protected void createAuthorView(LinearLayout parent) {
        parent.removeAllViews();

        for (int t = 0; t < images.size(); t++) {
            LinearLayout authorBox = new LinearLayout(context);

            authorBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            authorBox.setOrientation(LinearLayout.HORIZONTAL);
            authorBox.setClickable(true);
            parent.addView(authorBox);

            if (t == 0) {
                createAuthorName("by ", authorBox, false);
            } else {
                createAuthorName("     ", authorBox, false);
            }

            RoundedImageView authorImage = new RoundedImageView(this);
            RelativeLayout.LayoutParams paramsFlagBox;
            paramsFlagBox = new RelativeLayout.LayoutParams((int) (scale * 30), (int) (scale * 30));
            authorImage.setLayoutParams(paramsFlagBox);
            authorImage.setClickable(false);
            authorImage.setId(R.id.roundImageView);
            authorImage.setTag(names.get(t));
            imageLoader.displayImage(images.get(t), authorImage);
            authorBox.addView(authorImage);

            if (t < (names.size() - 1)) {
                createAuthorName(names.get(t) + ", ", authorBox, false);
            } else {
                createAuthorName(names.get(t), authorBox, false);
            }

            authorBox.setTag(bios.get(t));
        }
    }

    /**
     * Create TextView and insert into specified layout
     *
     * @param name   text in the box
     * @param parent layout in which you want to add
     * @param last   is the TextView last for the parent layout
     */
    protected void createAuthorName(String name, LinearLayout parent, Boolean last) {
        TextView authorName = new TextView(context);
        authorName.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) (scale * 30));
        if (!last) {
            llp.setMargins(5, 0, 0, 0);
        }
        authorName.setLayoutParams(llp);
        authorName.setTextSize(13);
        authorName.setClickable(false);
        authorName.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
        authorName.setText(name);
        parent.addView(authorName);
    }

    protected void setTextViewHTML(TextView text, String html) {
        ImageURLParser image = new ImageURLParser(item_text_news, context, this);
        CharSequence sequence = Html.fromHtml(html, image, null);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    FacebookCallback facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException exception) {
            if (exception instanceof FacebookAuthorizationException) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut();
                }
            }
        }
    };

    private void sharePhotoToFacebook() {

        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle(articleData.title)
                .setContentDescription(articleData.caption)
                .setImageUrl(Uri.parse(articleData.imageUrl))
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();

        shareButton.setShareContent(linkContent);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }
}