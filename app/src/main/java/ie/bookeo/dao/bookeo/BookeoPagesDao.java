package ie.bookeo.dao.bookeo;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebasePageResultListener;
import ie.bookeo.utils.FirebaseResultListener;

public class BookeoPagesDao {
    FirebaseFirestore db;
    FirebasePageResultListener listener;

    public BookeoPagesDao() {
        db = FirebaseFirestore.getInstance();
    }

    public BookeoPagesDao(FirebasePageResultListener listener) {
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

    public void getPages(String albumUuid){
        db.collection("albums").document(albumUuid).collection("pages").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.getResult().isEmpty()) {
                            List<DocumentSnapshot> list = task.getResult().getDocuments();
                            ArrayList<BookeoPage> pages = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : list) {
                                pages.add(documentSnapshot.toObject(BookeoPage.class));
                            }
                            listener.onComplete(pages);
                        }
                    }
                });
    }

    public void updateCaption(String albumUuid, String uuid, String caption, MyCaptionStyle style) {
        db.collection("albums").document(albumUuid).collection("pages").document(uuid).update("caption", caption);
        db.collection("albums").document(albumUuid).collection("pages").document(uuid).update("style", style);
    }

    public void updatePosition(String albumUuid, String uuid, int position ) {
        db.collection("albums").document(albumUuid).collection("pages").document(uuid).update("position", position);
    }

    public void updateEnlargement(String albumUuid, String uuid, boolean b) {
        db.collection("albums").document(albumUuid).collection("pages").document(uuid).update("enlarged", b);
    }

    public void deletePage(String albumUuid, String uuid) {
        db.collection("albums").document(albumUuid).collection("pages").document(uuid).delete();
    }
}
