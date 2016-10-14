package marinov.hristo.softuniproject.favourites;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.models.Favourites;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private Activity activity;
    private RecyclerView recyclerView;
    private List<Favourites> favouritesList;
    private final FavouritesAdapter mAdapter;

    public ItemTouchHelperCallback(FavouritesAdapter adapter, List<Favourites> favourites, RecyclerView recyclerView, Activity activity) {
        this.mAdapter = adapter;
        this.favouritesList = favourites;
        this.recyclerView = recyclerView;
        this.activity = activity;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final Favourites favourite = favouritesList.get(viewHolder.getAdapterPosition());
        favouritesList.remove(viewHolder.getAdapterPosition());
        mAdapter.notifyItemRemoved(position);

        favourite.delete();

        Snackbar.make(recyclerView, activity.getString(R.string.item_deleted), Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        favourite.save();
                        favouritesList.add(position, favourite);
                        mAdapter.notifyItemInserted(position);

                    }
                })
                .show();
    }

}