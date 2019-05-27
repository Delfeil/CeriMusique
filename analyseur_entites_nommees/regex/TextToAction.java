package regex;

import java.io.*;
import java.util.regex.*;
// import net.sf.json.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;

import javax.naming.*;
import correspondance.GestionCorrespondance;
import correspondance.TypeNotFoundException;
import correspondance.CorrespondanceNotFoundException;

/**
 * analyseur d'entités nommées
 * Permet à partir d'un texte, en retrouvant des mots clés, de déterminer les actions à effectuer
 * à l'aide d'expression régulières
 * Géré en Rest, combiné avec des EJB et des entityBeans et déployé sur un serveur GlassFish
 * Les EJB permettent, en s'interfaçant avec une base de donnée postgreSql,
 *   de retrouver des correspondances entre une partie du texte et un type de donnée (artiste, musique ou album)
 */
@Path("/")
public class TextToAction {

    private static InitialContext initCont;
    private static GestionCorrespondance gc; 

    /**
     * 
     * @param text Le texte à analysé (issu du module de "speech Recognition")
     * @return Chaine de character formaté comme un JSON, contenant les actions et paramètres trouvés
     */
    @Path("/action")
    @GET
    @Produces("text/plain" + ";charset=utf-8")
    public String actionFromText(@QueryParam("text") @DefaultValue("") String text) {
        System.out.println("----> Text: " + text);
        if(initCont == null) {
            try{
                initCont = new InitialContext();
                gc = (GestionCorrespondance) initCont.lookup("correspondance.GestionCorrespondance");
            } catch(Exception e) {
                return "{"+
                    "\"action\": \"error\","+
                    "\"param\": {"+
                    "}"+
                "}";
            }
        }
        return Action(text);
    }

    /**
     * Enregistrement dans la base de donnée d'une nouvelle correspondance
     * @param text Le texte à faire correspondre
     * @param type Le type de correspondance (artiste, musique ou album)
     * @return Détail de l'état
     */
    @Path("/validCorrespondance")
    @GET
    @Produces("text/plain")
    public String validCorrespondance(@QueryParam("text") @DefaultValue("") String text, @QueryParam("type") @DefaultValue("") String type) {
        System.out.println("text: " + text + " , type: " + type);
        if(initCont == null) {
            try{
                initCont = new InitialContext();
                gc = (GestionCorrespondance) initCont.lookup("correspondance.GestionCorrespondance");
            } catch(Exception e) {
                return "error";
            }
        }
        if(text.equals("") || type.equals("")) {
            return "error";
        }
        try {
            gc.getCorrespondance(text);
            //Si la correspondance existe déjà, on ne la recrée pas
                //Facilitée pour éviter les cas ou un album et un artiste ont le même nom
        } catch(CorrespondanceNotFoundException cnfe) {
            //Si la correspondance n'existe pas, on la crée
            try {
                gc.setCorrespondance(type, text);
            } catch(TypeNotFoundException tnfe) {
                return "ce type n'existe pas";
            }
        } catch(TypeNotFoundException tnfe) {
            return "ce type n'existe pas";
        }
        return "set";
    }
        //Ensembles des expressions régulières utilisées par l'analyseur d'entités nommées
    private static String regPlay = "(.*)?(jouer?|l\'ancer?|play)";
    private static String regAffiche = "(.*)?(afficher?|montrer?)";
    private static String regDe = "(.*)?(de|du)";
    private static String regMusic = "(.*)?(musiques?|morceaus?)";
    private static String regAlbum = "(.*)?(albums?|cd|CD)";
    private static String regArtiste = "(.*)?(artiste|chant(eur|euse)|interpr(e|è)te|groupe)";
    private static String regSuiv = "(.*)?(suivante?|next)";
    private static String regPreced = "(.*)?(pr(e|é)c(e|é)dente?|arri(e|è)re)";
    private static String regSon = "(.*)?(son|volume)";
    private static String regAugmenter = "(.*)?(augmenter?|monter?)" + regSon + "?";
    private static String regDiminuer = "(.*)?(diminuer?|r(é|e)duire)" + regSon + "?";
    private static String regcouperSon = "(.*)?((C|c)ouper?|arr(é|e)ter?)" + regSon;
    private static String regRemettreSon = "(.*)?(remettre)" + regSon;
    private static String regStop = "(.*)?(stop|arr(é|e)ter?)" + regMusic + "?";
    private static String regPause = "(.*)?(pause)" + regMusic + "?";
    private static String regReprendre = "(.*)?(reprendre|play)" + regMusic + "?";
    private static String regRecommencer = "(.*)?(recommencer?|red(é|e)marrer?)" + regMusic + "?";

