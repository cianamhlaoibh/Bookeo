package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.utils.MediaDisplayItemClickListener;

public class BookeoMediaItemAdapter extends  RecyclerView.Adapter<MediaAdapterHolder>  {

    private ArrayList<BookeoMediaItem> arMediaList;
    private Context contx;
    private final MediaDisplayItemClickListener mediaDisplayItemListener;

    public BookeoMediaItemAdapter(ArrayList<BookeoMediaItem> arMediaList, Context contx, MediaDisplayItemClickListener mediaDisplayItemListener) {
        this.arMediaList = arMediaList;
        this.contx = contx;
        this.mediaDisplayItemListener = mediaDisplayItemListener;
    }

    @NonNull
    @Override
    public MediaAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.media_item_holder, parent, false);
        return new MediaAdapterHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaAdapterHolder holder, final int position) {
        final BookeoMediaItem image = arMediaList.get(position);

        final ArrayList<String> urls = new ArrayList<>();
        for (BookeoMediaItem p : arMediaList) {
            urls.add(p.getUrl());
        }

        final ArrayList<String> names = new ArrayList<>();
        for (BookeoMediaItem p : arMediaList) {
            names.add(p.getName());
        }

        final ArrayList<String> uuids = new ArrayList<>();
        for (BookeoMediaItem p : arMediaList) {
            uuids.add(p.getUuid());
        }

        Glide.with(contx)
                .load(image.getUrl())
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaDisplayItemListener == null)return;
               mediaDisplayItemListener.onBPicClicked(holder, position, names, urls, uuids, image.getAlbumUuid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arMediaList.size();
    }
}
