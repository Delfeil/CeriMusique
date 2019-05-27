package correspondance;

public class TypeNotFoundException extends Exception {
    public TypeNotFoundException() {
        super("Ce type de correspondance n'existe pas");
    }
}