package poo.cal;

import java.util.Map;

public class DebugInvoker extends Thread {
    private Map<String, Entity> threadMap;
    private GlobalLock gl;
    private ApocalypseLogger logger;
    private Tunnels tunnels;
    private Refuge refuge;
    private RiskZones riskZones;
    private int howManyHumans = 0;

    public DebugInvoker(int howManyHumans, Map<String, Entity> threadList, GlobalLock gl, ApocalypseLogger logger, Tunnels tunnels, Refuge refuge, RiskZones riskZones) {
        this.howManyHumans = howManyHumans;
        this.threadMap = threadList;
        this.gl = gl;
        this.logger = logger;
        this.tunnels = tunnels;
        this.refuge = refuge;
        this.riskZones = riskZones;
    }
    @Override
    public void run() {
        Zombie z = new Zombie("Z0000", gl, logger, riskZones, 1);
        threadMap.put("Z0000", z);
        z.start();
        for (int i = 1; i <= howManyHumans; i++) {
            Human h = new Human("H"+String.format("%04d", i), gl, logger, threadMap, refuge, tunnels, riskZones);
            threadMap.put(h.getEntityId(), h);
            h.start();
            try{
                Thread.sleep(1000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        
    }

}
