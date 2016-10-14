package marinov.hristo.softuniproject.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import marinov.hristo.softuniproject.MyApplication;
import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.account.AccountActivity;
import marinov.hristo.softuniproject.favourites.FavouritesActivity;
import marinov.hristo.softuniproject.models.Favourites;
import marinov.hristo.softuniproject.models.User;
import marinov.hristo.softuniproject.utils.ConnectivityChangeReceiver;
import marinov.hristo.softuniproject.utils.DeveloperKey;
import marinov.hristo.softuniproject.utils.MyService;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class MainActivity extends AppCompatActivity implements IRecycleViewMain, ConnectivityChangeReceiver.ConnectivityReceiverListener {

    int page = 1, countJson;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    ArrayList<Data> datas = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();
    Intent serviceIntent;
    Editor editorLogin;
    SharedPreferences prefLogin;
    private MyService myService;
    private boolean loading = true;
    private ProgressBar spinner;
    private NewsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private static final String TAG = MainActivity.class.getName();
    // JSON Node names
    private static final String TAG_NEWS = "news";
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_TYPE = "type";
    private static final String TAG_PHOTO = "photo";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_YOUTUBE_ID = "youtubeId";
    private static final String TAG_FEATURED = "isFeatured";
    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        spinner = (ProgressBar) findViewById(R.id.news_spinner);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewNews);

        prefLogin = getApplicationContext().getSharedPreferences("activity_login", MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();

        datas = new ArrayList<>();

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsAdapter(this, datas, this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Check for scroll down
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            onLoadMoreItems();
                        }
                    }
                }
            }
        });

        if (ConnectivityChangeReceiver.isConnected()) {
            new getNews().execute();
        } else {
            showSnack(ConnectivityChangeReceiver.isConnected());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        onServiceStarted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favourites:
                Intent favouriteIntent = new Intent(this, FavouritesActivity.class);
                startActivity(favouriteIntent);

                return true;
            case R.id.account:
                Intent loginIntent = new Intent(this, AccountActivity.class);
                startActivity(loginIntent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onServiceStopped();

        page = 1;
        datas.clear();
    }

    private void onLoadMoreItems() {
        if (ConnectivityChangeReceiver.isConnected()) {
            new getNews().execute();
        } else {
            showSnack(ConnectivityChangeReceiver.isConnected());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFavouriteSelected(int position) {
        saveToFavourites(position);
    }

    @Override
    public void onArticleSelected(int position) {
        Data article = datas.get(position);

        if (article.isVideo || article.isFlash) {
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, DeveloperKey.DEVELOPER_KEY, article.youtubeId, 0, true, false);

            if (intent != null) {
                if (canResolveIntent(intent)) {
                    startActivityForResult(intent, REQ_START_STANDALONE_PLAYER);
                } else {
                    // Could not resolve the intent - must need to install or update the YouTube API service.
                    YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(this, REQ_RESOLVE_SERVICE_MISSING).show();
                }
            }
        } else {
            String id = article.id;
            Intent intent = new Intent(this, ItemNews.class);
            intent.putExtra("getID", id);
            startActivity(intent);
        }
    }

    /**
     * saveToFavourites - Save selected item in SQLite DB in table "Favourites"
     *
     * @param position list position
     */
    public void saveToFavourites(int position) {
        long userId = prefLogin.getLong("userId", -1);

        User user = User.findById(User.class, userId);
        if (user != null) {
            // Get all Favourite items for the current user
            List<Favourites> favouriteList = Favourites.find(Favourites.class, "user =?", String.valueOf(userId));
            Data data = datas.get(position);

            Favourites favItem = containsId(favouriteList, data.id);
            if (favItem == null) {
                Favourites favourites = new Favourites(user, data.id, data.description, data.imageUrl);
                favourites.save();

                Toast.makeText(this, R.string.added_to_fav, Toast.LENGTH_SHORT).show();
                myService.showNotification(data.id, getString(R.string.favourites), data.description);
            } else {
                int favIndex = favouriteList.indexOf(favItem);

                final Favourites favourite = favouriteList.get(favIndex);
                favourite.delete();
                favourite.save();
                favouriteList.remove(favItem);

                Toast.makeText(this, R.string.already_added, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.login_need, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the article already exist in the list
     *
     * @param list FavouriteList
     * @param id   selected article ID
     * @return Favourites object if exist, otherwise null
     */
    public static Favourites containsId(List<Favourites> list, String id) {
        for (Favourites object : list) {
            if (object.getArticleId().equals(id))
                return object;
        }
        return null;
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder binder = (MyService.ServiceBinder) service;

            myService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void onServiceStarted() {
        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MyService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    public void onServiceStopped() {
        if (serviceIntent == null)
            serviceIntent = new Intent(this, MyService.class);

        myService = null;
        unbindService(conn);
        stopService(serviceIntent);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private class getNews extends AsyncTask<Void, Void, Void> {
        int HttpResponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String dataString;
                        JSONArray news;
                        try {
                            URL url = new URL("http://www.haveglobe.com/api/news.json?page=" + page);
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
                                        news = jsonObj.optJSONArray(TAG_NEWS);
                                        countJson = news.length();

                                        if (countJson > 0) {
                                            for (int t = 0; t < news.length(); t++) {
                                                JSONObject c = news.getJSONObject(t);
                                                String id = c.optString(TAG_ID);
                                                String title = c.optString(TAG_TITLE);

                                                Boolean isVideo = false;
                                                Boolean isFlash = false;

                                                String photo = c.optString(TAG_PHOTO);
                                                String type = c.optString(TAG_TYPE);
                                                String tag = c.optString(TAG_CATEGORY);
                                                String youtubeId = c.optString(TAG_YOUTUBE_ID);

                                                if (type.equals("slide") || type.equals("comics"))
                                                    continue;

                                                switch (type) {
                                                    case "daily":
                                                        isVideo = true;
                                                        break;
                                                    case "flash":
                                                        isFlash = true;
                                                        break;
                                                }

                                                Boolean isFeatured = c.optBoolean(TAG_FEATURED);

                                                Data data = new Data();
                                                data.id = id;
                                                data.imageUrl = photo;
                                                data.description = title;
                                                data.isFeatured = isFeatured;
                                                data.isVideo = isVideo;
                                                data.isFlash = isFlash;
                                                data.tag = tag;
                                                data.youtubeId = youtubeId;

                                                datas.add(data);
                                                ids.add(id);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            Log.d(TAG, e.getLocalizedMessage());
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

            if (HttpResponse == 200) {
                mAdapter.notifyDataSetChanged();

                loading = true;
                page++;
            } else {
                loading = false;
            }

            mRecyclerView.invalidate();
            spinner.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            spinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode != Activity.RESULT_OK) {
            YouTubeInitializationResult errorReason = YouTubeStandalonePlayer.getReturnedInitializationResult(data);
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(this, 0).show();
            }
        }
    }

    /**
     * Showing the Internet status in Snackbar
     *
     * @param isConnected true if connected, otherwise false
     */
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = getString(R.string.connected);
            color = Color.GREEN;
        } else {
            message = getString(R.string.not_connected);
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.recyclerViewNews), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private boolean canResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }
}
