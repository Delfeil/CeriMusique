package correspondance;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface GestionCorrespondance
{
    public String getCorrespondance(String text) throws CorrespondanceNotFoundException, TypeNotFoundException;
    public void setCorrespondance(String type, String text) throws TypeNotFoundException;
    public void removeCorrespondance(String text) throws CorrespondanceNotFoundException;
    public List<Pattern> getAllCorrespondances(String type) throws TypeNotFoundException;
}