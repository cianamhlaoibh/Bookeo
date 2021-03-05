package ie.bookeo.view.bookeo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoAlbumDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.MyCreateListener;
import ie.bookeo.view.mediaExplorer.AddAlbumFragment;

public class InfoFragment extends DialogFragment implements View.OnClickListener {


    private TextView tvLocation;
    private Button btnCancel;
    private BookeoMediaItem item;

    public InfoFragment() {
    }


    public static InfoFragment newInstance(String title, String albumUuid, String uuid) {
        InfoFragment infoFragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        infoFragment.setArguments(args);
        args.putString("albumUuid",albumUuid);
        args.putString("uuid",uuid);
        infoFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);

        return infoFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_info, container, false);

        btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvLocation.setOnClickListener(this);

        String title = getArguments().getString(Config.TITLE);
        getDialog().setTitle(title);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            //noinspection ConstantConditions
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvLocation:
                Intent intent = new Intent(getContext(), BookeoMediaDisplay.class);
                intent.putExtra("folderUuid",getArguments().getString("albumUuid"));
                startActivity(intent);
                break;
            case R.id.btnCancel:
                dismiss();
                break;
        }
    }
}

