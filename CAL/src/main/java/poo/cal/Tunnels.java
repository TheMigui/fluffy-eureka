package poo.cal;

import javax.swing.JTextPane;


public class Tunnels {
    private Tunnel [] tunnels = new Tunnel[4];
    public Tunnels (JTextPane crossingTextPanes[], JTextPane waitingInTextPanes[], JTextPane waitingGroupTextPanes[]) {
        for (int i = 0; i < 4; i++) {
            tunnels[i] = new Tunnel(crossingTextPanes[i], waitingInTextPanes[i], waitingGroupTextPanes[i]);
        }
    }

    public void enterTunnel(Human h, int which, boolean isEnteringRefuge) {
        tunnels[which-1].enterTunnel(h, isEnteringRefuge);
    }
    
}
