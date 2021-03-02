package ie.bookeo.view.mediaExplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoFolderAdapter;
import ie.bookeo.adapter.mediaExplorer.MediaAdapter;
import ie.bookeo.adapter.MediaAdapterHolder;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.DeviceMediaDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.drive.GoogleDriveMediaItem;
import ie.bookeo.model.gallery_model.DeviceMediaItem;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MediaDisplayItemClickListener;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.ShowGallery;

public class DeviceImagesFragement extends Fragment implements MediaDisplayItemClickListener,  AlbumUploadListener {
    RecyclerView rvImage;
    ArrayList<DeviceMediaItem> arAllMedia;
    ProgressBar pbLoader;
    String folderPath;
    Toolbar tvFolderName;
    MediaAdapter adapter;
    Toolbar toolbar;
    TabLayout tabLayout;
    //AppBarLayout appBarLayout;
    TextView tvUploading;

    ActionMode actionMode;
    ActionCallback actionCallback;

    DeviceMediaDao deviceMediaDao;

    //DB
    private BookeoMediaItemDao bookeoMediaItemDao;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookeoFolderAdapter bookeoFolderAdapter;
    private RecyclerView rvAlbums;
    private List<BookeoAlbum> albums;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_images, container, false);
        arAllMedia = new ArrayList<>();
        rvImage = root.findViewById(R.id.rvFolders);
        rvImage.addItemDecoration(new MarginItemDecoration(getContext()));
        rvImage.hasFixedSize();
        pbLoader = root.findViewById(R.id.loader);
        tvUploading = root.findViewById(R.id.tvUploading);

        //appBarLayout = getActivity().findViewById(R.id.toolbarLayout);
        toolbar = getActivity().findViewById(R.id.toolbar);
        tabLayout = getActivity().findViewById(R.id.tabs);

        actionCallback = new ActionCallback();
        deviceMediaDao = new DeviceMediaDao();


            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = deviceMediaDao.getAllImages(getContext());
            adapter = new MediaAdapter(arAllMedia, getContext(), this);
            rvImage.setAdapter(adapter);
            pbLoader.setVisibility(View.GONE);

        //DB - albums for user to upload to when long press
        albums = new ArrayList<>();
        albums = getAlbums();

        rvAlbums = root.findViewById(R.id.rvBookeoAlbumIcons);
        bookeoFolderAdapter = new BookeoFolderAdapter(albums, getContext(), this);
        rvAlbums.addItemDecoration(new MarginItemDecoration(getContext()));
        rvAlbums.hasFixedSize();
        rvAlbums.setAdapter(bookeoFolderAdapter);
        return root;
    }
    public ArrayList<BookeoAlbum> getAlbums() {
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
            ShowGallery.show(getContext(), paths, position);
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
        tabLayout.setVisibility(View.GONE);
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
    public void toggleActionBar(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionCallback);
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
    public void onResume() {
        super.onResume();
        if (folderPath != null) {
            pbLoader.setVisibility(View.VISIBLE);
            arAllMedia = deviceMediaDao.getAllImages(getContext());
            rvImage.setAdapter(new MediaAdapter(arAllMedia, getContext(), this));
            pbLoader.setVisibility(View.GONE);
        }
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

        Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
        //https://stackoverflow.com/questions/39296873/how-can-i-recreate-a-fragment
        getFragmentManager()
                .beginTransaction()
                .detach(DeviceImagesFragement.this)
                .attach(DeviceImagesFragement.this)
                .commit();
        actionMode.finish();
    }

    class ActionCallback implements ActionMode.Callback, MyCreateListener{
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            toggleStatusBarColor(getActivity(), R.color.blue_grey_700);

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
                AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Bookeo Album", this, "root");
                addAlbumFragment.show(getActivity().getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
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
            toggleStatusBarColor(getActivity(), R.color.colorPrimary);
            toolbar.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            rvAlbums.setVisibility(View.GONE);
            getFragmentManager()
                    .beginTransaction()
                    .detach(DeviceImagesFragement.this)
                    .attach(DeviceImagesFragement.this)
                    .commit();
            //  adapter.notifyDataSetChanged();
        }
        @Override
        public void onCreated(BookeoAlbum bookeoAlbum) {
            albums.add(bookeoAlbum);
            bookeoFolderAdapter.notifyDataSetChanged();
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
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
