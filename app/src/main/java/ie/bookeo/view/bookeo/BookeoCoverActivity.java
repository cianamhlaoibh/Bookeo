package ie.bookeo.view.bookeo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebasePageResultListener;

/**
 * References
 *
 * - fetching data from firebase and returning it to the main thread [duplicate]
 * - URL - https://stackoverflow.com/questions/60194810/fetching-data-from-firebase-and-returning-it-to-the-main-thread
 *
 */

public class BookeoCoverActivity extends AppCompatActivity implements View.OnClickListener, FirebasePageResultListener {
    String id, albumUuid;
    BookeoPagesDao pagesDao;
    ArrayList<BookeoMediaItem> result;
    BookeoPage page;
    BookeoMediaItem item;
    ImageView ivCover,ivTitle, ivDelete, ivDone, ivQr, ivQrLrg;
    TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookeo_cover);

        id = getIntent().getStringExtra("id");
        albumUuid = getIntent().getStringExtra("albumUuid");

        pagesDao = new BookeoPagesDao(this);

        tvTitle = findViewById(R.id.tvTitle);

        ivCover = findViewById(R.id.ivCover);
        ivTitle = findViewById(R.id.ivTitle);
        ivQr = findViewById(R.id.ivQR);
        ivQrLrg = findViewById(R.id.ivQrLrg);
        ivDone = findViewById(R.id.ivDone);
        ivDelete = findViewById(R.id.ivColor);
        ivDone.setOnClickListener(this);
        ivTitle.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivQr.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pagesDao.getPage(albumUuid, id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivDone:
                if(page.getEnlarged() == null){
                    page.setEnlarged(false);
                }
                pagesDao.updateEnlargement(albumUuid, id, page.getEnlarged());
                finish();
                break;
            case R.id.ivTitle:
                Intent intent = new Intent(this, EditTitleActivity.class);
                intent.putExtra("albumUuid", albumUuid);
                intent.putExtra("uuid", id);
                startActivity(intent);
                break;
            case R.id.ivQR:
                if(ivQrLrg.getVisibility() == View.GONE){
                    ivQrLrg.setVisibility(View.VISIBLE);
                }else{
                    ivQrLrg.setVisibility(View.GONE);
                }
        }
    }

    // Function to create the QR code
    public static Bitmap createQR(String data){
        Bitmap myBitmap = QRCode.from(data).bitmap();
        return myBitmap;
    }


    @Override
    public void onComplete(BookeoPage page) {
        this.page = page;
        this.item = page.getItem();

            Glide.with(this).load(item.getUrl()).centerCrop().into(ivCover);

        tvTitle.setText(page.getCaption());

        //GENERATE QR CODE - IF VIDEO CLIP
        //String extension = item.getName().substring(item.getName().lastIndexOf("."));
        //if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")) {
            // The data that the QR code will contain
            String data = item.getAlbumUuid();
            // Create the QR code and display
            Bitmap qr = createQR(data);
            Glide.with(this).load(qr).into(ivQr);
            Glide.with(this).load(qr).into(ivQrLrg);
            ivQr.setVisibility(View.VISIBLE);
        //}
    }

    @Override
    public void onComplete(ArrayList<BookeoPage> pages) {

    }
}
