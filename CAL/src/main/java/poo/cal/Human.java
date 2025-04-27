package poo.cal;
import java.util.concurrent.ExecutorService;
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
            }
        }
    }
    private void prepare(){
        logger.log(id+" is waiting at the common zone");
        refuge.commonGate(this, true);
        this.sleep(random.nextInt(2)*1000 + 1000);
        refuge.commonGate(this, false);
    }
    private void leave(){
        riskZoneNo = random.nextInt(4);
        logger.log(id + " is entering into tunnel no. "+riskZoneNo);
        tunnels.enterTunnel(this, riskZoneNo);
        gl.check();
        logger.log(id + " is entering into risk zone no. "+riskZoneNo);
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
        if (zombieAttacking == null){
            food = 2;
            logger.log(id + " has picked the food and is now leaving risk zone no. " + riskZoneNo);
            riskZones.leave(this, riskZoneNo);
        }
        else{
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
        if (this.food >= 2 || this.isMarked || this.zombieAttacking != null)
            return false;
        this.zombieAttacking = z;
        return true;
    }
    private void goBack(){
        
    }
}
