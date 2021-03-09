package ie.bookeo.dao.bookeo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebaseMediaItemsResultListener;
import ie.bookeo.utils.FirebaseResultListener;

public class BookeoMediaItemDao {
    FirebaseFirestore db;
    StorageReference storageReference;
    FirebaseMediaItemsResultListener listener;


    public BookeoMediaItemDao() {
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("media_items");
    }

    public BookeoMediaItemDao(FirebaseMediaItemsResultListener listener) {
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("media_items");
        this.listener = listener;
    }



    // //https://www.youtube.com/watch?v=Bh0h_ZhX-Qghttps://www.youtube.com/watch?v=Bh0h_ZhX-Qg - add and retireve documents
    public void addMediaItem(BookeoMediaItem item, String filepath) {
        db.collection("albums").document(item.getAlbumUuid()).collection("media_items").document(item.getUuid())
            .set(item)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Log.d("SUCCESS", "DocumentSnapshot added with ID: " + uuid);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   // Log.w("ERROR", "Error adding document", e);
                }
            });
        //https://www.youtube.com/watch?v=lPfQN-Sfnjw&t=1013s - Firebase Storage - Upload Images
        final StorageReference fileRef = storageReference.child(item.getUuid());
        Uri uri = Uri.fromFile(new File(filepath));
        fileRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //  https://stackoverflow.com/questions/57183427/download-url-is-getting-as-com-google-android-gms-tasks-zzu441922b-while-using/57183557
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String url = uri.toString();
                                        //upload.setUrl(url);
                                        //https://www.youtube.com/watch?v=TBr_5QH1EvQ - update firstore
                                        db.collection("albums").document(item.getAlbumUuid()).collection("media_items").document(item.getUuid()).update("url", url);
                                        db.collection("albums").document(item.getAlbumUuid()).update("firstItem", url);
                                        Log.d("URL", "onSuccess: " + uri.toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void deleteMediaItem(String albumUuid, String id, Context contx) {
        db.collection("albums").document(albumUuid).collection("media_items").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(contx, "Media Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(contx, "Error occurred trying to delete image", Toast.LENGTH_SHORT).show();
                    }
                });
        final StorageReference fileRef = storageReference.child(id);
        fileRef.delete();
    }

    public ArrayList<BookeoMediaItem> getMediaItems(String albumUuid) {
        final ArrayList<BookeoMediaItem> mediaItems = new ArrayList<>();
        db.collection("albums").document(albumUuid).collection("media_items").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoMediaItem item = new BookeoMediaItem();
                                item = documentSnapshot.toObject(BookeoMediaItem.class);
                                BookeoMediaItem arItem = new BookeoMediaItem(item.getUuid(), item.getUrl(), item.getName(), item.getDate(), item.getAlbumUuid());
                                mediaItems.add(arItem);
                            }
                            listener.OnComplete(mediaItems);
                        }
                    }
                });
        //Log.d("SIZE", "onSuccess create: " + mediaItems.size());
        return mediaItems;
    }

    public ArrayList<BookeoMediaItem> getItem(String albumUuid, String uuid, Context contx) {
        final ArrayList<BookeoMediaItem> mediaItems = new ArrayList<>();
        db.collection("albums").document(albumUuid).collection("media_items").document(uuid).get()
               .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                   @Override
                   public void onSuccess(DocumentSnapshot documentSnapshot) {
                       mediaItems.add(documentSnapshot.toObject(BookeoMediaItem.class));
                       listener.OnComplete(mediaItems);
                   }
               })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("db_err", "onFailure: " + e.getMessage());
                    }
                });
        //Log.d("SIZE", "onSuccess create: " + mediaItems.size());
        return mediaItems;
    }

    public void updatePosition(String albumUuid, String uuid, int position ) {
        db.collection("albums").document(albumUuid).collection("media_items").document(uuid).update("position", position);
    }

    public ArrayList<BookeoMediaItem> getPageItems(String albumUuid,ArrayList<String> mediaUuid) {
        ArrayList<BookeoMediaItem> items = new ArrayList<>();
        for (String uuid: mediaUuid) {
            db.collection("albums").document(albumUuid).collection("media_items").document(uuid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            items.add(documentSnapshot.toObject(BookeoMediaItem.class));
                        }
                    });
        }
        return items;
    }
}
