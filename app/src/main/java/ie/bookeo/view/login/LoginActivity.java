package ie.bookeo.view.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import ie.bookeo.R;
import ie.bookeo.adapter.login.LoginTabAdapter;
import ie.bookeo.dao.UserDao;
import ie.bookeo.model.User;
import ie.bookeo.view.LandingActivity;

/*
*
*https://www.youtube.com/watch?v=ayKMfVt2Sg4
*
*/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int GOOGLE_SIGNIN_CODE = 1000;

    TabLayout tabLayout;
    ViewPager viewPager;
    FloatingActionButton fabGoogle, fabFb, fabMicrosoft;
    float a = 0;
    FirebaseAuth firebaseAuth;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tabLayout = findViewById(R.id.tlLogin);
        viewPager = findViewById(R.id.vpLogin);
        fabGoogle = findViewById(R.id.fabGoogle);
        fabGoogle.setOnClickListener(this);
        fabFb = findViewById(R.id.fabFacebook);
        fabFb.setOnClickListener(this);
        fabMicrosoft = findViewById(R.id.fabMicrosoft);
        fabMicrosoft.setOnClickListener(this);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final LoginTabAdapter loginAdapter = new LoginTabAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager.setAdapter(loginAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        fabGoogle.setTranslationY(300);
        fabFb.setTranslationY(300);
        fabMicrosoft.setTranslationY(300);
        tabLayout.setTranslationY(300);
        fabGoogle.setAlpha(a);
        fabFb.setAlpha(a);
        fabMicrosoft.setAlpha(a);
        tabLayout.setAlpha(a);
        fabGoogle.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        fabMicrosoft.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        fabFb.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();

        //set Google sign in
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("77984560108-douaep2tfn3ales9u7i1jrjvlutd035j.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //if user already logged in with email/pass or google
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null || firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
            startActivity(intent);
            finish();
        }
    }
    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.fabGoogle:
                Intent signIn = googleSignInClient.getSignInIntent();
                startActivityForResult(signIn, GOOGLE_SIGNIN_CODE);
                break;
            case R.id.fabFacebook:
                Toast.makeText(this, "Facebook Login Unavailable", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fabMicrosoft:
                Toast.makeText(this, "Microsoft Login Unavailable", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGNIN_CODE) {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                final GoogleSignInAccount signInAccount = signInAccountTask.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(task -> {
                            //Add to firebase
                            registerUser(signInAccount);
                            Intent intent = new Intent(getApplicationContext(), LandingActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(getApplicationContext(), "Signed in with Google", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerUser(GoogleSignInAccount signInAccount) {
        String name = signInAccount.getDisplayName();
        String email = signInAccount.getEmail();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDao = new UserDao();
        userDao.addUser(name, email, userId, this);
    }
}