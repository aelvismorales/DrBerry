package org.pytorch.demo.objectdetection;

import android.annotation.SuppressLint;
import android.app.Dialog;

import android.media.ExifInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.myViewHolder> {

    private ArrayList<Item> items;
    public MyAdapter(ArrayList<Item> itemsList){
        this.items=itemsList;
    }
    public class myViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView titleView;
        private TextView descriptionView;

        private Button viewButton;

        public myViewHolder(final View view){
            super(view);
            imageView=view.findViewById(R.id.imageViewGet);
            titleView=view.findViewById(R.id.titleText);
            descriptionView=view.findViewById(R.id.descriptionText);
            viewButton=view.findViewById(R.id.viewButton);

        }

    }
    @NonNull
    @Override
    public MyAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
        return new myViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String tt=items.get(position).getTitle();
        String don=items.get(position).getDescription();
        String path=items.get(position).getPath();
        String latitude=items.get(position).getLatitude();
        String longitude=items.get(position).getLongitude();


        holder.titleView.setText(items.get(position).getTitle());
        holder.descriptionView.setText(items.get(position).getDescription());
        holder.imageView.setImageBitmap(items.get(position).getImage());
        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog= new Dialog(view.getContext());
                dialog.setContentView(R.layout.view_item_result);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_round_corners);
                dialog.setCancelable(false);
                dialog.show();

                TextView title=dialog.findViewById(R.id.titleText);
                TextView description=dialog.findViewById(R.id.descriptionText);
                ImageView img=dialog.findViewById(R.id.imageViewItemResult);
                img.setImageBitmap(items.get(position).getImage());

                title.setText(tt);
                description.setText(don);
                Button update=dialog.findViewById(R.id.updateButton);
                ImageButton back=dialog.findViewById(R.id.returnButtomResult);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            ExifInterface exifInterface = new ExifInterface(path);
                            exifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT,title.getText().toString());
                            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,description.getText().toString());
                            exifInterface.saveAttributes();
                            items.get(position).setTitle(title.getText().toString());
                            items.get(position).setDescription(description.getText().toString());
                            notifyItemChanged(position);
                            Toast.makeText(view.getContext(),"Update succesfull",Toast.LENGTH_SHORT).show();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static double parseLatLong(String value) {
        String[] parts = value.split("/");
        double degrees = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);

        double result = degrees + (minutes / 60) + (seconds / 3600);

        // Verificar el signo
        if (value.startsWith("-")) {
            result = -result;
        }

        return result;
    }

}
