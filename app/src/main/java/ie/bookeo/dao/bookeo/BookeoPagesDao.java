package ie.bookeo.dao.bookeo;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.utils.FirebaseResultListener;

public class BookeoPagesDao {
    FirebaseFirestore db;
    FirebaseResultListener listener;

    public BookeoPagesDao() {
        db = FirebaseFirestore.getInstance();
    }

    public BookeoPagesDao(FirebaseResultListener listener) {
        db = FirebaseFirestore.getInstance();
        this.listener = listener;
    }

    public void addPage(BookeoPage page, String albumUuid) {
        db.collection("albums").document(albumUuid).collection("pages").document(page.getPageUuid()).set(page)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    public void getPage(String albumUuid, String pageUuid) {
        final BookeoPage page = new BookeoPage();
        db.collection("albums").document(albumUuid).collection("pages").document(pageUuid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        listener.onComplete(task.getResult().toObject(BookeoPage.class));
                    }
                });
    }
}
