package com.example.cvimagedectector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SeekBar brightnessSeekBar;
    private SeekBar sharpnessSeekBar;
    private SeekBar noiseSeekBar;
    private static final int SELECT_IMAGE_REQUEST = 1;
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private ImageView imageView;
    private Mat image;
    private Bitmap bitmap;
    private Mat bmp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        sharpnessSeekBar = findViewById(R.id.sharpnessSeekBar);
        noiseSeekBar = findViewById(R.id.noiseSeekBar);
        brightnessSeekBar.setProgress(50);
        sharpnessSeekBar.setProgress(30);

        imageView = findViewById(R.id.imageView);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button calculateParametersButton = findViewById(R.id.calculateParametersButton);
        Button classifyButton = findViewById(R.id.classifyButton);
        Button clearButton = findViewById(R.id.clearButton);
        Button exitButton = findViewById(R.id.exitButton);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        sharpnessSeekBar = findViewById(R.id.sharpnessSeekBar);
        noiseSeekBar = findViewById(R.id.noiseSeekBar);
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        currentBitmap = originalBitmap;
        bitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        imageView.setImageBitmap(currentBitmap);
        double initialBrightness = 1.0; // Adjust as needed
        updateImageBrightness(initialBrightness);

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double brightness = (progress / 100.0) * 2.0; // Scale to the desired range

                // Update the image with the new brightness
                updateImageBrightness(brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        sharpnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                double alpha = (progress - 20) / 30.0; // Adjust this scale as needed
                adjustSharpness(alpha);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        noiseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                reduceNoise(progress);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        calculateParametersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,CalculateParameters.class));

            }
        });

        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               startActivity(new Intent(MainActivity.this,Classify.class));

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // startActivity(new Intent(MainActivity.this,ClearAll.class));
                // imageView.setImageResource(R.drawable.sample);
                brightnessSeekBar.setProgress(50);
                sharpnessSeekBar.setProgress(30);
                noiseSeekBar.setProgress(0);
                double alpha = (30 - 20) / 30.0; // Adjust this scale as needed
                adjustSharpness(alpha);
                reduceNoise(0);
                double brightness = (50 / 100.0) * 2.0; // Scale to the desired range

                // Update the image with the new brightness
                updateImageBrightness(brightness);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitConfirmationDialog();
            }
        });
    }
    private void openSystemGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST);
    }

    private void openDatasetPicker() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                // Load the selected image into the currentBitmap
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                if (selectedBitmap != null) {
                    currentBitmap = selectedBitmap;
                    imageView.setImageBitmap(currentBitmap);


                    image = new Mat();
                    Utils.bitmapToMat(selectedBitmap, image);
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void showImageSourceDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[]{"System Gallery", "Dataset"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Open the system gallery
                            openSystemGallery();
                        } else {
                            // Open the dataset image picker
                            openDatasetPicker();
                        }
                    }
                })
                // Set a custom background drawable
                .setPositiveButton("Cancel", null) // Add a cancel button if needed
                .show();
    }


    private void showExitConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);

        builder.setMessage("Do you want to exit from the application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void adjustSharpness(double alpha) {
        if (image != null) {

            Mat sharpenedImage = new Mat();


            Mat blurredImage = new Mat();
            Imgproc.GaussianBlur(image, blurredImage, new Size(0, 0), 5.0);
            Core.addWeighted(image, 1 + alpha, blurredImage, -alpha, 0, sharpenedImage);


            Bitmap bitmap = Bitmap.createBitmap(sharpenedImage.cols(), sharpenedImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(sharpenedImage, bitmap);


            imageView.setImageBitmap(bitmap);
        }
    }










    private void updateImageBrightness(double brightness) {
        if (image != null) {
            Mat adjustedImage = new Mat();
            image.convertTo(adjustedImage, -1, brightness, 0); // Adjust brightness

            Bitmap bitmap = Bitmap.createBitmap(adjustedImage.cols(), adjustedImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(adjustedImage, bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }


    private void reduceNoise(double noiseLevel) {
        if (image != null) {
            Mat denoisedImage = new Mat();


            Size kernelSize = new Size(5, 5);
            Imgproc.GaussianBlur(image, denoisedImage, kernelSize, noiseLevel, noiseLevel);

            Bitmap bitmap = Bitmap.createBitmap(denoisedImage.cols(), denoisedImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(denoisedImage, bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }







}
