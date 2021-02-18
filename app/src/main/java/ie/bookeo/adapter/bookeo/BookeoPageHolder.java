package ie.bookeo.adapter.bookeo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

public class BookeoPageHolder extends RecyclerView.ViewHolder {

    ImageView ivPage, ivMedia, ivMediaLrg, ivQR;
    TextView tvCaption;

    public BookeoPageHolder(@NonNull View itemView) {
        super(itemView);

        ivPage = itemView.findViewById(R.id.ivPage);
        ivMedia = itemView.findViewById(R.id.ivMedia);
        ivQR = itemView.findViewById(R.id.ivQR);
        tvCaption = itemView.findViewById(R.id.tvCaption);
        ivMediaLrg = itemView.findViewById(R.id.ivImageLrg);
    }
}
