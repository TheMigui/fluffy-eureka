package poo.cal;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JTextArea;

public class RiskZones {
    private ConcurrentHashMap<Integer,GraphicArrayList<Entity>> zones;
    public RiskZones (JTextArea zone1,JTextArea zone2,JTextArea zone3,JTextArea zone4){
        zones = new ConcurrentHashMap<>();
        zones.put(1, new GraphicArrayList<>(zone1));
        zones.put(2, new GraphicArrayList<>(zone2));
        zones.put(3, new GraphicArrayList<>(zone3));
        zones.put(4, new GraphicArrayList<>(zone4));

    }

    public GraphicArrayList<Entity> getZone(int zoneNumber){
        return zones.get(zoneNumber);
    }

    public synchronized void enter(Entity e, int which){
        GraphicArrayList<Entity> zone = zones.get(which);
        zone.add(e);
    }
    public void leave(Entity e, int which){
        GraphicArrayList<Entity> zone = zones.get(which);
        zone.remove(e);
    }
    public void notifyAttack(Entity e, Zombie z, int which, boolean add){

    }
}
