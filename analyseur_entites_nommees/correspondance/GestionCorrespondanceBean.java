package correspondance;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.List;


import javax.ejb.Stateless;
import javax.persistence.*;

@Stateless
public class GestionCorrespondanceBean implements GestionCorrespondance
{

    @PersistenceContext(unitName="unit-correspondance")
    protected EntityManager em;

    public String getCorrespondance(String text) throws CorrespondanceNotFoundException, TypeNotFoundException {
        try {
            Pattern p = em.createNamedQuery("getPatternWithText", Pattern.class).setParameter("text", text).getSingleResult();
            Correspondance c = em.find(Correspondance.class, p.getId_correspondance());
            if(c != null) {
                return c.getCorrespondance();
            } else {
                throw new TypeNotFoundException();
            }
        } catch(Exception e) {
            throw new CorrespondanceNotFoundException();
        }
    }

    public void setCorrespondance(String type, String text) throws TypeNotFoundException {
        System.out.println("ajout correspondance: " + text + " = " + type);
        try {
            Integer id = em.createNamedQuery("id_correspondance", Integer.class).setParameter("type", type).getSingleResult();
            System.out.println("ajout correspondance2: " + text + " = " + type);
            Pattern p = new Pattern(text, id.intValue());
            System.out.println("ajout correspondance3: " + text + " = " + type);
            em.persist(p);
            System.out.println("ajout correspondance4: " + text + " = " + type);
        } catch(Exception e) {
            e.printStackTrace();
            throw new TypeNotFoundException();
        }
    }

    public void removeCorrespondance(String text) throws CorrespondanceNotFoundException {
        try {
            int id = em.createNamedQuery("getPatternId", int.class).setParameter("text", text).getSingleResult();
            Pattern p = em.find(Pattern.class, id);
            em.remove(p);
        } catch(Exception e) {
            e.printStackTrace();
            throw new CorrespondanceNotFoundException();
        }
    }

    public List<Pattern> getAllCorrespondances(String type) throws TypeNotFoundException {
        List<Pattern> patterns = null;
        try {
            Integer id = em.createNamedQuery("id_correspondance", Integer.class).setParameter("type", type).getSingleResult();
            patterns = em.createNamedQuery("type_pattern", Pattern.class).setParameter("id", id.intValue()).getResultList();
        } catch(Exception e) {
            throw new TypeNotFoundException();
        }
        return patterns;
    }

}