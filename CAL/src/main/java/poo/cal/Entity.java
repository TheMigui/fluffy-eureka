package poo.cal;
import java.util.Random;

public abstract class Entity implements Runnable{
    protected int id;
    protected GlobalLock gl;
    protected Random random = new Random();
    protected ApocalypseLogger logger;

    public Entity(int id, GlobalLock gl, ApocalypseLogger logger) {
        this.id = id;
        this.gl = gl;
        this.logger = logger;
    }

    abstract public void run();

    public int getId() {
        return id;
    }


}
