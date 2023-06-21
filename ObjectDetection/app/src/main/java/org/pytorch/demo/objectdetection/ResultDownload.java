package org.pytorch.demo.objectdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ResultDownload extends AppCompatActivity {

    private ImageView resultImageView;
    //private ImageButton returnButtom;
    //private Button takeImageButtom;
    private Button saveImageButtom;

    private EditText titleText;
    private EditText descriptionText;
    private Bitmap mBitmap = null;
    private String title = "";
    private String description = "";
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_result_download);
        Intent intent = getIntent();

        byte[] byteArray = intent.getByteArrayExtra("bitmap_result"); // El byteArray de la imagen comprimida

        mBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        // Obtén una instancia del LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Comprueba si tienes permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Obtén la última ubicación conocida
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                // Obtén los valores de latitud y longitud
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();

                // Utiliza los valores de latitud y longitud según sea necesario
            } else {
                // No se pudo obtener la ubicación actual
            }
        } else {
            // No se tienen los permisos necesarios para acceder a la ubicación
        }

        resultImageView=findViewById(R.id.imageView);
        resultImageView.setImageBitmap(mBitmap);


        saveImageButtom=findViewById(R.id.saveImageButtom);
        titleText=findViewById(R.id.titleText);
        descriptionText=findViewById(R.id.descriptionText);

        saveImageButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (titleText.getText().toString().length()>0){
                     title= titleText.getText().toString();
                     description=descriptionText.getText().toString();
                }
                ImageUtils.saveImageMetadata2(ResultDownload.this,mBitmap,title,description,latitude,longitude);
                Toast.makeText(ResultDownload.this, "Image successfully saved to file", Toast.LENGTH_SHORT).show();
                Intent search=new Intent(ResultDownload.this,MainActivity.class);
                startActivity(search);
            }
        });
    }

    public void Anterior(View view){
        Intent anterior=new Intent(this,MainActivity.class);
        startActivity(anterior);
    }


}