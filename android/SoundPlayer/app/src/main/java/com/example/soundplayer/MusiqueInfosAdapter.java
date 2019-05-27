package com.example.soundplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MusiqueInfosAdapter extends ArrayAdapter<MusiqueInfo> {

    Context context;

    public MusiqueInfosAdapter(Context context, int resourceId,
                               List<MusiqueInfo> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MusiqueInfo musique = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.info_musique, null);

        TextView textView = convertView.findViewById(R.id.info_content);
        String text = musique.getTitre() + " " + musique.getAlbum() + "\n" + musique.getArtiste();
        textView.setText(text);

        /*
         * Décodage de l'image d'un album, précédement encodé en Base64,
         * génération d'une bitmap à partir de l'image décodée
         * from: https://stackoverflow.com/questions/4837110/how-to-convert-a-base64-string-into-a-bitmap-image-to-show-it-in-a-imageview
         */
        ImageView imageView = convertView.findViewById(R.id.cover_content);
        byte[] decodedString = Base64.decode(musique.getImageString(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);


        return convertView;
    }
}
