package ie.bookeo.dao.bookeo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.model.bookeo.BookeoAlbum;

public class BookeoAlbumDao {
    FirebaseFirestore db;

    public BookeoAlbumDao() {
        db = FirebaseFirestore.getInstance();
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
}