    /**
     * 
     * @param text Le texte à analyser
     * @return Chaine de character formaté comme un JSON, contenant les actions et paramètres trouvés
     */
    public String Action(String text) {
        text = text.toLowerCase();

        if(Pattern.compile(regPlay).matcher(text).find()) {
                // L'ensembles des commandes demandant de "jouer"
            return playPattern(text);
        } else if(Pattern.compile(regAffiche).matcher(text).find()) {
                // L'ensembles des commandes demandant d'afficher
            return affichePattern(text);
        } else if(Pattern.compile(regSuiv).matcher(text).find()) {
            return "{"+
                "\"action\": \"next_musique\","+
                "\"param\": {"+
                "}"+
            "}";
            // Musique suivante
        } else if(Pattern.compile(regPreced).matcher(text).find()) {
            return "{"+
                "\"action\": \"previous_musique\","+
                "\"param\": {"+
                "}"+
            "}";
            // Musique précédente
        } else if(Pattern.compile(regAugmenter).matcher(text).find()) {
            return "{"+
                "\"action\": \"up_sound\","+
                "\"param\": {"+
                "}"+
            "}";
            // Augmenter son
        } else if(Pattern.compile(regDiminuer).matcher(text).find()) {
            return "{"+
                "\"action\": \"down_sound\","+
                "\"param\": {"+
                "}"+
            "}";
            // Diminuer son
        } else if(Pattern.compile(regcouperSon).matcher(text).find()) {
            return "{"+
                "\"action\": \"off_sound\","+
                "\"param\": {"+
                "}"+
            "}";
            // Couper son
        } else if(Pattern.compile(regRemettreSon).matcher(text).find()) {
            return "{"+
                "\"action\": \"on_sound\","+
                "\"param\": {"+
                "}"+
            "}";
            // Remettre son
        } else if(Pattern.compile(regStop).matcher(text).find()) {
            return "{"+
                "\"action\": \"stop\","+
                "\"param\": {"+
                "}"+
            "}";
            // Stop
        } else if(Pattern.compile(regPause).matcher(text).find()) {
            return "{"+
                "\"action\": \"pause\","+
                "\"param\": {"+
                "}"+
            "}";
            // Pause
        } else if(Pattern.compile(regReprendre).matcher(text).find()) {
            return "{"+
                "\"action\": \"resume\","+
                "\"param\": {"+
                "}"+
            "}";
            // Reprendre
        } else if(Pattern.compile(regRecommencer).matcher(text).find()) {
            return "{"+
                "\"action\": \"again\","+
                "\"param\": {"+
                "}"+
            "}";
            // Recommencer
        }
        return "{"+
            "\"action\": \"not_found\","+
            "\"param\": {"+
            "}"+
        "}";
        // Not Found
    }

