package poo.cal;

import java.util.Random;

public abstract class Entity extends Thread {
    protected String id;
    protected GlobalLock gl;
    protected Random random = new Random();
    protected ApocalypseLogger logger;
    private int delta = 100;
    

    public Entity(String id, GlobalLock gl, ApocalypseLogger logger) {
        this.id = id;
        this.gl = gl;
        this.logger = logger;
    }

    abstract public void run();

    public String getEntityId() {
        return id;
    }
    public String getCleanId(){
        return id;
    }
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
