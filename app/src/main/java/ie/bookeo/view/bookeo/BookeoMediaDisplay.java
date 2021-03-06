package ie.bookeo.view.bookeo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoMediaItemAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.adapter.bookeo.BookeoSubFolderAdapter;
import ie.bookeo.adapter.drive.GoogleDriveFolderAdapter;
import ie.bookeo.dao.bookeo.BookeoAlbumDao;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.FirebaseResultListener;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.utils.ShowGallery;
import ie.bookeo.utils.SubFolderResultListener;

/**
 * Reference
 *  - URL - https://github.com/CodeBoy722/Android-Simple-Image-Gallery
 *  - Creator - CodeBoy 722
 *  - Modified by Cian O Sullivan
 *
 *  - URL - https://medium.com/better-programming/gmail-like-list-67bc51adc68a
 *  - Github - https://github.com/Mustufa786/MultiSelectionList
 *  - Creator - Mustufa Ansari
 *  - Modified by Cian O Sullivan
 *
 *  - To display license activity
 *  - URL - https://developers.google.com/android/guides/opensource
 *
 *   - To program toolbar back button
 *   -URL https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
 *
 *   - To retireve items from firestore
 *   - URL - https://www.youtube.com/watch?v=Bh0h_ZhX-Qg
 *
 * This Activity loads all images to images associated with a particular folder into a recyclerview with grid manager from cloud storage
 */

public class BookeoMediaDisplay extends AppCompatActivity implements MediaDisplayItemClickListener, View.OnClickListener, FirebaseResultListener, SubFolderResultListener {

    RecyclerView rvMediaItems, rvFolders;
    ProgressBar pbLoader;
    BookeoMediaItemAdapter adapter;
    TextView tvNoMedia;
    FloatingActionButton fabGenerate, fabUpdate;
    Toolbar toolbar;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<BookeoMediaItem> items;
    ArrayList<BookeoAlbum> folders;
    BookeoSubFolderAdapter subFolderAdapter;
    BookeoAlbumDao dao;
    BookeoPagesDao pagesDao;
    BookeoAlbum album;
    String name, uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_bookeo_media_display);

        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = getIntent().getStringExtra("folderName");
        uuid = getIntent().getStringExtra("folderUuid");

        dao = new BookeoAlbumDao(this, this);
        rvFolders = findViewById(R.id.rvFolders);
        rvFolders.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


        rvMediaItems = findViewById(R.id.rvMediaItems);
        rvMediaItems.addItemDecoration(new MarginItemDecoration(this));
        rvMediaItems.hasFixedSize();

        tvNoMedia = findViewById(R.id.tvNoMedia);
        //DB
        items = new ArrayList<>();
        pbLoader = findViewById(R.id.loader);
        pbLoader.setVisibility(View.GONE);

        fabGenerate = findViewById(R.id.fabGenerate);
        fabGenerate.setOnClickListener(this);
        fabUpdate = findViewById(R.id.fabUpdate);
        fabUpdate.setOnClickListener(this);

    }

    private void checkIsGenerated() {
        if (album.getGenerated() == null) {
            fabGenerate.setVisibility(View.VISIBLE);
            fabUpdate.setVisibility(View.GONE);
        }else{
            fabUpdate.setVisibility(View.VISIBLE);
            fabGenerate.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        items = getDbMedia(uuid);
        adapter = new BookeoMediaItemAdapter(items, BookeoMediaDisplay.this, this);
        rvMediaItems.setAdapter(adapter);
        dao.getAlbum(uuid);
        dao.getSubFolders(uuid);
        pagesDao = new BookeoPagesDao();
    }

    public void viewVisibility(ArrayList<BookeoMediaItem> items) {
        if(items.isEmpty())
            tvNoMedia.setVisibility(View.VISIBLE);
        else
            tvNoMedia.setVisibility(View.GONE);
    }

    public void getSubFolders(){

    }

    //https://www.youtube.com/watch?v=Bh0h_ZhX-Qg
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
                return true;
            case R.id.Licenses:
                //https://developers.google.com/android/guides/opensource
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            //https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
            case android.R.id.home:
                if(album.getParent().equals("root")) {
                    Intent intent = new Intent(BookeoMediaDisplay.this, BookeoMain.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(getApplicationContext(), BookeoMediaDisplay.class);
                    intent.putExtra("folderUuid",album.getUuid());
                    intent.putExtra("folderName",album.getName());
                    finish();
                    startActivity(intent);
                }
                return true;
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
                        Intent intent = new Intent(BookeoMediaDisplay.this, BookeoMain.class);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabGenerate:
                generateAlbum();
                Intent i = new Intent(this, BookeoBook.class);
                i.putExtra("albumUuid", uuid);
                startActivity(i);
                break;
            case R.id.fabUpdate:
                Intent intent = new Intent(this, BookeoBook.class);
                intent.putExtra("albumUuid", uuid);
                startActivity(intent);;
        }
    }

    private void generateAlbum() {
        if(album.getGenerated() == null || album.getGenerated() == false) {
            BookeoAlbumDao bookeoAlbumDao = new BookeoAlbumDao();
            bookeoAlbumDao.generateBook(uuid);
            for (final BookeoMediaItem item : items) {
                String pageUuid = UUID.randomUUID().toString();

                BookeoPage page = new BookeoPage();
                page.setPageUuid(pageUuid);
                page.setPageNumber(items.indexOf(item));
                page.setAlbumUuid(item.getAlbumUuid());
                page.setItem(item);

                //upload item
                pagesDao.addPage(page, this.uuid);
            }
        }
    }


    @Override
    public void onComplete(BookeoAlbum album) {
        this.album = album;
        checkIsGenerated();
        toolbar.setTitle(album.getName());
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
    public void onDrivePicClicked(MediaAdapterHolder holder, String names, String urls, String ids, int position) {

    }

    @Override
    public void onBPicClicked(String albumUuid, String AlbumName) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, DeviceMediaItem item, int position) {

    }

    @Override
    public void onLongPress(MediaAdapterHolder holder, GoogleDriveMediaItem item, int position) {

    }

    @Override
    public void onSubFolderResult(ArrayList<BookeoAlbum> albums) {
        folders = albums;
        Log.d("onSubFolderResult", "onSubFolderResult: " + albums.size());
        Log.d("onSubFolderResult", "onSubFolderResult: " + folders.size());
        subFolderAdapter = new BookeoSubFolderAdapter(folders, getApplicationContext());
        rvFolders.setAdapter(subFolderAdapter);
    }

}
