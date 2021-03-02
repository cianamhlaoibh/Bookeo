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

    public BookeoCreateFolderAdapter(List<BookeoAlbum> albums, Context folderContx, AddAlbumListener lisenter) {
        this.arAlbums = albums;
        this.contx = folderContx;
       this.lisenter = lisenter;
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
                arAlbums = null;
                arAlbums = getSubFolders(album.getUuid());
            }
        });

        holder.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lisenter.addAlbum(album);
            }
        });
    }

    private ArrayList<BookeoAlbum> getSubFolders(String parentUuid) {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("parent", parentUuid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    String data = "";

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);

                                BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());

                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
        Log.d("SIZE", "getAlbums: added" + dbAlbums.size());
        return dbAlbums;
    }

    @Override
    public int getItemCount() {
        return arAlbums.size();
    }
}
