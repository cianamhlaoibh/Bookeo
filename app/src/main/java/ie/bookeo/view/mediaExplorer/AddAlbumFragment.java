package ie.bookeo.view.mediaExplorer;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoAlbumDao;
import ie.bookeo.model.bookeo.BookeoAlbum;
import ie.bookeo.utils.Config;
import ie.bookeo.utils.MyCreateListener;


/**
 * Reference
 *  - Project Name - IS4447 SQLite 2 Table App
 *  - Creator - Michael Gleeson
 *  - Modified by Cian O Sullivan
 *
 *  - To add items from firestore
 *  - URL - https://www.youtube.com/watch?v=Bh0h_ZhX-Qg
 *  - Creator - Coding in Flow
 *
 */

public class AddAlbumFragment extends DialogFragment {

    public static MyCreateListener mylistener;
    private EditText etAlbumName;
    private Button btnAdd, btnCancel;
    private String name;
    private BookeoAlbumDao bookeoAlbumDao;

    public AddAlbumFragment() {

    }


    public static AddAlbumFragment newInstance(String title, MyCreateListener listener, String parentUuid){
        mylistener = listener;
        AddAlbumFragment albumCreateDialogFragment = new AddAlbumFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        albumCreateDialogFragment.setArguments(args);
        args.putString("parentUuid",parentUuid);
        albumCreateDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);

        return albumCreateDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bookeo_album_create_dialog, container, false);

        etAlbumName = view.findViewById(R.id.etAlbumName);
        btnAdd = view.findViewById(R.id.btnUpload);
        btnCancel = view.findViewById(R.id.btnCancel);

        String title = getArguments().getString(Config.TITLE);
        getDialog().setTitle(title);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAlbum(view);
                //https://stackoverflow.com/questions/41664409/wait-for-5-seconds/41664445https://stackoverflow.com/questions/41664409/wait-for-5-seconds/41664445
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
                        getDialog().dismiss();;
//                    }
//                }, 1000);//wait a second to add album to database before dismissing activity

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    //https://www.youtube.com/watch?v=Bh0h_ZhX-Qg
    public void addAlbum(View view) {
        name = etAlbumName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            etAlbumName.setError("Please enter album name");
            return;
        }
        Date today = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
        String date = DATE_FORMAT.format(today);
        //Read more: https://www.java67.com/2013/01/how-to-format-date-in-java-simpledateformat-example.html#ixzz6fppjpeYL
        final String uuid = UUID.randomUUID().toString();
        String parent = getArguments().getString("parentUuid");
        //assign to user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        //add to firebase
        final BookeoAlbum bookeoAlbum = new BookeoAlbum(uuid, name, date, userId, parent);
        bookeoAlbumDao = new BookeoAlbumDao();
        int[] result = bookeoAlbumDao.addAlbum(bookeoAlbum, getContext());
        //If added to firestore update rvAlbum
        if(result[0] != -1){
            mylistener.onCreated(bookeoAlbum);
        }
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
}
