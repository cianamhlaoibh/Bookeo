package ie.bookeo.view.mediaExplorer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import ie.bookeo.R;
import ie.bookeo.adapter.mediaExplorer.ViewPagerAdapter;
import ie.bookeo.dao.drive.DriveServiceHelper;
import ie.bookeo.utils.DriveLoginListener;
import ie.bookeo.view.drive.GoogleDriveFragment;


/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  -URL - https://www.youtube.com/watch?v=TwHmrZxiPA8
 *
 * This Activity loads all folders containing images int a RecyclerView
 */

public class MainActivity extends AppCompatActivity implements DriveLoginListener {

    public static final int PERMISSION_REQUEST = 111;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    MenuInflater inflater;
    Menu optionMenu;
    GoogleDriveFragment googleDriveFragment;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    /**
     * Request the user for permission to access media files and read images on the device
     * this will be useful as from api 21 and above, if this check is not done the Activity will crash
     *
     * Setting up the RecyclerView and getting all folders that contain pictures from the device
     * the getPicturePaths() returns an ArrayList of imageFolder objects that is then used to
     * create a RecyclerView Adapter that is set to the RecyclerView
     *
     * @param savedInstanceState saving the activity state
     */
    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //       WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check permissions
        if(checkWriteExternalPermission())
            _init();
        else
            grantPermission();
    }

    public void _init(){
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        setTabs();
    }

    private boolean checkWriteExternalPermission()
    {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void grantPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _init();
                }
            }
        }
    }


    public void setTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(0, new FolderViewFragment(), "DEVICE");
        viewPagerAdapter.addFragment(1, new DeviceImagesFragement(), "IMAGES");
        viewPagerAdapter.addFragment(2, new GoogleDriveFragment(this), "DRIVE");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_photo_library);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_google_drive);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        optionMenu = menu;
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(DriveServiceHelper.mDriveService != null) {
            menu.findItem(R.id.DriveLogout).setVisible(true);
        }else{
            menu.findItem(R.id.DriveLogout).setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Licenses:
                //https://developers.google.com/android/guides/opensource
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;
            case R.id.DriveLogout:
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
                    item.setVisible(false);
                    googleDriveFragment.Logout();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void DriveLogin(GoogleDriveFragment fragment) {
        optionMenu.findItem(R.id.DriveLogout).setVisible(true);
        googleDriveFragment = fragment;
    }
}