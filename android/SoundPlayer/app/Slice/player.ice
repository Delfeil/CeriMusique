module Player{
  dictionary<string, string> infos;
  sequence<infos> infoMusiques;
  sequence<infos> infoAlbum;

  interface PlayerServeur{
    string playMusiqueTest();
    string playMusique(string name);
    string playMusiqueArtiste(string name);
    string playMusiqueAlbum(string name);
    string printAllMusique();
    string printMusique(string titre);
    string printMusiqueByArtiste(string artiste);
    string printMusiqueByAlbum(string album);
    string printAllAlbum();
    string printAlbum(string name);
    string printAlbumByArtiste(string artiste);
    void stop();
    void pause();
    void reprendre();
    void next();
    void previous();
  };
};