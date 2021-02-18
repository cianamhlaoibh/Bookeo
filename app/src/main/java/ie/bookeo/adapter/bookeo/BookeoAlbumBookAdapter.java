package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.view.bookeo.BookeoBook;

public class BookeoAlbumBookAdapter extends  RecyclerView.Adapter<BookeoAlbumBookHolder> {
    private List<BookeoAlbum> arAlbums;
    private Context contx;

    public BookeoAlbumBookAdapter(List<BookeoAlbum> albums, Context contx) {
        this.arAlbums = albums;
        this.contx = contx;
    }

    @NonNull
    @Override
    public BookeoAlbumBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_book, parent, false);
        return new BookeoAlbumBookHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoAlbumBookHolder holder, int position) {
        final BookeoAlbum album = arAlbums.get(position);
        Glide.with(contx)
                .load(album.getFirstItem())
                .apply(new RequestOptions().centerCrop())
                .into(holder.ivCover);
        holder.tvTitle.setText(album.getName());
        holder.tvDate.setText(album.getCreateDate());
        holder.ivArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(contx, BookeoBook.class);
                intent.putExtra("albumUuid", album.getUuid());
                intent.putExtra("isGenerated", album.getGenerate());
                contx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arAlbums.size();
    }
}
