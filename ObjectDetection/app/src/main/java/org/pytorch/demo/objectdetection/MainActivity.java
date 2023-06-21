// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    //private final String[] mTestImages = {"1.jpg", "2.jpg", "3.jpg","4.jpg","5.jpg","6.jpg","7.jpg","8.jpg","9.jpg","10.jpg","11.jpg","12.jpg","13.jpg","14.jpg","15.jpg","16.jpg","17.jpg","18.jpg","19.jpg","20.jpg","21.jpg","22.jpg","23.jpg","24.jpg","25.jpg","26.jpg","27.jpg","28.jpg","29.jpg","30.jpg","31.jpg","32.jpg","33.jpg","34.jpg","35.jpg","36.jpg"};
    private final String[] mTestImages={"1.jpg", "2.jpg", "3.jpg","4.jpg","5.jpg","6.jpg","7.jpg","8.jpg","9.jpg","10.jpg"};
    private ImageView mImageView;
    private ResultView mResultView;
    private Button mButtonDetect;
    private Button mCropButton;

    private Uri imagepath;
    private String picturepath;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }


        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
            mBitmap= resizeImage(mBitmap,PrePostProcessor.mInputWidth,PrePostProcessor.mInputWidth);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        mResultView=findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);

        final Button buttonLive=findViewById(R.id.livebutton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent= new Intent(MainActivity.this,ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        final Button buttonTest = findViewById(R.id.testButton);
        buttonTest.setText(("Test Image 1/10"));
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mImageIndex = (mImageIndex + 1) % mTestImages.length;
                buttonTest.setText(String.format("Text Image %d/%d", mImageIndex+1, mTestImages.length));

                try {
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
                    mBitmap= resizeImage(mBitmap,PrePostProcessor.mInputWidth,PrePostProcessor.mInputWidth);
                    mImageView.setImageBitmap(mBitmap);
                } catch (IOException e) {
                    Log.e("Object Detection", "Error reading assets", e);
                    finish();
                }
            }
        });


        final Button buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);

                //final CharSequence[] options = { "Choose from Photos", "Cancel" };
                final CharSequence[] options = { "Choose from Photos", "Take Picture", "Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Test Image");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Picture")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }
                        else if (options[item].equals("Choose from Photos")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1);
                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_round_corners);
                dialog.show();
            }
        });

        mCropButton=findViewById(R.id.cropButton);
        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCropActivity();
            }
        });

        mButtonDetect = findViewById(R.id.saveImageButtom);
        mProgressBar = findViewById(R.id.progressBar);
        mButtonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonDetect.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonDetect.setText(getString(R.string.run_model));

                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());

                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                Thread thread = new Thread(MainActivity.this);
                thread.start();
            }
        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "best.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    public void FAQ(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FAQ");
        builder.setMessage("Instrucciones:\n" +
                "Gracias por usar la app de DrBerry, para poder empezar el análisis sigue los siguientes pasos:\n" +
                "1.- Elige una foto de la hoja de arándanos que desees analizar usando el botón de SELECT. Ten en cuenta que una mejor iluminación ayudará a tener mejores resultados! Si no tienes imágenes o solo deseas probar la aplicación puedes elegir entre nuestras imágenes precargadas usando el boton TEST IMAGE.\n" +
                "2.- Recorta la imagen usando el botón RECORTE, este es un paso opcional que nos ayudará a eliminar contenido inecesario en los bordes de la imagen, así como reducir el tamaño de esta. Una imagen más pequeña es más rápida de procesar, pero recuerda que una imagen pequeña también puede tener muy poca información!\n" +
                "3.- Toca el botón DETECT, esto iniciará el análisis de la imagen. Luego de unos instantes, tu imagen aparecerá con uno o más cuadros indicando el nombre de la enfermedad, así como la certeza que tiene la aplicación de la predicción hecha.\n" +
                "4.-  Luego de esto, se te permitirá guardar la imagen analizada a la cual le podrás poner un nombre y una descripción, esta información será guardada en los metadatos de la imagen.\n" +
                "5.- ¿Tu imagen no dió un resultado? No te preocupes! Intenta denuevo recortando la imagen o probando una nueva imagen. Recuerda que DrBerry solo detecta las plagas de Oídio, Alternaria y Heliotis.");
        builder.setNegativeButton("X", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acciones a realizar cuando se hace clic en el botón negativo
                dialog.dismiss(); // Cerrar el cuadro de diálogo
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_round_corners);
        dialog.show();
    }

    public void gotoSearch(View view){
        Intent search=new Intent(this,searchImages.class);
        startActivity(search);
    }
    private void startCropActivity() {
        if (imagepath!=null){
            CropImage.activity(imagepath).setMinCropResultSize(1024,1024)
                    .start(this);
        }else {
            //CropImage.startPickImageActivity();
            CropImage.activity().setMinCropResultSize(1024,1024).start(this);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle extras=data.getExtras();
                        mBitmap = (Bitmap) extras.get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(0.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        mBitmap=resizeImage(mBitmap,PrePostProcessor.mInputWidth,PrePostProcessor.mInputHeight);
                        mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                picturepath= cursor.getString(columnIndex);
                                imagepath = selectedImage;
                                mBitmap = BitmapFactory.decodeFile(picturepath);
                                mBitmap= resizeImage(mBitmap,PrePostProcessor.mInputWidth,PrePostProcessor.mInputHeight);
                                mImageView.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                try {
                    mBitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    mBitmap= resizeImage(mBitmap,PrePostProcessor.mInputWidth,PrePostProcessor.mInputHeight);
                    mImageView.setImageBitmap(mBitmap);
                    Toast.makeText(this,"Recorte realizado",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this,"No se pudo realizar el recorte",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Bitmap resizeImage(Bitmap image,int maxWidth,int maxHeight){
        //int width = image.getWidth();
        //int height = image.getHeight();

        // Verificar si es necesario redimensionar la imagen
        //if (width <= maxWidth && height <= maxHeight) {
            //return image; // La imagen ya está dentro de los límites
        //}
        // Redimensionar la imagen con los nuevos tamaños
        //Bitmap resizedBitmap = Bitmap.createScaledBitmap(image, maxWidth, maxHeight, true);
        return Bitmap.createScaledBitmap(image, maxWidth, maxHeight, true);
    }

    @Override
    public void run() {
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(mBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();

        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);


        runOnUiThread(() -> {
            mButtonDetect.setEnabled(true);
            mButtonDetect.setText(getString(R.string.detect));
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            //mResultView.invalidate();
            //mResultView.setVisibility(View.VISIBLE);
            Bitmap nBitmap=mResultView.getBitmapFromResults(mBitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            nBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // Adjust quality as needed
            byte[] byteArray = stream.toByteArray();
            Intent intentresultView=new Intent(this, ResultDownload.class);
            intentresultView.putExtra("bitmap_result",byteArray);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            startActivity(intentresultView);
        });

    }
}