    /**
     * Fonction analysant les demande concernant la lecture (play, joue,...)
     * @param text Le texte à analyser
     * @return String formatée en JSON contenant les infos récupérées lors de l'analyse
     */
    public String playPattern(String text) {
        System.out.println("Found Play");
        if(Pattern.compile(regPlay + regMusic + regArtiste).matcher(text).find()) {
            //Joue les Musiques de l'Artiste ...
            String[] split = text.split(regArtiste);
            String nom = split[1];
            nom = nom.substring(1);
            String ret = "{"+
                "\"action\": \"play_musique\","+
                "\"param\": {"+
                    "\"artiste\": \"" + nom + "\""+
                "}"+
            "}";
            System.out.println("Joue musique artiste: " + ret);
            return ret;


        } else if(Pattern.compile(regPlay + regMusic + regAlbum).matcher(text).find()) {
            //Joue les musiques de l'Album...
            String[] split = text.split(regAlbum);
            for(int i=0; i<split.length; i++) {
                System.out.println(split[i]);
            }
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"play_musique\","+
                "\"param\": {"+
                    "\"album\": \"" + nom + "\""+
                "}"+
            "}";


        } else if(Pattern.compile(regPlay + regMusic + regDe).matcher(text).find()) {
            //Appel aux EJBs pour identifier si la suite correspond à un Artiste, Musique ou Album, ou si la correspondance n'as pas été faite
            //Joue la musique de + texte: déterminer si texte correspond au nom d'un artiste ou d'un album
            String[] split = text.split(regPlay + regMusic + regDe);
            if(split.length <= 1) {
                split = text.split(regPlay);
            }
            String nom = split[1];
            nom = nom.substring(1);
            System.out.println("Found Play: nom: " + nom);
            String type;

            try {
                type = gc.getCorrespondance(nom);
            } catch(CorrespondanceNotFoundException cnfe) {
                type = "";
            } catch(TypeNotFoundException thne) {
                type = "";
            }
            if(type.equals("artiste")) {
                return "{"+
                    "\"action\": \"play_musique\","+
                    "\"param\": {"+
                        "\"artiste\": \"" + nom + "\""+
                    "}"+
                "}";
            } else if(type.equals("album")) {
                return "{"+
                    "\"action\": \"play_musique\","+
                    "\"param\": {"+
                        "\"album\": \"" + nom + "\""+
                    "}"+
                "}";
            }


        } else if(Pattern.compile(regPlay + regMusic).matcher(text).find()) {
            // Joue la musique...
            String[] split = text.split(regMusic);
            for(int i=0; i<split.length; i++) {
                System.out.println(split[i]);
            }
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"play_musique\","+
                "\"param\": {"+
                    "\"musique\": \"" + nom + "\""+
                "}"+
            "}";
            

        } else if(Pattern.compile(regPlay + regAlbum).matcher(text).find()) {
            //Joue l'album...
            String[] split = text.split(regAlbum);
            for(int i=0; i<split.length; i++) {
                System.out.println(split[i]);
            }
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"play_musique\","+
                "\"param\": {"+
                    "\"album\": \"" + nom + "\""+
                "}"+
            "}";


        } else {
            //Appel aux EJBs pour identifier si la suite correspond à un Artiste, Musique ou Album, ou si la correspondance n'as pas été faite
            //Joue + text (déterminer si text correspond à (musique/du artiste/album))
            String[] split = text.split(regPlay + regDe);
            if(split.length <= 1) {
                split = text.split(regPlay);
            }
            System.out.println("split: " + split.length + " | " + split[0]);
            String nom = split[1];
            nom = nom.substring(1);
            System.out.println("Found Play: nom: " + nom);
            String type;

            try {
                type = gc.getCorrespondance(nom);
            } catch(CorrespondanceNotFoundException cnfe) {
                type = "";
            } catch(TypeNotFoundException thne) {
                type = "";
            }
            if(type.equals("musique")) {
                return "{"+
                    "\"action\": \"play_musique\","+
                    "\"param\": {"+
                        "\"musique\": \"" + nom + "\""+
                    "}"+
                "}";
            } else if(type.equals("artiste")) {
                return "{"+
                    "\"action\": \"play_musique\","+
                    "\"param\": {"+
                        "\"artiste\": \"" + nom + "\""+
                    "}"+
                "}";
            } else if(type.equals("album")) {
                return "{"+
                    "\"action\": \"play_musique\","+
                    "\"param\": {"+
                        "\"album\": \"" + nom + "\""+
                    "}"+
                "}";
            }


        }
        // Si aucun patter n'a été reconnu, ou que le texte après joue ne correspond pas à un artiste, une musique ou un album: je n'ai pas compris
        return "{"+
            "\"action\": \"not_found\","+
            "\"param\": {"+
            "}"+
        "}";
    }

