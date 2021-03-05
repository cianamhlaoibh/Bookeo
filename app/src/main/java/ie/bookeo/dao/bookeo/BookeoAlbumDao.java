package ie.bookeo.dao.bookeo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.utils.FirebaseResultListener;
import ie.bookeo.utils.SubFolderResultListener;

public class BookeoAlbumDao {
    FirebaseFirestore db;
    FirebaseResultListener listener;
    SubFolderResultListener subFolderResultListener;

    public BookeoAlbumDao() {
        db = FirebaseFirestore.getInstance();
    }

    public BookeoAlbumDao(FirebaseResultListener listener, SubFolderResultListener subFolderResultListener) {
        db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.subFolderResultListener =subFolderResultListener;
    }

    public int[] addAlbum(BookeoAlbum album, Context contx) {
        final int[] result = new int[1];
        db.collection("albums").document(album.getUuid()).set(album)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       Toast.makeText(contx, album.getName() +  " added", Toast.LENGTH_SHORT).show();
                        result[0] = 1;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(contx, "Error adding album", Toast.LENGTH_SHORT).show();
                        result[0] = -1;
                    }
                });
        return result;
    }

    public void getAlbum(String uuid) {
        db.collection("albums").document(uuid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        listener.onComplete(task.getResult().toObject(BookeoAlbum.class));
                    }
                });
    }

    public void getSubFolders(String parentUuid){
        db.collection("albums").whereEqualTo("parent", parentUuid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);

                                final BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());
                                arAlbum.setFirstItem(album.getFirstItem());


                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                            subFolderResultListener.onSubFolderResult(dbAlbums);
                        }
                    }
                });
    }

    public ArrayList<BookeoAlbum> getUserAlbums(String userId) {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        db.collection("albums").whereEqualTo("fk_user", userId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                                 BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);
                                BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());
                                dbAlbums.add(arAlbum);
                            }
                        }
                    }
                });
        Log.d("SIZE", "getUserAlbums: " + dbAlbums.size());
        return dbAlbums;
    }

    public void generateBook(String uuid) {
        db.collection("albums").document(uuid).update("generated", true);
    }
}
