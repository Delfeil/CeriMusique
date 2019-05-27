package correspondance;

public class CorrespondanceNotFoundException extends Exception {
    public CorrespondanceNotFoundException() {
        super("Pas de correspondance");
    }
}