    /**
     * Fonction analysant les demande concernant l'affichage
     * @param text Le texte à analyser
     * @return String formatée en JSON contenant les infos récupérées lors de l'analyse
     */
    public String affichePattern(String text) {
        System.out.println("Found Affiche");
        if(Pattern.compile(regAffiche + regMusic + regAlbum).matcher(text).find()) {
            //Affiche Musiques de l'album...
            String[] split = text.split(regAlbum);
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"affiche_musique\","+
                "\"param\": {"+
                    "\"album\": \"" + nom + "\""+
                "}"+
            "}";


        } else if(Pattern.compile(regAffiche + regMusic + regArtiste).matcher(text).find()) {
            // Affiche Musiques de l'artiste
            String[] split = text.split(regArtiste);
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"affiche_musique\","+
                "\"param\": {"+
                    "\"artiste\": \"" + nom + "\""+
                "}"+
            "}";
        } else if (Pattern.compile(regAffiche + regMusic + regDe).matcher(text).find()) {
            //Appel aux EJB pour identifier le type (Artiste ou album)
            //Affiche les musique de + texte: déterminer si texte correspond à un artiste ou un album
            String[] split = text.split(regAffiche + regMusic + regDe);
            String nom = split[1];
            nom = nom.substring(1);
            String type;
            try {
                type = gc.getCorrespondance(nom);
            } catch(CorrespondanceNotFoundException cnfe) {
                type = "";
            } catch(TypeNotFoundException thne) {
                type = "";
            }
            if(type.equals("artiste")) {
                return "{"+
                    "\"action\": \"affiche_musique\","+
                    "\"param\": {"+
                        "\"artiste\": \"" + nom + "\""+
                    "}"+
                "}";
            } else if(type.equals("album")) {
                return "{"+
                    "\"action\": \"affiche_musique\","+
                    "\"param\": {"+
                        "\"album\": \"" + nom + "\""+
                    "}"+
                "}";
            }


        } else if (Pattern.compile(regAffiche + regAlbum + regArtiste).matcher(text).find()) {
            // Affiche Albums de l'artiste...
            String[] split = text.split(regAlbum);
            String nom = split[1];
            nom = nom.substring(1);
            return "{"+
                "\"action\": \"affiche_album\","+
                "\"param\": {"+
                    "\"artiste\": \"" + nom + "\""+
                "}"+
            "}";


        } else if (Pattern.compile(regAffiche + regAlbum + regDe).matcher(text).find()) {
            //Appel aux EJB pour identifier le type (Artiste)
            //Affiche albums de + texte: déterminer si texte corespond au nom d'un artiste
            String[] split = text.split(regAffiche + regAlbum + regDe);
            String nom = split[1];
            nom = nom.substring(1);
            String type;
            try {
                type = gc.getCorrespondance(nom);
            } catch(CorrespondanceNotFoundException cnfe) {
                type = "";
            } catch(TypeNotFoundException thne) {
                type = "";
            }
            if(type.equals("artiste")) {
                return "{"+
                    "\"action\": \"affiche_album\","+
                    "\"param\": {"+
                        "\"artiste\": \"" + nom + "\""+
                    "}"+
                "}";
            }


        } else if(Pattern.compile(regAffiche + regMusic).matcher(text).find()) {
            String[] split = text.split(regMusic);
            if(split.length <=1) {
                // Affiches toutes les musiques.
                return "{"+
                "\"action\": \"affiche_musique\","+
                "\"param\": {"+
                "}"+
            "}";
            }
            for(int i=0; i<split.length; i++) {
                System.out.println(split[i]);
            }

            String nom = split[1];
            nom = nom.substring(1);
            //Affice la musique...
            return "{"+
                "\"action\": \"affiche_musique\","+
                "\"param\": {"+
                    "\"musique\": \"" + nom + "\""+
                "}"+
            "}";


        } else if(Pattern.compile(regAffiche + regAlbum).matcher(text).find()) {
            String[] split = text.split(regAlbum);
            if(split.length <=1) {
                //Affiche les albums.
                return "{"+
                "\"action\": \"affiche_album\","+
                "\"param\": {"+
                "}"+
            "}";
            }
            for(int i=0; i<split.length; i++) {
                System.out.println(split[i]);
            }

            String nom = split[1];
            nom = nom.substring(1);
            //Affiche l'album...
            return "{"+
                "\"action\": \"affiche_album\","+
                "\"param\": {"+
                    "\"album\": \"" + nom + "\""+
                "}"+
            "}";


        }
        return "{"+
            "\"action\": \"not_found\","+
            "\"param\": {"+
            "}"+
        "}";
    }
}