package poo.cal;

import javax.swing.JTextPane;

public class RiskZones {

    /*
     * RiskZones
     * 
     * This is a wrapper class for the 4 risk zones, in order to make it easier to manage them.
     * 
     * All methods take a Risk Zone index from 1 to 4 as a parameter, and call the corresponding method in the RiskZone class.
     * 
     * Indexes are 0-based, so we need to subtract 1 from the index passed as a parameter.
     */
    private RiskZone riskZones[] = new RiskZone[4];

    public RiskZones(JTextPane humanTextPanes[], JTextPane zombieTextPanes[], ConnHub hub) {
        for (int i = 0; i < 4; i++) {
            riskZones[i] = new RiskZone(i+1, humanTextPanes[i], zombieTextPanes[i], hub);
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

    public Human getRandomHuman(int riskZoneIndex, Zombie z) {
        return riskZones[riskZoneIndex - 1].getRandomHuman(z);
    }

    
    
}