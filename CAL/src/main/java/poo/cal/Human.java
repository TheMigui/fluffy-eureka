package poo.cal;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;



import java.util.concurrent.locks.Lock;
public class Human extends Entity{
    private Refuge refuge;
    private Tunnels tunnels;
    private RiskZones riskZones;
    private boolean isMarked = false;
    private boolean isAlive = true;
    private boolean isOutside = false;
    private int food = 0;
    private int riskZoneNo = 0;
    private Zombie zombieAttacking = null;
    private Map<String, Entity> threadMap;

    private Lock gatherLock = new ReentrantLock();

    public Human(String id, GlobalLock gl, ApocalypseLogger logger, Map<String, Entity> threadMap, Refuge refuge, Tunnels tunnels, RiskZones riskZones) {
        super(id, gl, logger);
        this.threadMap = threadMap;
        this.refuge = refuge;
        this.tunnels = tunnels;
        this.riskZones = riskZones;
    }
    @Override
    public String getEntityId(){
        if (isMarked)
            return super.getEntityId() + "*";
        else
            return super.getEntityId();
    }


    
    @Override
    public void run(){
        while(isAlive){
            prepare();
            leaveRefuge();
            gatherFood();
            if(isAlive){
                goBack();
                dropFoodAndRest();
                eat();
                if(isMarked)
                    heal();
            }
        }
    }
    private void prepare(){
        logger.log(id+" is waiting at the common zone");
        refuge.commonGate(this, true);
        this.sleep(random.nextInt(2)*1000 + 1000);
        refuge.commonGate(this, false);
    }
    //He cambiado esto para que espere en la zona común antes de ir al tunel
    private void leaveRefuge(){
        riskZoneNo = random.nextInt(4) + 1;
        logger.log(id + " is leaving the refuge through tunnel no. "+riskZoneNo);
        tunnels.enterTunnel(this, riskZoneNo, false);
        isOutside = true;
        riskZones.enter(this, riskZoneNo);
        logger.log(id + " has left tunnel no. "+riskZoneNo+" and is now entering into risk zone no. "+riskZoneNo);
        
    }
    private void gatherFood(){
        try{
            Thread.sleep(3000 + random.nextInt(3)*1000);
        }catch(InterruptedException e){
            if (zombieAttacking == null){
                e.printStackTrace();
            }
        }
        gl.check();
        gatherLock.lock();
        if (zombieAttacking == null){
            Thread.interrupted();
            isOutside = false;
            food = 2;
            logger.log(id + " has picked the food and is now leaving risk zone no. " + riskZoneNo);
            riskZones.leave(this, riskZoneNo);
            gatherLock.unlock();
        }
        else{
            gatherLock.unlock();
            zombieAttackSequence();
        }
    }
    private void zombieAttackSequence(){
        logger.log(id + " is being attacked by "+ zombieAttacking.getEntityId()+"!!!");
        riskZones.notifyAttack(this, zombieAttacking, riskZoneNo, true);
        this.sleep(zombieAttacking.getAttackDuration());
        this.isAlive = random.nextInt(3) != 0; // 1/3 chance of dying
        if (this.isAlive){
            this.isMarked = true;
            this.isOutside = false;
            this.food = 0;
            logger.log(id + " survived the attack from " + zombieAttacking.getEntityId() + " and is now leaving risk zone no. " + riskZoneNo + ". Phew!");
        }
        else{
            logger.log(id + " was killed by "+zombieAttacking.getEntityId() + " and has become the zombie no. " + id.replace('H', 'Z') + ". RIP");
            zombieAttacking.incrementKillCount();
            logger.log(zombieAttacking.getEntityId() + "'s kill count is now " + zombieAttacking.getKillCount());
            Zombie newZombie = new Zombie(id.replace('H', 'Z'), gl, logger, riskZones, riskZoneNo);
            newZombie.setDaemon(true);
            threadMap.put(newZombie.getEntityId(), newZombie);
            threadMap.remove(id);
            newZombie.start();

        }
        riskZones.notifyAttack(this, zombieAttacking, riskZoneNo, false);
        zombieAttacking.endAttack();
        zombieAttacking = null;
    }
    public boolean attackHuman(Zombie z){
        try{
            gatherLock.lock();
            if (!this.isOutside || this.isMarked || this.zombieAttacking != null)
                return false;
            this.zombieAttacking = z;
            threadMap.get(this.getEntityId()).interrupt();
            return true;
        }
        finally{
            gatherLock.unlock();
        }

    }
    private void goBack(){
        logger.log(id+" is getting back to the refuge through tunnel no. "+riskZoneNo);
        tunnels.enterTunnel(this, this.riskZoneNo, true);
        logger.log(id+" has left tunnel no. "+riskZoneNo+" and is now inside the refuge");
    }
    private void dropFoodAndRest(){
        refuge.dropFood(this.food);
        if(this.food > 0)
            logger.log(id + " has dropped " + food + " pieces of food in the refuge and is now going to rest");
        this.food = 0;
        logger.log(id + " is entering the rest zone");
        refuge.restGate(this, true);
        this.sleep(2000 + random.nextInt(3) * 1000);
        logger.log(id + " is leaving the rest zone");
        refuge.restGate(this, false);
    }
    private void eat(){
        logger.log(id + " is entering the dining zone");
        refuge.diningGate(this, true);
        refuge.eat();
        logger.log(id + " has got a piece of food and is now eating");
        this.sleep(3000 + random.nextInt(3)*1000);
        logger.log(id + " is leaving the dining zone");
        refuge.diningGate(this, false);
    }
    private void heal(){
        logger.log(id + " is entering the rest zone for healing");
        refuge.restGate(this, true);
        this.sleep(3000 + random.nextInt(3)*1000);
        this.isMarked = false;
        logger.log(id + " has healed and is leaving the rest zone");
        refuge.restGate(this, false);
    }
    public int getriskZoneNo() {
        return riskZoneNo;
    }
}
