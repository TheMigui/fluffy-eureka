package poo.cal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
public class Human extends Entity{
    private Refuge refuge;
    private Tunnels tunnels;
    private RiskZones riskZones;
    private boolean isMarked = false;
    private boolean isAlive = true;
    private int food = 0;
    private int riskZoneNo = 0;
    private Zombie zombieAttacking = null;
    private ExecutorService pool;

    private Lock gatherLock = new ReentrantLock();

    public Human(String id, GlobalLock gl, ApocalypseLogger logger, ExecutorService pool, Refuge refuge, Tunnels tunnels, RiskZones riskZones) {
        super(id, gl, logger);
        this.pool = pool;
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
    private void leave(){
        riskZoneNo = random.nextInt(4);
        logger.log(id + " wants to enter into tunnel no. "+riskZoneNo+1);
        try {
            refuge.awaitBarrier(riskZoneNo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gl.check();
        logger.log(id + " has left tunnel no. "+riskZoneNo+" and is now entering into risk zone no. "+riskZoneNo);
        riskZones.enter(this, riskZoneNo);
    }
    private void gatherFood(){
        try{
            Thread.sleep(3000 + random.nextInt(3)*1000);
        }catch(InterruptedException e){
            if (zombieAttacking != null){
                e.printStackTrace();
            }
        }
        gl.check();
        gatherLock.lock();
        if (zombieAttacking == null){
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
        logger.log(id + " is being attacked by "+ zombieAttacking.getId()+"!!!");
        riskZones.notifyAttack(this, zombieAttacking, riskZoneNo, true);
        this.sleep(zombieAttacking.getAttackDuration());
        this.isAlive = random.nextBoolean();
        if (this.isAlive){
            this.isMarked = true;
            this.food = 0;
            logger.log(id + " survived the attack from " + zombieAttacking.getId() + " and is now leaving risk zone no. " + riskZoneNo + ". Phew!");
        }
        else{
            logger.log(id + " was killed by "+zombieAttacking.getId() + " and has become the zombie no. " + id.replace('H', 'Z') + ". RIP");
            zombieAttacking.incrementKillCount();
            Zombie newZombie = new Zombie(id.replace('H', 'Z'), gl, logger, riskZones, riskZoneNo);
            pool.execute(newZombie);
        }
        zombieAttacking.endAttack();
        riskZones.notifyAttack(this, zombieAttacking, riskZoneNo, true);
        riskZones.leave(this, riskZoneNo);
        
    }
    public synchronized boolean attackHuman(Zombie z){
        try{
            gatherLock.lock();
            if (this.food >= 2 || this.isMarked || this.zombieAttacking != null)
            return false;
            this.zombieAttacking = z;
            return true;
        }
        finally{
            gatherLock.unlock();
        }

    }
    private void goBack(){
        logger.log(id+" has entered tunnel no. "+riskZoneNo);
        tunnels.enterTunneldang(this);
        logger.log(id+" has left tunnel no. "+riskZoneNo+" and is now inside the refuge");
    }
    private void dropFoodAndRest(){
        gl.check();
        refuge.dropFood(food);
        //log
        this.food = 0;
        gl.check();
        logger.log(id + " is entering the rest zone");
        refuge.restGate(this, true);
        this.sleep(2000 + random.nextInt(3) * 1000);
        logger.log(id + " is leaving the rest zone");
        refuge.restGate(this, false);
    }
    private void eat(){
        logger.log(id + " is entering the dining zone");
        refuge.diningGate(this, true);
        gl.check();
        refuge.eat();
        logger.log(id + " has got a piece of food and is now eating");
        this.sleep(3000 + random.nextInt(3)*1000);
        logger.log(id + " is leaving the dining zone");
        refuge.diningGate(this, false);
    }
    private void heal(){
        logger.log(id + " is entering the rest zone for heaing");
        refuge.restGate(this, true);
        gl.check();
        this.sleep(3000 + random.nextInt(3)*1000);
        this.isMarked = false;
        logger.log(id + " has healed and is leaving the rest zone");
        refuge.restGate(this, false);
    }
    public int getriskZoneNo() {
        return riskZoneNo;
    }
}
