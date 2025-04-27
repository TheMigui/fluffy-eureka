package poo.cal;

public class Human extends Entity{
    private Refuge refuge;
    private Tunnels tunnels;
    private RiskZones riskZones;
    private boolean isMarked = false;
    private boolean isAlive = true;
    private int food = 0;
    public Human(String id, GlobalLock gl, ApocalypseLogger logger, Refuge refuge, Tunnels tunnels, RiskZones riskZones) {
        super(id, gl, logger);
        this.refuge = refuge;
        this.tunnels = tunnels;
        this.riskZones = riskZones;
    }
    
    @Override
    public void run(){
        while(isAlive){
            prepare();
            gl.check();
            leave();
            gatherFood();
        }
    }
    private void prepare(){
        refuge.commonGate(this, true);
        this.sleep(random.nextInt(2)*1000 + 1000);
        refuge.commonGate(this, false);
    }
    private void leave(){
        int n = random.nextInt(4);
        tunnels.enterTunnel(this, n);
        gl.check();
        riskZones.enter(this, n);
    }
    private void gatherFood(){
        
    }
}
