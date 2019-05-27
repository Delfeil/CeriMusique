package correspondance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

@NamedQueries({
    @NamedQuery(name="type_pattern",
    query="SELECT pattern FROM Pattern pattern WHERE pattern.id_correspondance=:id"),
    @NamedQuery(name="getPatternWithText",
    query="SELECT pattern FROM Pattern pattern WHERE pattern.text=:text"),
    @NamedQuery(name="getPatternId",
    query="SELECT pattern.id FROM Pattern pattern WHERE pattern.text=:text"),
})
@Entity
@Table(name = "pattern")
public class Pattern implements Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String text;

    private int id_correspondance;

    public Pattern(){}

    public Pattern(String text, int id_correspondance) {
        this.text = text;
        this.id_correspondance = id_correspondance;   
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the id_correspondance
     */
    public int getId_correspondance() {
        return id_correspondance;
    }
}