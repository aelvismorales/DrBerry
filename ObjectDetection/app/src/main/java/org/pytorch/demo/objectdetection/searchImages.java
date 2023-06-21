package org.pytorch.demo.objectdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class searchImages extends AppCompatActivity {
    String PathDirectory="";
    ArrayList<Item> items;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Images Saved");
        setContentView(R.layout.activity_search_images);

        recyclerView=findViewById(R.id.recyclerView);
        items=new ArrayList<>();
        scanImagesInDirectory();
        setAdapter();
    }

    private void setAdapter(){
        MyAdapter adapter= new MyAdapter(items);
        RecyclerView.LayoutManager Layout_Manager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(Layout_Manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
    private void scanImagesInDirectory() {

        File rootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File directory = new File(rootDirectory, "DrBerry");

        if (directory.exists() && directory.isDirectory()) {
            PathDirectory=directory.getAbsolutePath();
        } else {
            Toast.makeText(this,"We can't find directory DrBerry",Toast.LENGTH_SHORT).show();
        }

        File directory_1 = new File(PathDirectory);
        if (!directory.isDirectory()) {
            Toast.makeText(this,"We can't find directory DrBerry",Toast.LENGTH_SHORT).show();
        }

        File[] files = directory_1.listFiles();
        if (files == null || files.length == 0) {
            Toast.makeText(this,"There is no image on DrBerry",Toast.LENGTH_SHORT).show();
        }
        for (File file : files) {
            if (file.isFile()) {
                // Obtener la ruta de la imagen
                String imagePath = file.getAbsolutePath();

                // Escanear metadatos de la imagen
                scanImageMetadata(imagePath);
            }
        }
    }

    private void scanImageMetadata(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);

            // Obtener metadatos específicos de la imagen
            //String imageDateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String path=exifInterface.getAttribute(ExifInterface.TAG_ARTIST);
            String latitude=exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude=exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String imageTitle = exifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT);
            String imageDescription = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);

            // Realizar acciones con los metadatos obtenidos
            // ...
            items.add(new Item(imageTitle,imageDescription,bitmap,path,latitude,longitude));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Anterior(View view){
        Intent anterior=new Intent(this,MainActivity.class);
        startActivity(anterior);
    }

}