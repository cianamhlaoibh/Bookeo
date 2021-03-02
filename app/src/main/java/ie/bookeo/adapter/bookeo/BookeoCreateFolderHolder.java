package ie.bookeo.adapter.bookeo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ie.bookeo.R;

public class BookeoCreateFolderHolder  extends RecyclerView.ViewHolder{
    TextView tvFolderName;
    ImageView ivAdd, ivUpload;

    public BookeoCreateFolderHolder(@NonNull View itemView) {
        super(itemView);
        tvFolderName = itemView.findViewById(R.id.tvFolderName);
        ivAdd = itemView.findViewById(R.id.ivAdd);
        ivUpload = itemView.findViewById(R.id.ivUpload);
    }
}
