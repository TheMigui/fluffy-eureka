package poo.cal;


import java.util.concurrent.atomic.AtomicInteger;
public class Zombie extends Entity{
    RiskZones riskZones;
    private int riskZoneNo;
    private AtomicInteger killCount = new AtomicInteger(0);
    private int attackDuration = 0;



    public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones, int riskZoneNo) {
        super(id, gl, logger);
        this.riskZones = riskZones;
        this.riskZoneNo = riskZoneNo;
    }

    // public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones){
    //     Zombie(id, gl, logger, riskZones, random.nextInt(4) + 1);
    // } 

    public synchronized void endAttack(){}



    public int getAttackDuration() {
        return attackDuration;
    }

    public void incrementKillCount(){
        killCount.incrementAndGet();
    }
    public int getKillCount(){
        return killCount.get();
    }


    @Override
    public void run(){}
}
