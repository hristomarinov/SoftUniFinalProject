package marinov.hristo.softuniproject.main;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

import marinov.hristo.softuniproject.R;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Activity activity;
    private List<Data> datas;
    private ImageLoader imageLoader;
    private IRecycleViewMain mListener;

    class ViewHolder extends RecyclerView.ViewHolder {

        int position;
        ImageView image, play_circle, fav_icon;
        TextView description, tag, type;
        LinearLayout type_layout;
        RelativeLayout line_elements;

        public void setItemPosition(int position) {
            this.position = position;
        }

        public ViewHolder(final View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.image_news);
            fav_icon = (ImageView) itemView.findViewById(R.id.fav_icon_news);
            play_circle = (ImageView) itemView.findViewById(R.id.play_circle);
            line_elements = (RelativeLayout) itemView.findViewById(R.id.bottom_line_news);
            type_layout = (LinearLayout) itemView.findViewById(R.id.type_news_layout);
            type = (TextView) itemView.findViewById(R.id.type_news);
            description = (TextView) itemView.findViewById(R.id.description_news);
            tag = (TextView) itemView.findViewById(R.id.tag_news);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onArticleSelected(position);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu(description, position);
                    return true;
                }
            });
        }
    }

    public NewsAdapter(Activity activity, List<Data> objects, IRecycleViewMain listener) {
        this.activity = activity;
        this.datas = objects;
        this.mListener = listener;
        this.imageLoader = ImageLoader.getInstance();
        this.imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        final Data data = datas.get(viewType);

        LayoutInflater inflater = activity.getLayoutInflater();

        if (data.isVideo || data.isFeatured || viewType == 0) {
            view = inflater.inflate(R.layout.list_item_media, parent, false);
        } else {
            view = inflater.inflate(R.layout.list_item_article, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder != null) {
            final Data data = datas.get(position);

            imageLoader.displayImage(data.imageUrl, holder.image);
            holder.description.setText(data.description);
            holder.description.setTag(data.id);

            if (data.isVideo) {
                holder.type_layout.setVisibility(View.VISIBLE);
                holder.type.setText(R.string.video);
                holder.tag.setText("");
                holder.type_layout.setBackgroundColor(ContextCompat.getColor(activity, R.color.video));
            } else if (data.isFeatured) {
                holder.type_layout.setVisibility(View.GONE);
                holder.type.setText("");
                holder.tag.setText("");
                holder.type_layout.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));
            } else {
                holder.type_layout.setVisibility(View.GONE);
                holder.type.setText("");
                if (position > 0) {
                    holder.tag.setText(data.tag);
                } else {
                    holder.tag.setText("");
                }
                holder.type_layout.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));
            }

            holder.fav_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFavouriteSelected(holder.getAdapterPosition());
                }
            });

            holder.setItemPosition(position);
        }
    }

    /**
     * Showing popup menu
     */
    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(activity, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        int position;

        public MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.open_article:
                    mListener.onArticleSelected(position);

                    return true;
                case R.id.action_add_favourites:
                    mListener.onFavouriteSelected(position);

                    return true;
                default:
            }
            return false;
        }
    }
}
