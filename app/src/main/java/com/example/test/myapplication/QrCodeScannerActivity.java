package com.example.test.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final int ZXING_CAMERA_PERMISSION = 1;

    private ZXingScannerView mScannerView;
    private boolean mFlash;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main2);
//        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.scan_code_layout);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.END;
        final ImageButton flashBtn = new ImageButton(this);
        flashBtn.setImageResource(R.drawable.flash_off);
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlash(view);
            }
        });
        flashBtn.setLayoutParams(params);
        contentFrame.addView(flashBtn);
        launchActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            mScannerView.setResultHandler(this);
            // You can optionally set aspect ratio tolerance level
            // that is used in calculating the optimal Camera preview size
            mScannerView.setAspectTolerance(0.2f);
            mScannerView.startCamera();
            mScannerView.setFlash(mFlash);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }

    @Override
    public void handleResult(Result rawResult) {
        Toast.makeText(this, "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(QrCodeScannerActivity.this);
            }
        }, 2000);
        sendResultToPlugin(rawResult);
    }

    private void sendResultToPlugin(Result rawResult) {
        Intent intent = new Intent();
        intent.putExtra("SCAN_RESULT", rawResult.getText());
        intent.putExtra("SCAN_RESULT_FORMAT", rawResult.getBarcodeFormat());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void toggleFlash(View v) {
        mFlash = !mFlash;
        ImageView imageView = (ImageView) v;
        if (mFlash) {
            imageView.setImageResource(R.drawable.flash_icon);
        } else {
            imageView.setImageResource(R.drawable.flash_off);
        }
        mScannerView.setFlash(mFlash);
    }
    public void launchActivity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            mScannerView.stopCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mScannerView.stopCamera();
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
