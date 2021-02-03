package ie.bookeo.view.mediaExplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import ie.bookeo.adapter.bookeo.BookeoFolderAdapter;
import ie.bookeo.dao.bookeo.BookeoAlbumDao;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.DeviceMediaDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.ShowGallery;
import ie.bookeo.R;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.mediaExplorer.MediaAdapter;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * References
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  - URL - https://medium.com/better-programming/gmail-like-list-67bc51adc68a
 *  - Github - https://github.com/Mustufa786/MultiSelectionList
 *  - Creator - Mustufa Ansari
 *  - Modified by Cian O Sullivan
 *
 *  - To program toolbar back button
 *  - URL https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
 *
 *  - To retireve items from firestore
 *  - URL - https://www.youtube.com/watch?v=Bh0h_ZhX-Qg
 *  - Creator - Coding in Flow
 *
 *  - To add media to firebase storage
 *  - URL - https://www.youtube.com/watch?v=lPfQN-Sfnjw&t=1013s
 *  - Creator - Coding in Flow
 *
 *  - To update document in firestore
 *  - URL - https://www.youtube.com/watch?v=TBr_5QH1EvQ
 *  - Creator - Coding in Flow
 *
 * This Activity loads all images to images associated with a particular folder into a recyclerview with grid manager
 */

public class MediaDisplayActivity extends AppCompatActivity implements MediaDisplayItemClickListener, MyCreateListener, AlbumUploadListener {

    RecyclerView rvImage;
    ArrayList<DeviceMediaItem> arAllMedia;
    ProgressBar pbLoader;
    String folderPath;
    Toolbar tvFolderName;
    MediaAdapter adapter;
    Toolbar toolbar;
    TextView tvUploading;

    ActionMode actionMode;
    ActionCallback actionCallback;

    private DeviceMediaDao deviceMediaDao;

    //DB
    private BookeoAlbumDao bookeoAlbumDao;
    private BookeoMediaItemDao bookeoMediaItemDao;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookeoFolderAdapter bookeoFolderAdapter;
    private RecyclerView rvAlbums;
    private List<BookeoAlbum> albums;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_image_display);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //tvFolderName = findViewById(R.id.toolbar);
        String name = getIntent().getStringExtra("folderName");
        toolbar.setTitle(name);
        folderPath = getIntent().getStringExtra("folderPath");

        arAllMedia = new ArrayList<>();
        rvImage = findViewById(R.id.rvFolders);
        rvImage.addItemDecoration(new MarginItemDecoration(this));
        rvImage.hasFixedSize();
        pbLoader = findViewById(R.id.loader);
        tvUploading = findViewById(R.id.tvUploading);

        actionCallback = new ActionCallback();
        deviceMediaDao = new DeviceMediaDao();

        if (arAllMedia.isEmpty() && !getIntent().getStringExtra("folderName").contains("all")) {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = deviceMediaDao.getAllImagesByFolder(folderPath, this);;
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);
        }else {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = deviceMediaDao.getAllImages(this);
            adapter = new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);
        }

        //DB - albums for user to upload to when long press
        albums = new ArrayList<>();
        albums = getUserAlbums();

        rvAlbums = findViewById(R.id.rvBookeoAlbumIcons);
        bookeoFolderAdapter = new BookeoFolderAdapter(albums, getApplicationContext(), this);
        rvAlbums.addItemDecoration(new MarginItemDecoration(this));
        rvAlbums.hasFixedSize();
        rvAlbums.setAdapter(bookeoFolderAdapter);
    }

    public ArrayList<BookeoAlbum> getUserAlbums() {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("fk_user", userId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    String data = "";

                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);

                                BookeoAlbum arAlbum = new BookeoAlbum(album.getUuid(), album.getName(), album.getCreateDate());

                                dbAlbums.add(arAlbum);

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arAlbum.getName());
                            }
                        }
                        bookeoFolderAdapter.notifyDataSetChanged();
                    }
                });
        Log.d("SIZE", "getAlbums: added" + dbAlbums.size());
        return dbAlbums;
    }

    /**
     * @param holder   The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param paths     An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> paths, Context contx) {
        if (adapter.selectedItemCount() > 0) {
            toggleActionBar(position);
            adapter.toggleIcon(holder, position);
        } else {
            ShowGallery.show(this, paths, position);
        }
    }

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuid, String albumUuid) {}

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    @Override
    public void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {}

    @Override
    public void onLongPress(MediaAdapterHolder view, DeviceMediaItem item, int position) {
        //Toast.makeText(this, "long click " + position, Toast.LENGTH_SHORT).show();
        toolbar.setVisibility(View.GONE);
        toggleActionBar(position);
        adapter.toggleIcon(view, position);
        rvAlbums.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position) {

    }

    /*
       toggling action bar that will change the color and option
     */
    private void toggleActionBar(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionCallback);
        }
        toggleSelection(position);
    }

    /*
       toggle selection of items and show the count of selected items on the action bar
     */
    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.selectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (folderPath != null) {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = deviceMediaDao.getAllImagesByFolder(folderPath, this);
            rvImage.setAdapter(new MediaAdapter(arAllMedia, MediaDisplayActivity.this, this));
            pbLoader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreated(BookeoAlbum bookeoAlbum) {
        albums.add(bookeoAlbum);
        bookeoFolderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUploadAlbumClicked(final String albumUuid) {
        List<DeviceMediaItem> uploadItems;
        uploadItems = adapter.getUploadItems();

        for (final DeviceMediaItem uploadItem : uploadItems) {
            final String uuid = UUID.randomUUID().toString();

            BookeoMediaItem upload = new BookeoMediaItem();
            upload.setUuid(uuid);
            upload.setName(uploadItem.getName());
            upload.setDate(uploadItem.getDate());
            upload.setAlbumUuid(albumUuid);

            //upload item
            bookeoMediaItemDao = new BookeoMediaItemDao();
            bookeoMediaItemDao.addMediaItem(upload, uploadItem.getPath());

            //Toast.makeText(MediaDisplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(MediaDisplayActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
        MediaDisplayActivity.this.recreate();
    }

    class ActionCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            toggleStatusBarColor(MediaDisplayActivity.this, R.color.blue_grey_700);

            mode.getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.add) {
                AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Bookeo Album", MediaDisplayActivity.this);
                addAlbumFragment.show(getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
                adapter.notifyDataSetChanged();
                //mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
            toggleStatusBarColor(MediaDisplayActivity.this, R.color.colorPrimary);
            toolbar.setVisibility(View.VISIBLE);
            rvAlbums.setVisibility(View.GONE);
            MediaDisplayActivity.this.recreate();
          //  adapter.notifyDataSetChanged();
        }
    }
    /*
      this will toggle or action bar color
     */
    public static void toggleStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimary));
    }

    @Override
    //https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(MediaDisplayActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

