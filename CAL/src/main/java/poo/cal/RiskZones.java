package poo.cal;

import javax.swing.JTextPane;

public class RiskZones {
    private RiskZone riskZones[] = new RiskZone[4];

    public RiskZones(JTextPane humanTextPanes[], JTextPane zombieTextPanes[], ConnHub hub) {
        for (int i = 1; i <= 4; i++) {
            riskZones[i] = new RiskZone(i, humanTextPanes[i], zombieTextPanes[i], hub);
        }
    }

    public void enter(Entity e, int riskZoneIndex) {
        riskZones[riskZoneIndex - 1].enter(e);
    }
    public RiskZone getRiskZone(int id){
        return riskZones[id-1];
    }

    public void leave(Entity e, int riskZoneIndex) {
        riskZones[riskZoneIndex - 1].leave(e);
    }

    public void notifyAttack(Human h, Zombie z, int riskZoneIndex, boolean isAttacking) {
        riskZones[riskZoneIndex - 1].notifyAttack(h, z, isAttacking);
    }

    public Human getRandomHuman(int riskZoneIndex) {
        return riskZones[riskZoneIndex - 1].getRandomHuman();
    }

    
    
}