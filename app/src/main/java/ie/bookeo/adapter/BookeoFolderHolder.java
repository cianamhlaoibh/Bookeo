package ie.bookeo.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

public class BookeoFolderHolder extends RecyclerView.ViewHolder {

    CardView albumCard;
    TextView etAlbumName;

    public BookeoFolderHolder(@NonNull View itemView) {
        super(itemView);

        albumCard = itemView.findViewById(R.id.albumCard);
        etAlbumName = itemView.findViewById(R.id.tvAlbumName);
    }
}
