package ie.bookeo.dao.bookeo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import ie.bookeo.model.bookeo.BookeoMediaItem;

public class BookeoMediaItemDao {
    FirebaseFirestore db;
    StorageReference storageReference;


    public BookeoMediaItemDao() {
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("media_items");
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
                        Toast.makeText(contx, "Image Deleted", Toast.LENGTH_SHORT).show();
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

}
