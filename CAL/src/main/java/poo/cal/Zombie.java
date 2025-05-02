package poo.cal;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
public class Zombie extends Entity{
    RiskZones riskZones;
    private int riskZoneNo;
    private AtomicInteger killCount = new AtomicInteger(0);
    private int attackDuration = 0;
    private Random random;



    public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones, int riskZoneNo) {
        super(id, gl, logger);
        this.riskZones = riskZones;
        this.riskZoneNo = riskZoneNo;
    }

    // public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones){
    //     Zombie(id, gl, logger, riskZones, random.nextInt(4) + 1);
    // } 


    public void run(){
        try{
            while(true){
                enterZombie();
                attackZombie();

            }
        }
        catch (Exception e){ //CAMBIAR

        }

    }


    private void enterZombie(){
      int riskZoneNo = random.nextInt(4);
      riskZones.enter(this,riskZoneNo);
    }
    //Me preocupa lo "synchronized" que esté esto.
    public synchronized void attackZombie(){
        RiskZone targetZone = riskZones.getRiskZone(riskZoneNo);
        Human preyHuman = riskZones.getRandomHuman(riskZoneNo);
        preyHuman.attackHuman(this);
        preyHuman.zombieAttackSequence();
       // riskZones.enter(this,riskZoneNo);
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
