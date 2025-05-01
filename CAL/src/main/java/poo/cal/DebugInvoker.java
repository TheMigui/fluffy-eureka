package poo.cal;
import java.util.List;

public class DebugInvoker extends Thread {
    private List<Entity> threadList;
    private GlobalLock gl;
    private ApocalypseLogger logger;
    private Tunnels tunnels;
    private Refuge refuge;
    private RiskZones riskZones;
    private int howMany = 0;

    public DebugInvoker(int howMany, List<Entity> threadList, GlobalLock gl, ApocalypseLogger logger, Tunnels tunnels, Refuge refuge, RiskZones riskZones) {
        this.howMany = howMany;
        this.threadList = threadList;
        this.gl = gl;
        this.logger = logger;
        this.tunnels = tunnels;
        this.refuge = refuge;
        this.riskZones = riskZones;
    }
    @Override
    public void run() {
        for (int i = 0; i < howMany; i++) {
            Human h = new Human("H"+String.format("%04d", i), gl, logger, threadList, refuge, tunnels, riskZones);
            threadList.add(h);
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
