package ie.bookeo.adapter.drive;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

import ie.bookeo.R;

public class FolderHolder extends RecyclerView.ViewHolder {

    public TextView tvFolderName;

    public FolderHolder(@NonNull View itemView) {
        super(itemView);
        tvFolderName = itemView.findViewById(R.id.tvFolderName);
    }
}
