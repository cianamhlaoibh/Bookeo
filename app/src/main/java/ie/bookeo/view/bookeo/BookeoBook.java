package ie.bookeo.view.bookeo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoMediaItemAdapter;
import ie.bookeo.adapter.bookeo.BookeoPagesAdapter;
import ie.bookeo.model.bookeo.BookeoMediaItem;

public class BookeoBook extends AppCompatActivity {

    RecyclerView rvPages;
    ArrayList<BookeoMediaItem> mediaItems;
    ProgressBar pbLoader;
    Toolbar tvFolderName;
    ImageButton ibAlbum;
    BookeoPagesAdapter adapter;
    TextView tvNoMedia;

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<BookeoMediaItem> items;
    String name, uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_book);

        uuid = getIntent().getStringExtra("albumUuid");
        mediaItems = new ArrayList<>();
        rvPages = findViewById(R.id.rvPages);
        rvPages.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        //DB
        items = new ArrayList<>();
        items = getDbMedia(uuid);

        adapter = new BookeoPagesAdapter(items, this);
        rvPages.setAdapter(adapter);
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
                                //BookeoMediaItem arItem = new BookeoMediaItem(item.getUuid(), item.getUrl(), item.getName(), item.getCaption(), item.getDate(), item.getAlbumUuid());

                                mediaItems.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        return mediaItems;
    }
}
