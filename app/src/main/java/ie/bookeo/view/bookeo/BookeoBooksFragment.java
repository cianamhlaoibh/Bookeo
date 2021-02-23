package ie.bookeo.view.bookeo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.bookeo.BookeoAlbumBookAdapter;
import ie.bookeo.adapter.bookeo.BookeoMainFolderAdapter;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.MarginItemDecoration;

public class BookeoBooksFragment extends Fragment {
    RecyclerView rvBooks;
    TextView tvEmpty;
    private BookeoAlbumBookAdapter bookeoAlbumBookAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference albumsRef = db.collection("albums");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookeo_album_books, container, false);
        rvBooks = root.findViewById(R.id.rvBooks);
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<BookeoAlbum> albums = getAlbums();
        bookeoAlbumBookAdapter = new BookeoAlbumBookAdapter(albums, getContext());
        rvBooks.setAdapter(bookeoAlbumBookAdapter);
        bookeoAlbumBookAdapter.notifyDataSetChanged();
    }
    public ArrayList<BookeoAlbum> getAlbums() {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("fk_user", userId).whereEqualTo("generated", true).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    String data = "";

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);

                                final BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());
                                arAlbum.setFirstItem(album.getFirstItem());


                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                        }
                        bookeoAlbumBookAdapter.notifyDataSetChanged();
                    }
                });
        return dbAlbums;
    }
}
