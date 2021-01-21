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
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ie.bookeo.R;
import ie.bookeo.activity.FolderViewActivity;

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
                            Toast.makeText(getContext(), "User Created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), FolderViewActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getContext(), "Error Occurred: " + task.getException(), Toast.LENGTH_SHORT).show();
                            Log.d("TASK", "onComplete: " + task.getException());
                        }
                    }
                });
        }
    }
}
