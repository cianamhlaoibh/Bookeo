package ie.bookeo.adapter.drive;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ie.bookeo.R;

public class DriveFolderHolder extends RecyclerView.ViewHolder {

    TextView tvFolderName;

    public DriveFolderHolder(@NonNull View itemView) {
        super(itemView);
        tvFolderName = itemView.findViewById(R.id.tvFolderName);
    }
}
