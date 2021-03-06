package ie.bookeo.view.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ie.bookeo.R;
import ie.bookeo.view.LandingActivity;

/**
 * References
 *
 *  - Login & Register Android App Using Firebase Tutorial Series
 *  - URL - https://www.youtube.com/watch?v=tbh9YaWPKKs&list=PLlGT4GXi8_8eVRzsP295cTiz7SbZBn58c
 *  - Creator - SmallAcademy
 *
 *  - Modern Login and Sign up Animation using Fragments and Viewpager
 *  - URL - https://www.youtube.com/watch?v=ayKMfVt2Sg4
 *  - Creator - Coding With Tea
 *
 */
public class LoginFragment extends Fragment implements View.OnClickListener{

    EditText etEmail, etPassword;
    Button btnLogin;
    float a = 0;
    String email, password;
    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = root.findViewById(R.id.etEmail);
        etPassword = root.findViewById(R.id.etPassword);
        btnLogin = root.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        etEmail.setTranslationY(800);
        etPassword.setTranslationY(800);
        btnLogin.setTranslationY(800);
        etEmail.setAlpha(a);
        etPassword.setAlpha(a);
        btnLogin.setAlpha(a);
        etEmail.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        etPassword.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        btnLogin.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();

        firebaseAuth = FirebaseAuth.getInstance();
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnLogin) {
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            if(TextUtils.isEmpty(email)){
                etEmail.setError("Please enter email address");
                return;
            }
            if(TextUtils.isEmpty(password)){
                etPassword.setError("Please enter password");
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), LandingActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getContext(), "Login Unsuccessful! Try Again", Toast.LENGTH_LONG).show();
                        etEmail.setText("");
                        etPassword.setText("");
                        etEmail.findFocus();
                    }
                }
            });

        }
    }
}