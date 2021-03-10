package ie.bookeo.view.bookeo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoPagesDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebasePageResultListener;

public class EditTitleActivity extends AppCompatActivity implements View.OnClickListener, FirebasePageResultListener {

    ImageView ivCaption, ivDone;
    EditText etCaption;
    String caption, id, albumUuid;
    BookeoPagesDao dao;
    BookeoPage page;
    BookeoMediaItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_title);

        id = getIntent().getStringExtra("uuid");
        albumUuid = getIntent().getStringExtra("albumUuid");
        dao = new BookeoPagesDao(this);
        dao.getPage(albumUuid, id);

        etCaption = findViewById(R.id.etCaption);
        ivCaption = findViewById(R.id.ivCaption);
        ivDone = findViewById(R.id.ivDone);

        ivDone.setOnClickListener(this);
        ivCaption.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCaption:
                etCaption.requestFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                break;
            case R.id.ivDone:
                saveCaption();
                finish();
                break;
        }
    }

    private void saveCaption() {
        caption = etCaption.getText().toString();
        dao.updateTitle(albumUuid, id, caption);
    }

    @Override
    public void onComplete(BookeoPage page) {
        this.page = page;
        this.item = page.getItem();
        etCaption.setText(page.getCaption());
        MyCaptionStyle style = page.getStyle();
    }

    @Override
    public void onComplete(ArrayList<BookeoPage> pages) {

    }
}