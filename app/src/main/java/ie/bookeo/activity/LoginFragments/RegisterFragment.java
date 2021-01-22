package ie.bookeo.activity.LoginFragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.activity.MainActivity;
import ie.bookeo.model.User;

/*
 *
 *https://www.youtube.com/watch?v=ayKMfVt2Sg4
 *
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    String name, email, password, confirmPass;
    float a = 0;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_register, container, false);

        etName = root.findViewById(R.id.etName);
        etEmail = root.findViewById(R.id.etEmail);
        etPassword = root.findViewById(R.id.etPassword);
        etConfirmPassword = root.findViewById(R.id.etConfirmPassword);
        btnRegister = root.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);
        //animation
        etName.setTranslationY(800);
        etEmail.setTranslationY(800);
        etPassword.setTranslationY(800);
        etConfirmPassword.setTranslationY(800);
        btnRegister.setTranslationY(800);
        etName.setAlpha(a);
        etEmail.setAlpha(a);
        etPassword.setAlpha(a);
        etConfirmPassword.setAlpha(a);
        btnRegister.setAlpha(a);
        etName.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        etEmail.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        etPassword.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        etConfirmPassword.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        btnRegister.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();

        firebaseAuth = FirebaseAuth.getInstance();

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRegister){
            email = etEmail.getText().toString().trim();
            name = etName.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            confirmPass = etConfirmPassword.getText().toString().trim();

            if(TextUtils.isEmpty(email)){
                etEmail.setError("Please enter email address");
                return;
            }
            if(TextUtils.isEmpty(name)){
                etEmail.setError("Please enter email address");
                return;
            }
            if(TextUtils.isEmpty(password)){
                etEmail.setError("Please enter email address");
                return;
            }
            if(password.length() < 6){
                etEmail.setError("Password must be at least 6 characters");
                return;
            }
            if(TextUtils.isEmpty(confirmPass)){
                etEmail.setError("Please enter email address");
                return;
            }
            if(!password.equals(confirmPass)){
                etPassword.setError("Passwords do not match");
                etPassword.setText("");
                etConfirmPassword.setText("");
                etPassword.findFocus();
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            addUserToDb(email, name);
                            Toast.makeText(getContext(), "User Created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getContext(), "Error Occurred: " + task.getException(), Toast.LENGTH_SHORT).show();
                            Log.d("TASK", "onComplete: " + task.getException());
                        }
                    }
                });
        }
    }
    private void addUserToDb(String email, String name) {
        //Read more: https://www.java67.com/2013/01/how-to-format-date-in-java-simpledateformat-example.html#ixzz6fppjpeYL
        final String userId = UUID.randomUUID().toString();
        final User user = new User(userId, email, name);

        db.collection("user").document(userId).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "User Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error adding User!", Toast.LENGTH_SHORT).show();
                        Log.d("ERROR", e.toString());
                    }
                });
    }
}
