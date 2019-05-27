package com.example.soundplayer;

public class MusiqueInfo {
    private String titre;
    private String album;
    private String artiste;
    private String imageString;

    public MusiqueInfo(String titre, String album, String artiste, String imageString) {
        this.titre = titre;
        this.album = album;
        this.artiste = artiste;
        this.imageString = imageString;
    }

    public String getTitre() {
        if(titre.equals("")) {
            return titre;
        }
        return titre + ":";
    }

    @Override
    public String toString() {
        return "MusiqueInfo{" +
                "titre='" + titre + '\'' +
                ", album='" + album + '\'' +
                ", artiste='" + artiste + '\'' +
                ", imageString='" + imageString + '\'' +
                '}';
    }

    public String getAlbum() {
        return album;
    }

    public String getArtiste() {
        return artiste;
    }

    public String getImageString() {
        return imageString;
    }
}
