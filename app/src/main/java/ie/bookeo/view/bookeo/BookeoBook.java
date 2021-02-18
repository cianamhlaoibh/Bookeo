package ie.bookeo.view.bookeo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoMediaItemAdapter;
import ie.bookeo.adapter.bookeo.BookeoPagesAdapter;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;

/**
 * Reference
 *  - URL - https://androidapps-development-blogs.medium.com/drag-and-drop-reorder-in-recyclerview-android-2a3093d16ba2
 *  - Creator - Golap Gunjan Barman
 *
 */

public class BookeoBook extends AppCompatActivity {

    RecyclerView rvPages;
    BookeoPagesAdapter adapter;
    BookeoMediaItemDao dao;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<BookeoMediaItem> items;
    String uuid;
    Boolean isGenerated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_book);
        dao = new BookeoMediaItemDao();
        uuid = getIntent().getStringExtra("albumUuid");
        isGenerated = getIntent().getBooleanExtra("isGenerated", true);
        rvPages = findViewById(R.id.rvPages);
        rvPages.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPages);
    }

    //https://androidapps-development-blogs.medium.com/drag-and-drop-reorder-in-recyclerview-android-2a3093d16ba2
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            dao.updatePosition(items.get(fromPosition).getAlbumUuid(), items.get(fromPosition).getUuid(), toPosition);
            dao.updatePosition(items.get(toPosition).getAlbumUuid(), items.get(toPosition).getUuid(), fromPosition);
            Collections.swap(items, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    } ;

    @Override
    protected void onResume() {
        super.onResume();
        //DB
        items = new ArrayList<>();
        if(isGenerated) {
            items = getDbMediaOrdered(uuid);
        }else{
            items = getDbMedia(uuid);
        }
        adapter = new BookeoPagesAdapter(items, this);
        rvPages.setAdapter(adapter);
    }

    public ArrayList<BookeoMediaItem> getDbMediaOrdered(String albumUuid) {
        final ArrayList<BookeoMediaItem> mediaItems = new ArrayList<>();

        db.collection("albums").document(albumUuid).collection("media_items").orderBy("position").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoMediaItem item;
                                item = documentSnapshot.toObject(BookeoMediaItem.class);
                                mediaItems.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        return mediaItems;
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
                                BookeoMediaItem item;
                                item = documentSnapshot.toObject(BookeoMediaItem.class);
                                mediaItems.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        return mediaItems;
    }
}
