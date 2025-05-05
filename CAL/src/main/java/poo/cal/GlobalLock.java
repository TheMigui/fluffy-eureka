package poo.cal;

import javax.swing.JButton;

public class GlobalLock {
    /*
     * GlobalLock (Global Lock)
     * 
     * GlobalLock is a class that handles the pause/resume functionality of the simulation.
     * 
     * When the simulation is paused, all threads that call the check() method will be blocked until the simulation is resumed.
     * 
     * Humans and Zombies will call the check() method at startup and whenever they are sleeping.
     * 
     * They only check the GlobalLock when sleeping because actions in between are imperceptibly fast.
     * 
     * ApocalypseLogger also checks the GlobalLock before logging, so that no logs are written when the simulation is paused.
     * (an exception is made for pause/resume events, which are logged regardless of the GlobalLock state)
     * 
     */
    private boolean isOpen = true;
    private ApocalypseLogger logger; // This reference is used to log pause/resume events.
    private ConnHub hub; // This reference is used let the clients know when the simulation status changes.
    private JButton pauseButton; // This reference is used to update the text of the pause/resume button in the GUI.

    public GlobalLock(JButton pauseButton){
        this.pauseButton = pauseButton;
    }

    public void setApocalypseLogger(ApocalypseLogger logger) {
        this.logger = logger; // It is set here because the GlobalLock is created before the ApocalypseLogger.
    }

    public void setConnHub(ConnHub hub) {
        this.hub = hub; // Same as above
    }

    public synchronized void check(){
        while(!isOpen){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isOpen() {
        return isOpen;
    }

    /**
     * setOpen
     * 
     * This method is used to set the state of the GlobalLock (open/closed).
     * 
     * It also logs who opened/closed the simulation, changes the server's button text and notifies the ConnHub 
     * to update the clients.
     * 
     * It is a synchronized method to ensure that only one thread can change the state of the GlobalLock at a time
     * @param isOpen true if the simulation is open, false if it is closed.
     * @param responsible The name of who opened/closed the simulation.
     */
    public synchronized void setOpen(boolean isOpen, String responsible) {
        logger.log("GL has been " + (isOpen ? "opened" : "closed") + " " + responsible, true);
        this.isOpen = isOpen;
        if(isOpen){
            this.notifyAll();
        }
        hub.updateSimulationStatus(isOpen);
        pauseButton.setText(isOpen ? "Pause" : "Resume");
    }
    public void open(String responsible) {
        this.setOpen(true, responsible);
    }
    public void close(String responsible) {
        this.setOpen(false, responsible);
    }
    
}
