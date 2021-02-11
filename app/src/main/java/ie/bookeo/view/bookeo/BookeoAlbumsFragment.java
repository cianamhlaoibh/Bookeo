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
import ie.bookeo.adapter.bookeo.BookeoMainFolderAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;

public class BookeoAlbumsFragment extends Fragment implements MediaDisplayItemClickListener {
    RecyclerView rvAlbums;
    TextView tvEmpty;
    private BookeoMainFolderAdapter bookeoMainFolderAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference albumsRef = db.collection("albums");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookeo_albums, container, false);
        rvAlbums = root.findViewById(R.id.rvBookeoAlbums);
        rvAlbums.addItemDecoration(new MarginItemDecoration(getContext()));
        rvAlbums.hasFixedSize();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<BookeoAlbum> albums = getAlbums();
        bookeoMainFolderAdapter = new BookeoMainFolderAdapter(albums, getContext(),this);
        rvAlbums.setAdapter(bookeoMainFolderAdapter);
    }
    public ArrayList<BookeoAlbum> getAlbums() {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("fk_user", userId).get()
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
                        bookeoMainFolderAdapter.notifyDataSetChanged();
                    }
                });
        return dbAlbums;
    }
    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> paths, Context contx) {

    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {

    }

    /**
     * Each time an item in the RecyclerView is clicked this method from the implementation of the transitListerner
     * in this activity is executed, this is possible because this class is passed as a parameter in the creation
     * of the RecyclerView's Adapter, see the adapter class to understand better what is happening here
     * @param pictureFolderPath a String corresponding to a folder path on the device external storage
     */
    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
    }

    @Override
    public void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {
        Intent move = new Intent(getContext(), BookeoMediaDisplay.class);
        move.putExtra("folderUuid",albumUuid);
        move.putExtra("folderName",AlbumName);
        startActivity(move);
    }

    @Override
    public void onLongPress(MediaAdapterHolder view, DeviceMediaItem item, int position) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position) {

    }
}
