package poo.cal;

import java.util.Map;
import java.util.Random;
public class HumanGenerator extends Thread {
    /*
     * HumanGenerator
     * 
     * This class is responsible for generating humans in the simulation without blocking the main thread.
     * 
     */
    private Map<String, Entity> threadMap;
    private GlobalLock gl;
    private ApocalypseLogger logger;
    private Tunnels tunnels;
    private Refuge refuge;
    private RiskZones riskZones;
    private ZombieRanking zombieRanking;
    private int howManyHumans = 0;
    private Random random = new Random();

    public HumanGenerator(int howManyHumans, Map<String, Entity> threadList, GlobalLock gl, ApocalypseLogger logger, Tunnels tunnels, Refuge refuge, RiskZones riskZones, ZombieRanking zombieRanking) {
        this.howManyHumans = howManyHumans;
        this.threadMap = threadList;
        this.gl = gl;
        this.logger = logger;
        this.tunnels = tunnels;
        this.refuge = refuge;
        this.riskZones = riskZones;
        this.zombieRanking = zombieRanking;
    }
    @Override

    /**
     * run
     * 
     * This method is called when the thread is started. It creates a new Zombie and a new Human for each human to be generated.
     * 
     * It also adds all threads to the threadMap, so they can be accessed later.
     * 
     * Every time a new human is created, the thread waits and then checks the Global Lock to stop generating humans
     * if the simulation is paused.
     */
    public void run() {
        Zombie z = new Zombie("Z0000", gl, logger, riskZones, zombieRanking, 1);
        z.setDaemon(true);
        threadMap.put("Z0000", z);
        z.start();
        
        for (int i = 1; i <= howManyHumans; i++) {
            Human h = new Human("H"+String.format("%04d", i), gl, logger, threadMap, refuge, tunnels, riskZones);
            threadMap.put(h.getEntityId(), h);
            h.setDaemon(true);
            h.start();
            try{
                Thread.sleep(random.nextInt(1501) + 500);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            gl.check();
        }
    }

}
