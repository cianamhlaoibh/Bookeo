package ie.bookeo.view.bookeo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

import com.bumptech.glide.Glide;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.model.bookeo.BookeoPage;
import ie.bookeo.model.bookeo.MyCaptionStyle;
import ie.bookeo.utils.FirebaseResultListener;

public class EditCaptionActivity extends AppCompatActivity implements View.OnClickListener, FirebaseResultListener {

    ImageView ivCaption, ivFormat, ivSize, ivColor, ivDone;
    Spinner spFormat, spSize, spColor;
    EditText etCaption;
    String caption, format, size, color, id, albumUuid;
    BookeoMediaItemDao dao;
    BookeoPage page;
    BookeoMediaItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_caption);

        id = getIntent().getStringExtra("uuid");
        albumUuid = getIntent().getStringExtra("albumUuid");
        dao = new BookeoMediaItemDao(this);
        dao.getMediaItem(id, albumUuid);

        etCaption = findViewById(R.id.etCaption);
        ivCaption = findViewById(R.id.ivCaption);
        ivFormat = findViewById(R.id.ivFormat);
        ivSize = findViewById(R.id.ivSize);
        ivColor = findViewById(R.id.ivColor);
        ivDone = findViewById(R.id.ivDone);
        spFormat = findViewById(R.id.spFormat);
        spColor = findViewById(R.id.spColor);
        spSize = findViewById(R.id.spSize);

        ivDone.setOnClickListener(this);
        ivCaption.setOnClickListener(this);
        ivFormat.setOnClickListener(this);
        ivSize.setOnClickListener(this);
        ivColor.setOnClickListener(this);

        spFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                format = spFormat.getSelectedItem().toString();
                //https://stackoverflow.com/questions/6200533/how-to-set-textview-textstyle-such-as-bold-italic
                switch (format) {
                    case "Bold":
                        etCaption.setTypeface(etCaption.getTypeface(), Typeface.BOLD);
                        break;
                    case "Italic":
                        etCaption.setTypeface(etCaption.getTypeface(), Typeface.ITALIC);
                        break;
                    case "Normal":
                        etCaption.setTypeface(Typeface.create(etCaption.getTypeface(), Typeface.NORMAL));
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        spSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                size = spSize.getSelectedItem().toString();
                //https://stackoverflow.com/questions/6998938/textview-setting-the-text-size-programmatically-doesnt-seem-to-work/32470652
                switch (size) {
                    case "Small":
                        etCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,8);
                        break;
                    case "Medium":
                        etCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                        break;
                    case "Large":
                        etCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        spColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color = spColor.getSelectedItem().toString();
                switch (color) {
                    case "Black":
                        etCaption.setTextColor(Color.parseColor("#000000"));
                        break;
                    case "Red":
                        etCaption.setTextColor(Color.parseColor("#FF0000"));
                        break;
                    case "Beige":
                        etCaption.setTextColor(Color.parseColor("#CFB997"));
                        break;
                    case "Navy":
                        etCaption.setTextColor(Color.parseColor("#000080"));
                        break;
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCaption:
                etCaption.requestFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                break;
            case R.id.ivFormat:
                spFormat.setVisibility(View.VISIBLE);
                spSize.setVisibility(View.GONE);
                spColor.setVisibility(View.GONE);
                break;
            case R.id.ivSize:
                spSize.setVisibility(View.VISIBLE);
                spFormat.setVisibility(View.GONE);
                spColor.setVisibility(View.GONE);
                break;
            case R.id.ivColor:
                spColor.setVisibility(View.VISIBLE);
                spFormat.setVisibility(View.GONE);
                spSize.setVisibility(View.GONE);
                break;
            case R.id.ivDone:
                saveCaption();
                finish();
                break;
        }
    }

    private void saveCaption() {
        format = spFormat.getSelectedItem().toString();
        size = spSize.getSelectedItem().toString();
        color = spColor.getSelectedItem().toString();
        caption = etCaption.getText().toString();
        MyCaptionStyle style = new MyCaptionStyle(format, size, color);
        dao.updateCaption(albumUuid, id, caption, style);
    }

    @Override
    public void onComplete(BookeoMediaItem item) {

    }

    @Override
    public void onComplete(BookeoPage page) {
        this.page = page;
        this.item = page.getItem();
        etCaption.setText(page.getCaption());
        MyCaptionStyle style = page.getStyle();
        if (style != null) {
            style.applyCaptionStyle(style, etCaption);
            spFormat.setSelection(((ArrayAdapter)spColor.getAdapter()).getPosition(style.getColor()));
            spSize.setSelection(((ArrayAdapter)spSize.getAdapter()).getPosition(style.getColor()));
            spColor.setSelection(((ArrayAdapter)spColor.getAdapter()).getPosition(style.getColor()));
        }
    }
}