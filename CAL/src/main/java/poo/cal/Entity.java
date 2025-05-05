package poo.cal;

import java.util.Random;

public abstract class Entity extends Thread {
    /*
     * Entity
     * 
     * This class represents an entity in the simulation, either a human or a zombie.
     * 
     * It contains all common attributes and methods for both types of entities.
     */
    protected String id; // The id of the entity ("H0001", "Z1234"...)
    protected GlobalLock gl; // Used for pausing when the simulation is paused
    protected Random random = new Random();
    protected ApocalypseLogger logger;
    protected int delta = 100;
    protected int riskZoneNo; // This represents the risk zone the entity is in / wants to enter / is leaving from.
    
    /**
     * Entity constructor.
     * 
     * It initializes the id, GlobalLock and ApocalypseLogger instances.
     * @param id The id of the entity ("H0001", "Z1234"...)
     * @param gl GlobalLock instance used for checking if the simulation is paused
     * @param logger ApocalypseLogger instance used for logging events
     */
    public Entity(String id, GlobalLock gl, ApocalypseLogger logger) {
        this.id = id;
        this.gl = gl;
        this.logger = logger;
    }

    @Override
    /**
     * run
     * 
     * Called when the thread is started.
     * 
     * Both Human and Zombie classes will override this method to implement their own behavior.
     * However, they will call the super method to set the thread name to the entity id.
     */
    public void run(){
        Thread.currentThread().setName(this.getEntityId()); // Set the thread name to its id. This is for debugging purposes
        gl.check(); // Before starting, check if the simulation is paused
    }

    public String getEntityId() {
        return id;
    }
    public String getCleanId(){ // Sometimes extra information is added to the id (p.ex: "H0001*" means that
                                // the human has been marked by a zombie)
                                // getCleanId returns the id without this extra information.
        return id;
    }
    public int getriskZoneNo() {
        return riskZoneNo; 
    }

    /**
     * sleep
     * 
     * This is a custom sleep method.
     * 
     * Instead of sleeping for a fixed amount of time, it sleeps in intervals of delta milliseconds.
     * 
     * In between sleeps, it checks if the simulation is paused, and gets locked if it is.
     * 
     * Therefore, if the simulation is paused, the thread will (approximately) remember 
     * how much more time it has to sleep when the simulation is resumed.
     * @param ms The amount of time to sleep in milliseconds.
     */
    protected void sleep(int ms){
        int remainingMs = ms;
        try {
            while(remainingMs > 0){
                Thread.sleep(remainingMs < delta ? remainingMs : delta);
                gl.check();
                remainingMs -= delta;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public String toString(){
        return this.getEntityId();
    }

}
