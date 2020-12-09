package ie.bookeo.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;
import ie.bookeo.model.gallery_model.MediaItem;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.view.ViewCompat.setTransitionName;
/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  - URL - https://medium.com/better-programming/gmail-like-list-67bc51adc68a
 *  - Creator - Mustufa Ansari
 *  - Modified by Cian O Sullivan
 *
 * This is the adapter class for  the MediaDisplayActivty Recycler View that populates a RecyclerView with images.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapterHolder> {

    private ArrayList<MediaItem> arMediaList;
    private Context contx;
    private final MediaDisplayItemClickListener mediaDisplayItemListener;
    private MediaAdapterHolder mediaAdapterHolder;

    private SparseBooleanArray selectedItems;
    private ArrayList<MediaItem> arSelectedItems;
    private int selectedIndex = -1;

    /**
     *
     * @param arMediaList ArrayList of pictureFacer objects
     * @param contx The Activities Context
     * @param mediaDisplayItemListener An interface for listening to clicks on the RecyclerView's items
     */
    public MediaAdapter(ArrayList<MediaItem> arMediaList, Context contx, MediaDisplayItemClickListener mediaDisplayItemListener) {
        this.arMediaList = arMediaList;
        this.contx = contx;
        this.mediaDisplayItemListener = mediaDisplayItemListener;
        selectedItems = new SparseBooleanArray();
        arSelectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell = inflater.inflate(R.layout.pic_holder_item, container, false);
        return new MediaAdapterHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaAdapterHolder holder, final int position) {

        final MediaItem image = arMediaList.get(position);

        final ArrayList<String> paths = new ArrayList<>();
        for (MediaItem p : arMediaList) {
            paths.add(p.getPath());
        }

        Glide.with(contx)
                .load(image.getPath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        setTransitionName(holder.picture, String.valueOf(position) + "_image");

        //Changes the activated state of this view.
        holder.picture.setActivated(selectedItems.get(position, false));

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaDisplayItemListener == null)return;
                    mediaDisplayItemListener.onPicClicked(holder, position, paths, null);
            }
        });
        holder.picture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mediaDisplayItemListener == null) {
                    return false;
                } else {
                    mediaDisplayItemListener.onLongPress(holder, arMediaList.get(position), position);
                    return true;
                }
            }
        });

        toggleIcon(holder, position);

    }

    /*
       This method will trigger when we we long press the item and it will change the icon of the item to check icon.
     */
    public void toggleIcon(MediaAdapterHolder holder, int position) {
        if (selectedItems.get(position, false)) {
            //bi.lytImage.setVisibility(View.GONE);
            holder.ivCheck.setVisibility(View.VISIBLE);
            if (selectedIndex == position) selectedIndex = -1;
        } else {
            //bi.lytImage.setVisibility(View.VISIBLE);
            holder.ivCheck.setVisibility(View.GONE);
            if (selectedIndex == position) selectedIndex = -1;
        }
    }

    public void removeIcons() {

    }

    /*
       This method helps you to get all selected items from the list
     */

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public List<MediaItem> getUploadItems() {
        return arSelectedItems;
    }

    /*
       this will be used when we want to delete items from our list
     */
    public void removeItems(int position) {
        arMediaList.remove(position);
        selectedIndex = -1;

    }

    /*
       for clearing our selection
     */

    public void clearSelection() {
        selectedItems.clear();

        notifyDataSetChanged();
    }

    /*
             this function will toggle the selection of items
     */

    public void toggleSelection(int position) {
        selectedIndex = position;
        if (selectedItems.get(position, false)) {
            arSelectedItems.remove(position);
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
            arSelectedItems.add(arMediaList.get(position));
        }
        notifyItemChanged(position);
    }

    /*
      How many items have been selected? this method exactly the same . this will return a total number of selected items.
     */

    public int selectedItemCount() {
        return selectedItems.size();
    }
    @Override
    public int getItemCount() {
        return arMediaList.size();
    }
}
