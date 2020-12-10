package ie.bookeo.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.BookeoFolderAdapter;
import ie.bookeo.adapter.BookeoMediaItemAdapter;
import ie.bookeo.adapter.MediaAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.model.BookeoAlbum;
import ie.bookeo.model.BookeoMediaItem;
import ie.bookeo.model.gallery_model.MediaItem;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.utils.ShowGallery;

public class BookeoMediaDisplay extends AppCompatActivity implements MediaDisplayItemClickListener {

    RecyclerView rvMediaItems;
    ArrayList<BookeoMediaItem> mediaItems;
    ProgressBar pbLoader;
    Toolbar tvFolderName;
    ImageButton ibAlbum;
    BookeoMediaItemAdapter adapter;
    TextView tvNoMedia;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<BookeoMediaItem> items;
    String name, uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_bookeo_media_display);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = getIntent().getStringExtra("folderName");
        uuid = getIntent().getStringExtra("folderUuid");
        toolbar.setTitle(name);

        mediaItems = new ArrayList<>();
        rvMediaItems = findViewById(R.id.rvMediaItems);
        rvMediaItems.addItemDecoration(new MarginItemDecoration(this));
        rvMediaItems.hasFixedSize();

        tvNoMedia = findViewById(R.id.tvNoMedia);
        //DB
        items = new ArrayList<>();
        items = getDbMedia(uuid);

        adapter = new BookeoMediaItemAdapter(items, BookeoMediaDisplay.this, this);
        rvMediaItems.setAdapter(adapter);
        pbLoader = findViewById(R.id.loader);
        pbLoader.setVisibility(View.GONE);

       // viewVisibility();
    }

    public void viewVisibility(ArrayList<BookeoMediaItem> items) {
        if(items.isEmpty())
            tvNoMedia.setVisibility(View.VISIBLE);
        else
            tvNoMedia.setVisibility(View.GONE);
    }

    public ArrayList<BookeoMediaItem> getDbMedia(String albumUuid) {
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

                                //data = albums.get(0).getUuid() + " " + albums.get(0).getName() + " " + album.getCreateDate();
                                Log.d("OUTPUT", "onSuccess create: " + arItem.getUrl());
                                Log.d("OUTPUT--", "onSuccess create: " + mediaItems.get(0).getUrl());
                                Log.d("SIZE--", "onSuccess create: " + mediaItems.size());
                            }
                            }
                        viewVisibility(mediaItems);
                        adapter.notifyDataSetChanged();
                        }
                });
        Log.d("SIZE", "onSuccess create: " + mediaItems.size());
        return mediaItems;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_album_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Delete:
                deleteAlbum(uuid);
                return super.onOptionsItemSelected(item);
            case R.id.Licenses:
                //https://developers.google.com/android/guides/opensource
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteAlbum(String uuid) {
        db.collection("albums").document(uuid).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Album " + name + " Deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BookeoMediaDisplay.this, FolderViewActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error Trying to Delete Album " + name , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> path, Context contx) {}

    @Override
    public void onBPicClicked(MediaAdapterHolder holder, int position, ArrayList<String> names, ArrayList<String> urls, ArrayList<String> uuids, String albumUuid) {
        ShowGallery.bShow(this, names, urls, position, uuids, albumUuid);
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, MediaItem item, int position) {

    }
}