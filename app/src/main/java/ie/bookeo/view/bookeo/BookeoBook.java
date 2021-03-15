package ie.bookeo.view.bookeo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.adapter.bookeo.BookeoPagesAdapter;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.utils.FirebaseMediaItemsResultListener;
import ie.bookeo.utils.FirebasePageResultListener;
import ie.bookeo.view.mediaExplorer.MainActivity;

/**
 * Reference
 *  - URL - https://androidapps-development-blogs.medium.com/drag-and-drop-reorder-in-recyclerview-android-2a3093d16ba2
 *  - Creator - Golap Gunjan Barman
 *
 */

public class BookeoBook extends AppCompatActivity implements View.OnClickListener, FirebaseMediaItemsResultListener, FirebasePageResultListener {

    RecyclerView rvPages;
    Toolbar toolbar;
    TextView tvTitle;
    BookeoPagesAdapter adapter;
    BookeoMediaItemDao dao;
    BookeoMediaItemDao itemsDao;
    BookeoPagesDao pagesDao;
    BookeoPage coverPage;
    ConstraintLayout cover;
    ImageView ivBackgroud, ivAddPage, ivDeletePage, ivDeleteBook, ivAddCover, ivCoverPage, ivQR;
    String status = "DEACTIVE";

    //DB
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<BookeoPage> pages;
    ArrayList<BookeoMediaItem> items;
    String uuid;
    Boolean isGenerated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_book);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dao = new BookeoMediaItemDao();

        uuid = getIntent().getStringExtra("albumUuid");
        isGenerated = getIntent().getBooleanExtra("isGenerated", true);
        rvPages = findViewById(R.id.rvPages);
        rvPages.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPages);

        ivAddPage = findViewById(R.id.ivAddPage);
        ivAddPage.setOnClickListener(this);
        ivDeletePage = findViewById(R.id.ivDeletePage);
        ivDeletePage.setOnClickListener(this);
        ivAddCover = findViewById(R.id.ivAddCover);
        ivAddCover.setOnClickListener(this);
        ivCoverPage = findViewById(R.id.ivCoverPage);
        cover = findViewById(R.id.cover);
        ivCoverPage.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        ivQR = findViewById(R.id.ivQR);
    }

    //https://androidapps-development-blogs.medium.com/drag-and-drop-reorder-in-recyclerview-android-2a3093d16ba2
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            pagesDao.updatePosition(pages.get(fromPosition).getAlbumUuid(), pages.get(fromPosition).getPageUuid(), toPosition);
            pagesDao.updatePosition(pages.get(toPosition).getAlbumUuid(), pages.get(toPosition).getPageUuid(), fromPosition);
            Collections.swap(pages, fromPosition, toPosition);
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
        pages = new ArrayList<>();
        if(isGenerated) {
            pages = getDbMediaOrdered(uuid);
        }else{
            pages = getDbMedia(uuid);
        }
        adapter = new BookeoPagesAdapter(pages, this);
        rvPages.setAdapter(adapter);
        itemsDao = new BookeoMediaItemDao(this);
        itemsDao.getMediaItems(uuid);
        pagesDao = new BookeoPagesDao(this);
        pagesDao.getCoverPage(uuid);
        adapter.notifyDataSetChanged();
    }

    public ArrayList<BookeoPage> getDbMediaOrdered(String albumUuid) {
        final ArrayList<BookeoPage> pages = new ArrayList<>();
        db.collection("albums").document(albumUuid).collection("pages").whereEqualTo("type","standard").orderBy("pageNumber").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoPage page;
                                page = documentSnapshot.toObject(BookeoPage.class);
                                pages.add(page);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        return pages;
    }
    public ArrayList<BookeoPage> getDbMedia(String albumUuid) {
        final ArrayList<BookeoPage> pages = new ArrayList<>();
        db.collection("albums").document(albumUuid).collection("pages").whereEqualTo("type","standard").orderBy("pageNumber").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {
                                BookeoPage page;
                                page = documentSnapshot.toObject(BookeoPage.class);
                                pages.add(page);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        return pages;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivAddPage:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Select and Upload Media to add new pages to your book", Toast.LENGTH_LONG).show();
                break;
            case R.id.ivDeletePage:
                if(status.equals("DEACTIVE")) {
                    adapter.activateButtons(false);
                    status = "ACTIVE";
                }else{
                    adapter.activateButtons(true);
                    status = "DEACTIVE";
                }
                break;
            case R.id.ivAddCover:
                //if(cover!=null) {
                    Intent selectIntent = new Intent(getApplicationContext(), BookeoMediaDisplay.class);
                    selectIntent.putExtra("folderUuid", uuid);
                    selectIntent.putExtra("selectMode", true);
                    startActivity(selectIntent);
               // }
                break;
            case R.id.ivCoverPage:
                Intent viewPage = new Intent(getApplicationContext(), BookeoCoverActivity.class);
                viewPage.putExtra("id", coverPage.getPageUuid());
                viewPage.putExtra("albumUuid", coverPage.getAlbumUuid());
                startActivity(viewPage);
                break;
        }
    }

    private void updateAlbumBook() {
        ArrayList<String> uuids = new ArrayList<>();
        ArrayList<String> itemUuids = new ArrayList<>();
        //adds new images
        for (BookeoPage page : pages) {
            uuids.add(page.getItem().getUuid());
        }
        for (BookeoMediaItem item : items) {
            itemUuids.add(item.getUuid());
        }
        for (BookeoMediaItem item : items) {
            if (!uuids.contains(item.getUuid())) {
                String pageUuid = UUID.randomUUID().toString();

                BookeoPage page = new BookeoPage();
                page.setPageUuid(pageUuid);
                page.setPageNumber(items.indexOf(item));
                page.setType("standard");
                page.setAlbumUuid(item.getAlbumUuid());
                page.setItem(item);

                //upload item
                pagesDao.addPage(page, this.uuid);
                pages.add(page);
                adapter.notifyDataSetChanged();
            }
        }
        //remove Imaged
        for(BookeoPage page : pages){
            if(!itemUuids.contains(page.getItem().getUuid())){
                pagesDao.deletePage(this.uuid, page.getPageUuid());
                pages.remove(page);
            }
        }
    }

    @Override
    public void OnComplete(ArrayList<BookeoMediaItem> items) {
        this.items = new ArrayList<>();
        if(!this.items.isEmpty()) {
            this.items.clear();
        }
        this.items.addAll(items);
        updateAlbumBook();
    }

    @Override
    //https://stackoverflow.com/questions/35810229/how-to-display-and-set-click-event-on-back-arrow-on-toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this,BookeoMain.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onComplete(BookeoPage item) {
        coverPage = item;
        if(item == null){
            ivAddCover.setVisibility(View.GONE);
        }else {
            Glide.with(getApplicationContext())
                    .load(item.getItem().getUrl())
                    .centerCrop()
                    .into(ivCoverPage);
            ivAddCover.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(item.getCaption());
            String data = item.getAlbumUuid();
            // Create the QR code and display
            Bitmap qr = createQR(data);
            Glide.with(this).load(qr). into(ivQR);
            ivQR.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onComplete(ArrayList<BookeoPage> pages) {

    }
    public static Bitmap createQR(String data){
        Bitmap myBitmap = QRCode.from(data).bitmap();
        return myBitmap;
    }
}
