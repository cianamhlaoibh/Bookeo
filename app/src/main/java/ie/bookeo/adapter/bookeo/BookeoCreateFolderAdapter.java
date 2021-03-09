package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.utils.AddAlbumListener;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.ItemClickListener;
import ie.bookeo.utils.SubFolderResultListener;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 * This is the adapter class for the MainActivity Bookeo Album Recycler View that populates a RecyclerView with folders from firestore.
 * This class also contain the Folder View Holder which represent a folder on the screen
 */

public class BookeoCreateFolderAdapter extends RecyclerView.Adapter<BookeoCreateFolderHolder> {

    private List<BookeoAlbum> arAlbums;
    private Context contx;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AddAlbumListener lisenter;
    private AlbumUploadListener uploadListener;
    private ItemClickListener clickListener;

    public BookeoCreateFolderAdapter(List<BookeoAlbum> albums, Context folderContx, AddAlbumListener lisenter, AlbumUploadListener uploadListener, ItemClickListener clickListener) {
        this.arAlbums = albums;
        this.contx = folderContx;
       this.lisenter = lisenter;
        this.uploadListener = uploadListener;
        this.clickListener = clickListener;

    }
    @NonNull
    @Override
    public BookeoCreateFolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_folder, parent, false);
        return new BookeoCreateFolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoCreateFolderHolder holder, int position) {
        final BookeoAlbum album = arAlbums.get(position);

        //setting the number of images
        String text = ""+album.getName();
        holder.tvFolderName.setText(text);

        holder.tvFolderName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(album.getUuid());
            }
        });

        holder.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lisenter.addAlbum(album);
            }
        });

        holder.ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadListener.onUploadAlbumClicked(album.getUuid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return arAlbums.size();
    }

    //This implemented as adapter.notifyDataSetChanged not working in Fragment
    public void updateDataSet(ArrayList<BookeoAlbum> albums) {
        arAlbums.clear();
        arAlbums.addAll(albums);
        notifyDataSetChanged();
    }
}
