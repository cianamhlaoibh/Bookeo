package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import ie.bookeo.R;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.view.bookeo.BookeoBook;

public class BookeoPagesAdapter extends RecyclerView.Adapter<BookeoPageHolder>  {
    private List<BookeoMediaItem> items;
    private Context contx;

    public BookeoPagesAdapter(List<BookeoMediaItem> items, Context contx) {
        this.items = items;
        this.contx = contx;
    }

    @NonNull
    @Override
    public BookeoPageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_book_page, parent, false);
        return new BookeoPageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoPageHolder holder, int position) {
        final BookeoMediaItem item = items.get(position);
        Glide.with(contx)
                .load(item.getUrl())
                .fitCenter()
                .into(holder.ivMedia);
        String caption = item.getCaption();
        if (caption != null) {
            holder.tvCaption.setText(caption);
        }
        holder.ivMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - EDIT IMAGE
            }
        });
        //TODO - GENERATE QR CODE - IF VIDEO CLIP
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
