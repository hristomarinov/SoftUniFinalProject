package marinov.hristo.softuniproject.favourites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.main.ItemNews;
import marinov.hristo.softuniproject.models.Favourites;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class FavouritesActivity extends AppCompatActivity implements IRecycleViewFavourites, OnStartDragListener {

    long userId;
    Editor editorLogin;
    SharedPreferences prefLogin;
    RecyclerView recyclerView;
    FavouritesAdapter adapter;
    List<Favourites> favourites = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        
        setTitle(getString(R.string.favourites));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        prefLogin = getApplicationContext().getSharedPreferences("activity_login", MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();

        userId = prefLogin.getLong("userId", -1);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        // Get all Favourite items for the current user
        favourites = Favourites.find(Favourites.class, "user =?", String.valueOf(userId));

        adapter = new FavouritesAdapter(favourites, this, this, this);
        recyclerView.setAdapter(adapter);


        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter, favourites, recyclerView, this);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

//        recyclerView.setOnFlingListener(new RecyclerViewSwipeListener(true) {
//            @Override
//            public boolean onFling(int velocityX, int velocityY) {
////                System.out.println(velocityX + "    " + velocityY);
//                return super.onFling(velocityX, velocityY);
//            }
//        });
    }

    @Override
    public void onItemSelected(int position) {
        String id = favourites.get(position).getArticleId();
        Intent intent = new Intent(this, ItemNews.class);
        intent.putExtra("getID", id);
        startActivity(intent);
    }

    @Override
    public void onArticleRemove(final int position) {
        final Favourites favourite = favourites.get(position);
        favourites.remove(position);
        adapter.notifyItemRemoved(position);

        favourite.delete();

        Snackbar.make(recyclerView, getString(R.string.item_deleted), Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        favourite.save();
                        favourites.add(position, favourite);
                        adapter.notifyItemInserted(position);

                    }
                })
                .show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
