package ie.bookeo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ie.bookeo.R;
import ie.bookeo.model.BookeoAlbum;
import ie.bookeo.utils.AlbumUploadListener;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 * This is the adapter class for the FolderViewActivity Bookeo Album Recycler View that populates a RecyclerView with folders from firestore.
 * This class also contain the Folder View Holder which represent a folder on the screen
 */

public class BookeoFolderAdapter extends RecyclerView.Adapter<BookeoFolderHolder> {

    private List<BookeoAlbum> arAlbums;
    private Context contx;
    private AlbumUploadListener albumUploadListener;

    public BookeoFolderAdapter(List<BookeoAlbum> albums, Context folderContx, AlbumUploadListener listenr) {
        this.arAlbums = albums;
        Log.d("SIZE", "BookeoFolderAdapter: "+arAlbums.size());
        this.contx = folderContx;
        this.albumUploadListener = listenr;
    }
    @NonNull
    @Override
    public BookeoFolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.album_thumbnail_holder_item, parent, false);
        return new BookeoFolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoFolderHolder holder, int position) {
        final BookeoAlbum album = arAlbums.get(position);

        //setting the number of images
        String text = ""+album.getName();
        holder.etAlbumName.setText(text);

        holder.albumCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               albumUploadListener.onUploadAlbumClicked(album.getUuid());
            }
        });

    }

    @Override
    public int getItemCount() {
        return arAlbums.size();
    }
}
