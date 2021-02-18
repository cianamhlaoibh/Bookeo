package ie.bookeo.adapter.bookeo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

public class BookeoAlbumBookHolder extends RecyclerView.ViewHolder {

    ConstraintLayout layout;
    ImageView ivCover, ivArrow;
    TextView tvTitle, tvDate;

    public BookeoAlbumBookHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.layout);
        ivCover = itemView.findViewById(R.id.ivCover);
        tvTitle = itemView.findViewById(R.id.tvTitle);
        tvDate = itemView.findViewById(R.id.tvDate);
        ivArrow = itemView.findViewById(R.id.ivArrow);
    }
}
