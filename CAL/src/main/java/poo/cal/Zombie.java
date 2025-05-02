package poo.cal;

import java.util.concurrent.atomic.AtomicInteger;
public class Zombie extends Entity{
    RiskZones riskZones;
    private int riskZoneNo;
    private AtomicInteger killCount = new AtomicInteger(0);
    private int attackDuration = 0;




    public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones, int riskZoneNo){ 
        super(id, gl, logger);
        this.riskZones = riskZones;
        this.riskZoneNo = riskZoneNo;
    }



    public void run(){
        enterDangerZone();
        this.sleep(2000 + random.nextInt(2)*1000);
        leaveDangerZone();
        while(true){
            enterDangerZone();
            attackZombie();
            leaveDangerZone();
        }


    }


    private void enterDangerZone(){
      
      riskZones.enter(this,riskZoneNo);
    }
    private void leaveDangerZone(){
        riskZones.leave(this,riskZoneNo);
        int previousZone = riskZoneNo;
        while (previousZone == riskZoneNo){
            riskZoneNo = 1 + random.nextInt(4);
        }
    }
    public synchronized void attackZombie(){
        logger.log(this.id + " is inside Risk Zone no. " + riskZoneNo + " and is looking for fresh meat");
        attackDuration = random.nextInt(1001) + 500;
        Human prey = riskZones.getRandomHuman(riskZoneNo);
        while (prey != null && !prey.attackHuman(this)){
            prey = riskZones.getRandomHuman(riskZoneNo);
        }
        if (prey == null){
            logger.log(this.id + " found no one and is waiting to leave");
        }else{
            synchronized(this){
                try{
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.sleep(2000 + random.nextInt(2)*1000);

      }
    public synchronized void endAttack(){
        this.notify();
    }



    public int getAttackDuration() {
        return attackDuration;
    }

    public void incrementKillCount(){   
        killCount.incrementAndGet();
    }
    public int getKillCount(){
        return killCount.get();
    }


    
}
