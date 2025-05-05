package poo.cal;

import javax.swing.JTextPane;


public class Tunnels {
    /*
     * Tunnels
     * 
     * This is a wrapper class for the 4 tunnels, in order to make it easier to manage them.
     * 
     * It is very similar to the RiskZones class
     */
    private Tunnel [] tunnels = new Tunnel[4];
    public Tunnels (JTextPane crossingTextPanes[], JTextPane waitingInTextPanes[], JTextPane waitingGroupTextPanes[], ConnHub hub) {
        for (int i = 0; i < 4; i++) {
            tunnels[i] = new Tunnel(i+1, crossingTextPanes[i], waitingInTextPanes[i], waitingGroupTextPanes[i], hub);
        }
    }

    public void enterTunnel(Human h, int which, boolean isEnteringRefuge) {
        tunnels[which-1].enterTunnel(h, isEnteringRefuge);
    }
}
