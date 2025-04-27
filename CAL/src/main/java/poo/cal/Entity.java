package poo.cal;
import java.util.Random;

public abstract class Entity implements Runnable{
    protected String id;
    protected GlobalLock gl;
    protected Random random = new Random();
    protected ApocalypseLogger logger;

    public Entity(String id, GlobalLock gl, ApocalypseLogger logger) {
        this.id = id;
        this.gl = gl;
        this.logger = logger;
    }

    abstract public void run();

    public String getId() {
        return id;
    }

    protected void sleep(int ms){
        try {
            Thread.sleep(ms);
            gl.check();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return this.id;
    }

}
