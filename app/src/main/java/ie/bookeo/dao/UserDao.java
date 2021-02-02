package ie.bookeo.dao;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import ie.bookeo.model.User;

public class UserDao {

    FirebaseFirestore db;

    public UserDao() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void addUser(String name, String email, String id, Context contx) {
        final User user = new User(id, email, name);
        db.collection("user").document(id).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(contx, "Welcome " + name, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(contx, "Error Registering your Account!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
