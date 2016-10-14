package marinov.hristo.softuniproject.favourites;

import android.app.Activity;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Collections;
import java.util.List;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.models.Favourites;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private Activity activity;
    private ImageLoader imageLoader;
    private IRecycleViewFavourites mListener;
    private List<Favourites> favouritesList;
    private final OnStartDragListener mDragStartListener;

    class ViewHolder extends RecyclerView.ViewHolder {

        int position;
        TextView description;
        ImageView imageView, reorderIcon;

        public void setItemPosition(int position) {
            this.position = position;
        }

        public ViewHolder(View itemView) {
            super(itemView);

            description = (TextView) itemView.findViewById(R.id.fav_description_news);
            imageView = (ImageView) itemView.findViewById(R.id.fav_image_news);
            reorderIcon = (ImageView) itemView.findViewById(R.id.move_icon);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                        mListener.onItemSelected(position);
                    }
                    return true;
                }
            });
        }
    }

    public FavouritesAdapter(List<Favourites> favouritesList, IRecycleViewFavourites listener, Activity activity, OnStartDragListener dragStartListener) {
        this.favouritesList = favouritesList;
        this.mListener = listener;
        this.activity = activity;
        this.imageLoader = ImageLoader.getInstance();
        this.imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
        this.mDragStartListener = dragStartListener;
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favourites, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (holder != null) {
            holder.description.setText(favouritesList.get(position).getArticleName());
            imageLoader.displayImage(favouritesList.get(position).getImageURI(), holder.imageView);

            holder.reorderIcon.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_DOWN) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        return super.onDoubleTap(e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            holder.setItemPosition(position);
        }
    }


    @Override
    public void onItemDismiss(int position) {
        favouritesList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(favouritesList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}
