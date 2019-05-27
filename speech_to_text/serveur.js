const express = require('express'); //Express.js
const session = require('express-session');//Gestion des sessions

const multer = require('multer');
var storage = multer.memoryStorage();
var upload = multer({ storage: storage });

//My First Project-2dcb50461b9a.json
// 2dcb50461b9a9116acdfbe1a21f411cf3b022440
// Imports the Google Cloud client library
const speech = require('@google-cloud/speech');
const fs = require('fs');

// création du client pour l'api de google
const client = new speech.SpeechClient();
//Format et caractéristique que le fichier audio transis doit respecter
const config = {
  encoding: 'AMR_WB',
  sampleRateHertz: 16000,
  audioChannelCount: 1,
  languageCode: 'fr-FR',      //Configuration pour la transcription
  model: 'command_and_search'
};

/**
 * Serveur NodeJs + ExpressJs, utilisant l'api de spech recogniton de Google,
 * pour retranscrire un enregistrement audio en un texte
 */
const app = express(); // expressJS


var server=app.listen(3101, function() {
  console.log('listening on 3101');
});

  //Route à laquelle est passée l'enregistrement
  // Le fichier audio, doit être dans un format précis: .wma
app.post('/transcribe', upload.single('file'), function(req, res) {
  console.log("req: ", req.file, 'conf: ', config);

      //Récupération des données du fichier audio
      var audio = {
        content: req.file.buffer
      };

      var request = {
        audio: audio,
        config: config
      };

      // transcription du message
      client.recognize(request)
        .then(function(data) {
          const response = data[0];
          const transcription = response.results
            .map(result => result.alternatives[0].transcript)
            .join('\n');
          console.log("transcription: ", transcription)
          return res.status(200).json({
            transcription: transcription
          });
        })
        .catch(function(err) {
          console.log("Erreur 400: Erreur de transcription", err);
          return res.status(400).json({
            message: "Erreur de transcription"
          });
        });

    // }
  // });
});