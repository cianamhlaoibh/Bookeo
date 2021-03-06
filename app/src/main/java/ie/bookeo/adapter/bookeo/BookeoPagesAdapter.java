package ie.bookeo.adapter.bookeo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;
import java.util.List;


import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.view.bookeo.BookeoPageActivity;


/**
 * Reference
 *  - URL - https://github.com/kenglxn/QRGen
 *  - Creator - Ken Gullaksen
 *
 *  -URL: https://stackoverflow.com/questions/41404866/show-hide-buttons-in-recyclerview-adapter-from-activity
 */
public class BookeoPagesAdapter extends RecyclerView.Adapter<BookeoPageHolder>  {
    private List<BookeoPage> pages;
    private Context contx;
    private BookeoPagesDao dao;
    private BookeoMediaItemDao itemsDao;
    private boolean activate;

    public BookeoPagesAdapter(List<BookeoPage> pages, Context contx) {
        this.pages = pages;
        this.contx = contx;
        dao = new BookeoPagesDao();
        itemsDao = new BookeoMediaItemDao();
        activate = false;
    }

    @NonNull
    @Override
    public BookeoPageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contx).inflate(R.layout.holder_book_page, parent, false);
        return new BookeoPageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookeoPageHolder holder, int position) {
        final BookeoPage page = pages.get(position);
        final BookeoMediaItem item = page.getItem();

                   Glide.with(contx)
                           .load(item.getUrl())
                           .fitCenter()
                           .into(holder.ivMedia);
                   String caption = page.getCaption();
                   if (caption != null) {
                       holder.tvCaption.setText(caption);
                       MyCaptionStyle style = page.getStyle();
                       if (style != null) {
                           style.applyCaptionStyle(style, holder.tvCaption);
                       }
                   }else{
                       holder.tvCaption.setVisibility(View.GONE);
                   }
                   if (page.getEnlarged() == null || page.getEnlarged() == false) {
                       holder.ivMedia.setVisibility(View.VISIBLE);
                       holder.ivMediaLrg.setVisibility(View.GONE);
                       Glide.with(contx).load(item.getUrl()).into(holder.ivMedia);
                   } else if (page.getEnlarged() == true) {
                       holder.ivMediaLrg.setVisibility(View.VISIBLE);
                       holder.ivMedia.setVisibility(View.GONE);
                       Glide.with(contx).load(item.getUrl()).into(holder.ivMediaLrg);
                   }
               holder.ivRemove.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       dao.deletePage(page.getAlbumUuid(), page.getPageUuid());
                       itemsDao.deleteMediaItem(page.getAlbumUuid(), page.getItem().getUuid(), contx);
                       pages.remove(position);
                       notifyDataSetChanged();
                   }
               });
               //GENERATE QR CODE - IF VIDEO CLIP
               String extension = item.getName().substring(item.getName().lastIndexOf("."));
               if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mkv")) {
                   // The data that the QR code will contain
                   String data = item.getAlbumUuid()+","+item.getUuid();
                   // Create the QR code and display
                   Bitmap qr = createQR(data);
                   Glide.with(contx).load(qr).into(holder.ivQR);
                   holder.ivQR.setVisibility(View.VISIBLE);
               }else{
                   holder.ivQR.setVisibility(View.GONE);
               }


            holder.ivPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(contx, BookeoPageActivity.class);
                    intent.putExtra("id", page.getPageUuid());
                    intent.putExtra("position", position);
                    intent.putExtra("albumUuid", page.getAlbumUuid());
                    contx.startActivity(intent);
                }
            });

        if (activate) {
            holder.ivRemove.setVisibility(View.VISIBLE);
        } else {
            holder.ivRemove.setVisibility(View.INVISIBLE);
        }
    }

    // Function to create the QR code
    public static Bitmap createQR(String data){
        Bitmap myBitmap = QRCode.from(data).bitmap();
        return myBitmap;
    }

    public void activateButtons(boolean activate) {
        this.activate = activate;
        notifyDataSetChanged(); //need to call it for the child views to be re-created with buttons.
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}
