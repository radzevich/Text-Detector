package com.example.textdetector;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Arrays;

public class ScannerActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView captureIV;
    private TextView resultTV;
    private Button snapBtn;
    private Button detectBtn;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTV = findViewById(R.id.idTVDetectedText);
        snapBtn = findViewById(R.id.idBtnSnap);
        detectBtn = findViewById(R.id.idBtnDetect);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    captureImage();
                } else {
                    requestPermissions();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            boolean cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermissionGranted) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                captureImage();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }

    private boolean checkPermissions() {
        int cameraPermissions = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermissions == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        int permissionCode = 200;
        ActivityCompat.requestPermissions(this, new String[] {CAMERA}, permissionCode);
    }

    private void captureImage() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void detectText() {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        TextRecognizerOptions options =
                new TextRecognizerOptions.Builder()
                        .build();

        TextRecognizer recognizer = TextRecognition.getClient(options);


        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {

                StringBuilder sb = new StringBuilder();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    sb.append(blockText);

                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();

                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();

                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();

                        for (Text.Element element : line.getElements()) {
                          String elementText = element.getText();
                        }
                    }
                }

                resultTV.setText(sb);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Fail to detect text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}