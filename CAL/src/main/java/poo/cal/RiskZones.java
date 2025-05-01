package poo.cal;

import javax.swing.JTextPane;

public class RiskZones {
    private RiskZone riskZones[] = new RiskZone[4];

    public RiskZones(JTextPane humanTextPanes[], JTextPane zombieTextPanes[]) {
        for (int i = 0; i < riskZones.length; i++) {
            riskZones[i] = new RiskZone(humanTextPanes[i], zombieTextPanes[i]);
        }
    }

    public void enter(Entity e, int riskZoneIndex) {
        riskZones[riskZoneIndex - 1].enter(e);
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