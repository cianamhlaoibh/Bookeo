package ie.bookeo.view.bookeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import java.util.ArrayList;

import ie.bookeo.R;
import ie.bookeo.dao.bookeo.BookeoMediaItemDao;
import ie.bookeo.model.bookeo.BookeoMediaItem;
import ie.bookeo.utils.FirebaseMediaItemsResultListener;
import ie.bookeo.utils.ShowGallery;

/**
 * Reference
 *  - URL - https://github.com/yuriy-budiyev/code-scanner
 *  - Creator - Yuriy Budiyev
 *  - Modified by Cian O Sullivan
 *
 **/

public class CodeScannerActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    public static final int PERMISSION_REQUEST = 111;
    CodeScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_scanner);
        scannerView = findViewById(R.id.scanner_view);
        if(checkWriteExternalPermission())
            _init();
        else
            grantPermission();
    }

    public void _init() {
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Scan Successful", Toast.LENGTH_SHORT).show();
                        ShowGallery.qrShow(getApplicationContext(), result.getText());
                        finish();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
        mCodeScanner.startPreview();
    }

    public void _init_res() {
        mCodeScanner.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkWriteExternalPermission())
            _init_res();
        else
            grantPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
    }

    private boolean checkWriteExternalPermission()
    {
        String permission = Manifest.permission.CAMERA;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void grantPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _init();
                }else{
                    finish();
                }
            }
        }
    }


}