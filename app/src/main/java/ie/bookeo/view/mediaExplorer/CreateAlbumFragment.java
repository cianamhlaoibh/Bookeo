package ie.bookeo.view.mediaExplorer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoCreateFolderAdapter;
import ie.bookeo.adapter.mediaExplorer.MediaFolderAdapter;
import ie.bookeo.dao.bookeo.BookeoAlbumDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.gallery_model.AlbumFolder;
import ie.bookeo.utils.AddAlbumListener;
import ie.bookeo.utils.AlbumUploadListener;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.FirebaseResultListener;
import ie.bookeo.utils.ItemClickListener;
import ie.bookeo.utils.MarginItemDecoration;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.utils.SubFolderResultListener;
import ie.bookeo.view.bookeo.BookeoMain;
import ie.bookeo.view.bookeo.BookeoMediaDisplay;

public class CreateAlbumFragment extends DialogFragment implements View.OnClickListener, AddAlbumListener, MyCreateListener, AlbumUploadListener, SubFolderResultListener, ItemClickListener, FirebaseResultListener {

    private static AlbumUploadListener uploadListener;
    RecyclerView rvFolder;
    ImageView ivAdd;
    Toolbar toolbar;
    ArrayList<BookeoAlbum> folders;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BookeoCreateFolderAdapter bookeoFolderAdapter;
    BookeoAlbumDao dao;

    public CreateAlbumFragment() {

    }

    public static CreateAlbumFragment newInstance(String title, AlbumUploadListener listener) {
        CreateAlbumFragment createAlbumFragment = new CreateAlbumFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        createAlbumFragment.setArguments(args);
        createAlbumFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        uploadListener = listener;
        return createAlbumFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_album, container, false);

        rvFolder = root.findViewById(R.id.rvFolders);
        rvFolder.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        folders = getUserFolders("root");
        dao = new BookeoAlbumDao(this, this, getContext());
        bookeoFolderAdapter = new BookeoCreateFolderAdapter(folders, getContext(), this, this, this);
        rvFolder.setAdapter(bookeoFolderAdapter);

        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(this);

        //DB - albums for user to upload to when long press
        ivAdd = root.findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            //noinspection ConstantConditions
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void addAlbum(BookeoAlbum album) {
        AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Bookeo Sub-Album", CreateAlbumFragment.this, album.getUuid());
        addAlbumFragment.show(getActivity().getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
    }


    @Override
    //https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                Intent intent = new Intent(MediaDisplayActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                dismiss();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreated(BookeoAlbum bookeoAlbum) {
        //when album added move to sub folder
        dao.getSubFolders(bookeoAlbum.getParent());
    }

    @Override
    public void onUploadAlbumClicked(String albumUuid) {
        uploadListener.onUploadAlbumClicked(albumUuid);
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                BookeoAlbum album = folders.get(0);
                if (album.getParent().equals("root")) {
                    dismiss();
                } else {
                    String parentUuid = album.getParent();
                    dao.getAlbum(parentUuid);
                }
                break;
            case R.id.ivAdd:
                AddAlbumFragment addAlbumFragment = AddAlbumFragment.newInstance("Create Top Level Album", CreateAlbumFragment.this, "root");
                addAlbumFragment.show(getActivity().getSupportFragmentManager(), Config.CREATE_BOOKEO_ALBUM);
                break;
        }
    }

    @Override
    public void onSubFolderResult(ArrayList<BookeoAlbum> albums) {
        folders.clear();
        folders.addAll(albums);
        bookeoFolderAdapter.updateDataSet(albums);
    }

    @Override
    public void onClick(String uuid) {
        //when item click get subfolders
        dao.getSubFolders(uuid);
    }

    @Override
    public void onComplete(BookeoAlbum album) {
        //for backward navigation - get parent album and uses its parent to get albums at the level
        String parentUuid = album.getParent();
        dao.getSubFolders(parentUuid);
    }

    private ArrayList<BookeoAlbum> getUserFolders(String parentUuid) {
        final ArrayList<BookeoAlbum> dbAlbums = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        db.collection("albums").whereEqualTo("fk_user", userId).whereEqualTo("parent", parentUuid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoAlbum album = new BookeoAlbum();
                                album = documentSnapshot.toObject(BookeoAlbum.class);
                                dbAlbums.add(album);
                            }
                        }
                        bookeoFolderAdapter.notifyDataSetChanged();
                    }
                });
        Log.d("SIZE", "getAlbums: added" + dbAlbums.size());
        return dbAlbums;
    }
}
