package cl.diegoherrera.codescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.Line;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScannerActivity extends AppCompatActivity implements Camera.AutoFocusCallback {

    private BarcodeDetector detector;
    private SurfaceView cameraPreview;
    private CameraSource cameraSource;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private String data;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadView();

        builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Mensaje")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("data", data);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                })
                .setNegativeButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            cameraSource.start(cameraPreview.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SecurityException e){
                            e.printStackTrace();
                        }
                    }
                });

        detector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setAutoFocusEnabled(true)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SecurityException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> codes = detections.getDetectedItems();

                if(codes.size() > 0){
                    tvInfo.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog == null || !dialog.isShowing()) {
                                Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(1000);

                                cameraSource.stop();
                                //txtResult.setText(codes.valueAt(0).displayValue);

                                builder.setMessage(codes.valueAt(0).displayValue);
                                data = codes.valueAt(0).displayValue;

                                dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                }

            }
        });

    }

    private void loadView() {
        //Sizes
        RelativeLayout.LayoutParams lpMPMP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams lpWCWC = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lpWCMPLeftRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.4f);
        LinearLayout.LayoutParams lpWCMPCenter = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams lpMPWCTopBotton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.1f);
        LinearLayout.LayoutParams lpMPWCMiddle = new LinearLayout.LayoutParams(2, 0, 1);
        lpMPWCMiddle.gravity = Gravity.CENTER_HORIZONTAL;

        //Relative layout base
        RelativeLayout rlBase = new RelativeLayout(this);
        rlBase.setLayoutParams(lpMPMP);

        //Camera preview (SurfaceView)
        cameraPreview = new SurfaceView(this);
        cameraPreview.setLayoutParams(lpMPMP);

        //TextView (required to async response of the detector)
        tvInfo = new TextView(this);
        tvInfo.setLayoutParams(lpWCWC);
        tvInfo.setVisibility(View.GONE);

        //Interface of semi-transparent borders
        LinearLayout llBeauty = new LinearLayout(this);
        llBeauty.setLayoutParams(lpMPMP);
        llBeauty.setOrientation(LinearLayout.HORIZONTAL);

        //Left-Right semi-transparent borders
        View ivLeft = new View(this);
        ivLeft.setLayoutParams(lpWCMPLeftRight);
        ivLeft.setBackgroundColor(Color.parseColor("#44000000"));
        View ivRight = new View(this);
        ivRight.setLayoutParams(lpWCMPLeftRight);
        ivRight.setBackgroundColor(Color.parseColor("#44000000"));

        //Center view
        LinearLayout llCenter = new LinearLayout(this);
        llCenter.setLayoutParams(lpWCMPCenter);
        llCenter.setOrientation(LinearLayout.VERTICAL);

        //Top-Botton semi.transparent borders
        View ivTop = new View(this);
        ivTop.setLayoutParams(lpMPWCTopBotton);
        ivTop.setBackgroundColor(Color.parseColor("#44000000"));
        View ivBotton = new View(this);
        ivBotton.setLayoutParams(lpMPWCTopBotton);
        ivBotton.setBackgroundColor(Color.parseColor("#44000000"));

        //Red line in middle
        ImageView ivRedLine = new ImageView(this);
        ivRedLine.setLayoutParams(lpMPWCMiddle);
        ivRedLine.setBackgroundColor(Color.parseColor("#afd60303"));

        //Add Views to Center
        llCenter.addView(ivTop);
        llCenter.addView(ivRedLine);
        llCenter.addView(ivBotton);

        //Add View to beauty
        llBeauty.addView(ivLeft);
        llBeauty.addView(llCenter);
        llBeauty.addView(ivRight);

        //Add views to base layout
        rlBase.addView(tvInfo);
        rlBase.addView(cameraPreview);
        rlBase.addView(llBeauty);

        setContentView(rlBase);
    }

    /**
     * Detect touch point to make a focus
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            float x =  event.getX();
            float y = event.getY();
            float touchMajor = event.getTouchMajor();
            float touchMinor = event.getTouchMinor();

            Rect touchRect = new Rect((int)(x - touchMajor / 2), (int)(y - touchMinor / 2), (int)(x + touchMajor / 2), (int)(y + touchMinor / 2));

            this.submitFocusAreaRect(touchRect);
        }
        return super.onTouchEvent(event);
    }

    /**
     * Calculate focus zomm, position, etc.
     * @param touchRect
     */
    private void submitFocusAreaRect(final Rect touchRect) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            Camera.Parameters cameraParameters = camera.getParameters();

                            if (cameraParameters.getMaxNumFocusAreas() == 0) {
                                return;
                            }

                            Rect focusArea = new Rect();

                            focusArea.set(touchRect.left * 2000 / cameraPreview.getWidth() - 1000,
                                    touchRect.top * 2000 / cameraPreview.getHeight() - 1000,
                                    touchRect.right * 2000 / cameraPreview.getWidth() - 1000,
                                    touchRect.bottom * 2000 / cameraPreview.getHeight() - 1000);

                            ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                            focusAreas.add(new Camera.Area(focusArea, 1000));


                            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            cameraParameters.setFocusAreas(focusAreas);
                            camera.setParameters(cameraParameters);

                            camera.autoFocus(this);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.getMessage();
                }catch (RuntimeException e){
                    e.getMessage();
                }

                break;
            }
        }

    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }


}
