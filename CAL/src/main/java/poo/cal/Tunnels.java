package poo.cal;

import javax.swing.JTextPane;


public class Tunnels {
    private Tunnel [] tunnels = new Tunnel[4];
    public Tunnels (JTextPane crossingTextPanes[], JTextPane waitingInTextPanes[], JTextPane waitingGroupTextPanes[], ConnHub hub) {
        for (int i = 0; i < 4; i++) {
            tunnels[i] = new Tunnel(i+1, crossingTextPanes[i], waitingInTextPanes[i], waitingGroupTextPanes[i], hub);
        }
    }

    public void enterTunnel(Human h, int which, boolean isEnteringRefuge) {
        tunnels[which-1].enterTunnel(h, isEnteringRefuge);
    }
    
    public int[] getHumansInTunnels(){
        int [] humansInTunnels = new int[4];
        for (int i = 0; i < 4; i++) {
            humansInTunnels[i] = tunnels[i].getHumansInTunnel();
        }
        return humansInTunnels;
    }
}
