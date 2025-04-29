package poo.cal;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class RiskZones {
    private ArrayList<GraphicArrayList<Entity>> zones;
    
    public RiskZones(JTextArea zone1, JTextArea zone2, JTextArea zone3, JTextArea zone4) {
        zones = new ArrayList<>(4);
        // Añadimos null en la posición 0 para que los índices coincidan con los números de zona 1-4
        zones.add(null);
        zones.add(new GraphicArrayList<>(zone1));
        zones.add(new GraphicArrayList<>(zone2));
        zones.add(new GraphicArrayList<>(zone3));
        zones.add(new GraphicArrayList<>(zone4));
    }

    public GraphicArrayList<Entity> getZone(int zoneNumber) {
        // Validar que el número de zona esté en el rango correcto
        if (zoneNumber < 1 || zoneNumber > 4) {
            return null;
        }
        return zones.get(zoneNumber);
    }

    public synchronized void enter(Entity e, int which) {
        if (which >= 1 && which <= 4) {
            GraphicArrayList<Entity> zone = zones.get(which);
            zone.add(e);
        }
    }

    public void leave(Entity e, int which) {
        if (which >= 1 && which <= 4) {
            GraphicArrayList<Entity> zone = zones.get(which);
            zone.remove(e);
        }
    }
}