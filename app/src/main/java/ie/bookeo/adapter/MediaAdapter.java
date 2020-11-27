package ie.bookeo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;
import ie.bookeo.model.MediaItem;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.setTransitionName;
/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 * This is the adapter class for  the MediaDisplayActivty Recycler View that populates a RecyclerView with images.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapterHolder> {

    private ArrayList<MediaItem> arMediaList;
    private Context contx;
    private final MediaDisplayItemClickListener mediaDisplayItemListener;

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

        Glide.with(contx)
                .load(image.getPath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        setTransitionName(holder.picture, String.valueOf(position) + "_image");

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDisplayItemListener.onPicClicked(holder,position, arMediaList,null);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arMediaList.size();
    }
}
