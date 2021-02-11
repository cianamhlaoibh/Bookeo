package ie.bookeo.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ie.bookeo.R;
import ie.bookeo.view.bookeo.BookeoAlbumsFragment;
import ie.bookeo.view.bookeo.BookeoMain;
import ie.bookeo.view.login.LoginActivity;
import ie.bookeo.view.mediaExplorer.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;


public class LandingActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnContinue, btnStart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        btnStart = findViewById(R.id.btnStart);
        btnContinue = findViewById(R.id.btnContinue);
        btnStart.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                Intent startIntent = new Intent(this, MainActivity.class);
                startActivity(startIntent);
                break;
            case R.id.btnContinue:
                Intent continueIntent = new Intent(this, BookeoMain.class);
                startActivity(continueIntent);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Licenses:
                //https://developers.google.com/android/guides/opensource
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;
            case R.id.Logout:
                //log out for user who signed in with google
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
                if(signInAccount != null) {
                    GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Logout Failure", Toast.LENGTH_LONG).show();
                                }
                            });
                    //also sign out of firebase auth
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //signout for email/password
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
