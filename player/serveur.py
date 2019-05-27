import sys, Ice
import Player
import pymongo
import pprint
import vlc
import json
import base64

# client = pymongo.MongoClient("mongodb+srv://delfeilcasanova:delfcasa@cluster0-guvid.gcp.mongodb.net/test?retryWrites=true")
client = pymongo.MongoClient('localhost', 27017)
musiques = client.db['musique']

default_cover = "/home/casanova/Documents/Master-ilsen/s2/Application-Architecture/Player/musicDB/default_cover.png"

vlc_instance = vlc.Instance()
player = vlc_instance.media_list_player_new()
ip = "192.168.1.6"
# ip = "192.168.43.181"

##
# Serveur ICE gérant le lecteur demusiques,
# intéragie avec une base de donée mongoDb por récupérer la liste de musiques référencées
# Utilise libvlc pour faier du streaming
class PlayerI(Player.PlayerServeur):
  id_stream = 0
  def playMusiqueTest(self, current=None):
    media = vlc_instance.media_list_new()
    media.add_media(vlc_instance.media_new('/home/casanova/Documents/Master-ilsen/s2/Application-Architecture/Player/musicDB/emune/la_familia_pt_1/grind.mp3', 'sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100,scodec=none}:http{mux=mp3,dst=:8083/stream.mp3}', 'sout-all', 'sout_keep'))
    player.set_media_list(media)
    if player.is_playing():
      player.stop()
    player.play()
    return "http://"+ip+":8083/stream.mp3"
  def playMusique(self, titre, current=None):
    #Joue la musique...
    lMusiques = []
    for musique in musiques.find({"titre": titre}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_musique": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      musiqueInfos["path_musique"] = musique["path_musique"]
      # Serialisation de l'image de la musiquepour pouvoir la passer par le réseau
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      # Si aucunes musiques n'est trouvée, on retourne ""
      return "";
    return self.playlMusique(lMusiques)
  def playMusiqueArtiste(self, name, current=None):
    #Joue les musiques de l'artiste...
    lMusiques = []
    for musique in musiques.find({"artiste": name}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_musique": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      musiqueInfos["path_musique"] = musique["path_musique"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.playlMusique(lMusiques)
  def playMusiqueAlbum(self, name, current=None):
    #Joue les musiques de l'album...
    lMusiques = []
    for musique in musiques.find({"album": name}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_musique": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      musiqueInfos["path_musique"] = musique["path_musique"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.playlMusique(lMusiques)
  def playlMusique(self, lMusiques):
    # Fonction lançant le streaming à partir d'une liste de musiques récupérée précédement
    media = vlc_instance.media_list_new()
    stream_name = ':8083/stream'+str(self.id_stream)+'.mp3'
    for musique in lMusiques:
      print("-----> play: titre: " + musique["titre"] + ", album: " + musique["album"] + ", artiste: " + musique["artiste"])
      media.add_media(vlc_instance.media_new(musique["path_musique"], 'sout=#transcode{vcodec=none,acodec=mp3,ab=128,channels=2,samplerate=44100,scodec=none}:http{mux=mp3,dst='+stream_name+'}', 'sout-all', 'sout_keep'))
    player.set_media_list(media)
    if player.is_playing():
      player.stop()
    player.play()
    self.id_stream += 1
    #
    ## Retourne un json avec les infos des musiqes à jouer et de l'url du stream
    #
    res = json.dumps({
      "url": "http://"+ip+ stream_name,
      "info": lMusiques
    }, separators=(',', ':'))
    print("retruned: " + res)
    return res
  def printAllMusique(self, current=None):
    # Affiche toutes les musiques
    lMusiques = []
    for musique in musiques.find({}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.returnList(lMusiques)
  def printMusique(self, titre, current=None):
    #Affiche la musique...
    lMusiques = []
    for musique in musiques.find({"titre": titre}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.returnList(lMusiques)
  def printMusiqueByArtiste(self, artiste, current=None):
    # affiche les musiques de l'artiste...
    lMusiques = []
    for musique in musiques.find({"artiste": artiste}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.returnList(lMusiques)
  def printMusiqueByAlbum(self, album, current=None):
    # affiche les musiques de l'album...
    lMusiques = []
    for musique in musiques.find({"album": album}, {"_id": 0, "artiste": 1, "album": 1, "titre": 1, "path_image": 1}):
      musiqueInfos = {}
      musiqueInfos["titre"] = musique["titre"]
      musiqueInfos["artiste"] = musique["artiste"]
      musiqueInfos["album"] = musique["album"]
      if(musique["path_image"] == ""):
        musique["path_image"] = default_cover
      cover = open(musique["path_image"], "rb")
      musiqueInfos["cover"] = base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
      lMusiques.append(musiqueInfos)
    if len(lMusiques) == 0:
      return "";
    return self.returnList(lMusiques)
  def printAllAlbum(self, current=None):
    # Affiche tous les albums
    lAlbum = []
    for album in musiques.find({}, {"_id": 0, "artiste": 1, "album": 1, "path_image": 1}).distinct("album"):
      print("----> album: " + str(album))
      albumInfos = {}
      albumInfos["album"] = ""
      albumInfos["artiste"] = self.findAlbumArtist(album)
      albumInfos["titre"] = album
      albumInfos["cover"] = self.findAlbumCover(album)
      lAlbum.append(albumInfos)
    print("-----> res: " + str(lAlbum))
    if len(lAlbum) == 0:
      return "";
    return self.returnList(lAlbum)
  def printAlbum(self, name, current=None):
    # Affiche l'album...
    lAlbum = []
    for album in musiques.find({"album": name}, {"_id": 0, "artiste": 1, "album": 1, "path_image": 1}).distinct("album"):
      print("----> album: " + str(album))
      albumInfos = {}
      albumInfos["album"] = ""
      albumInfos["artiste"] = self.findAlbumArtist(album)
      albumInfos["titre"] = album
      albumInfos["cover"] = self.findAlbumCover(album)
      lAlbum.append(albumInfos)
    if len(lAlbum) == 0:
      return "";
    return self.returnList(lAlbum)
  def printAlbumByArtiste(self, artiste, current=None):
    # Affiche les albums de l'artiste...
    lAlbum = []
    for album in musiques.find({"artiste": artiste}, {"_id": 0, "artiste": 1, "album": 1, "path_image": 1}).distinct("album"):
      print("----> album: " + str(album))
      albumInfos = {}
      albumInfos["album"] = ""
      albumInfos["artiste"] = self.findAlbumArtist(album)
      albumInfos["titre"] = album
      albumInfos["cover"] = self.findAlbumCover(album)
      lAlbum.append(albumInfos)
    if len(lAlbum) == 0:
      return "";
    return self.returnList(lAlbum)
  def findAlbumArtist(self, album):
    # récupère l'artiste d'un album
    artiste = musiques.find({"album": album}, {"_id": 0, "artiste": 1, "album": 1}).distinct("artiste")
    return str(artiste[0])
  def findAlbumCover(self, album):
    # récupère l'image d'un album et la rends sérialisable
    res = musiques.find({"album": album}, {"_id": 0, "artiste": 1, "album": 1, "path_image": 1}).distinct("path_image")
    if(res[0] == ""):
      res[0] = default_cover
    cover = open(res[0], "rb")
    return base64.b64encode(cover.read()).decode('utf8').replace("'", '"')
  def returnList(self, list):
    #Retourne un json contenant la liste de albums/musiques
    res = json.dumps({
      "info": list
    }, separators=(',', ':'))
    return res
  def stop(self, current=None):
    # Arrèt du stream
    if player.is_playing():
      player.stop()
  def pause(self, current=None):
    # Mise en pause du stream
    if player.is_playing():
      player.pause()
  def reprendre(self, current=None):
    # Reprendre le stream
    if not player.is_playing():
      player.play()
  def next(self, current=None):
    # musique suivante
    if player.is_playing():
      player.next()
  def previous(self, current=None):
    # musique précédente
    if player.is_playing():
      player.previous()

with Ice.initialize(sys.argv) as communicator:
  adapter = communicator.createObjectAdapterWithEndpoints("MonServeurAdapter", "default -p 10000")
  object = PlayerI()
  adapter.add(object, communicator.stringToIdentity("MonServeur"))
  adapter.activate()
  communicator.waitForShutdown()