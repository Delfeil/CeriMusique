package correspondance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

@NamedQueries({
    @NamedQuery(name="id_correspondance",
    query="SELECT correspondance.id FROM Correspondance correspondance WHERE correspondance.correspondance=:type")
})
@Entity
@Table(name = "correspondance")
public class Correspondance implements Serializable {
    @Id
    private Integer id;

    private String correspondance;

    public Correspondance(){}

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the correspondance
     */
    public String getCorrespondance() {
        return correspondance;
    }
}  