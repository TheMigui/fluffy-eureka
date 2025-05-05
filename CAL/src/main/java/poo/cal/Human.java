package poo.cal;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;



import java.util.concurrent.locks.Lock;
public class Human extends Entity{
    /*
     * Human
     * 
     * This class represents a human in the simulation. It does all the actions that a human is expected to do.
     * 
     * Every human will be run by a thread, as it extends Entity, which extends Thread.
     */
    private Refuge refuge;
    private Tunnels tunnels;
    private RiskZones riskZones;
    private boolean isMarked = false;
    private boolean isAlive = true;
    private boolean isOutside = false;
    private int food = 0;
    private Zombie attackingZombie = null;
    private Map<String, Entity> threadMap; // Map of all threads in the simulation, used for interruptions

    private Lock gatherLock = new ReentrantLock(); // This lock is used while the human is outside, so changes to shared
                                                   // variables between the human and the zombie are synchronized

    /**
     * Human constructor
     * @param id
     * @param gl
     * @param logger
     * @param threadMap
     * @param refuge
     * @param tunnels
     * @param riskZones
     */
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

    /**
     * run
     * 
     * This method is called when the thread is started. It runs the main loop of the human.
     * 
     * For readability, the method is divided into several smaller methods that represent each action of the human.
     */
    public void run(){
        super.run();
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

    /**
     * prepare
     * 
     * This method represents the first action of the human according to
     * the requirements.
     */
    private void prepare(){
        logger.log(id+" is waiting at the common zone");
        refuge.commonGate(this, true);
        this.sleep(random.nextInt(1001) + 1000);
        refuge.commonGate(this, false);
    }
    
    /**
     * leaveRefuge
     * 
     * This method represents the 2nd action of the human according to
     * the requirements.
     */
    private void leaveRefuge(){
        riskZoneNo = random.nextInt(4) + 1; // Chooses a random 
                                                  // risk zone (and tunnel)
        logger.log(id + " is leaving the refuge through tunnel no. "+riskZoneNo);
        tunnels.enterTunnel(this, riskZoneNo, false);
        isOutside = true;
        riskZones.enter(this, riskZoneNo);
        logger.log(id + " has left tunnel no. "+riskZoneNo+" and is now entering into risk zone no. "+riskZoneNo);
        
    }

    /**
     * gatherFood
     * 
     * This method represents the 3rd action of the human according to
     * the requirements, and starts the attack sequence if attacked by 
     * a zombie.
     */
    private void gatherFood(){
        // Exceptionally, the entity method for sleeping is not used here,
        // in order to handle the interruption of the thread by the zombie
        try{
            Thread.sleep(3000 + random.nextInt(2001));
        }catch(InterruptedException e){
            // Before interrupting the thread, the zombie sets the
            // attackingZombie variable to itself, so if attackingZombie is
            // not null, it means that there was no error, the interruption
            // was done by the zombie and the human is being attacked
            if (attackingZombie == null){
                e.printStackTrace();
            }
        }
        gl.check(); // Check if the simulation has been paused while
                    // sleeping
        gatherLock.lock(); // Locks sensitive variables that determine if the human can be attacked
        if (attackingZombie == null){ // If the human was attacked during
                                      // the sleep, the attackingZombie 
                                      //variable would not be null
            isOutside = false;
            food = 2;
            logger.log(id + " has picked the food and is now leaving risk zone no. " + riskZoneNo);
            riskZones.leave(this, riskZoneNo);
            gatherLock.unlock();
        }
        else{ // This means that the human was attacked by a zombie
            gatherLock.unlock();
            zombieAttackSequence();
        }
    }

    /**
     * zombieAttackSequence
     * 
     * This method is called when the human has detected a zombie attack.
     * 
     * It is important to note that it's the human who handles the attack
     * logic, the zombie simply interrupts the human thread and sets the
     * attackingZombie variable. After that, the zombie waits for the 
     * human to signal that the attack is over.
     */
    private void zombieAttackSequence(){
        logger.log(id + " is being attacked by "+ attackingZombie.getEntityId()+"!!!");
        riskZones.notifyAttack(this, attackingZombie, riskZoneNo, true); // this is used so the TextPane shows
                           // the human and the attacking zombie
                           // in red and on top of the rest.
        this.sleep(attackingZombie.getAttackDuration()); // The duration of the attack is determined by the zombie, as required
        this.isAlive = random.nextInt(3) != 0; // 1/3 chance of dying
        if (this.isAlive){
            // The human is marked and leaves, but loses the food
            this.isMarked = true;
            this.isOutside = false;
            this.food = 0;

            logger.log(id + " survived the attack from " + attackingZombie.getEntityId() + " and is now leaving risk zone no. " + riskZoneNo + ". Phew!");
        }
        else{
            logger.log(id + " was killed by "+attackingZombie.getEntityId() + " and has become the zombie no. " + id.replace('H', 'Z') + ". RIP");

            attackingZombie.incrementKillCount();
            logger.log(attackingZombie.getEntityId() + "'s kill count is now " + attackingZombie.getKillCount());

            // Creates the new zombie that replaces the human
            Zombie newZombie = new Zombie(id.replace('H', 'Z'), gl, logger, riskZones, attackingZombie.getZombieRanking(), riskZoneNo);
            newZombie.setDaemon(true);

            // The new zombie is added to the threadMap and the human is removed
            threadMap.put(newZombie.getEntityId(), newZombie);
            threadMap.remove(id);
            newZombie.start();
        }
        // These actions must be done regardless of the outcome of the attack

        // Remove the human and zombie from the top of the TextPane
        riskZones.notifyAttack(this, attackingZombie, riskZoneNo, false);

        // The human leaves the risk zone
        riskZones.leave(this, riskZoneNo);
        // The zombie is notified that the attack is over, so it can start doing things again
        attackingZombie.endAttack();

        // This variable is cleaned up to avoid problems with the next attack
        attackingZombie = null;
    }

    /**
     * attackHuman
     * 
     * This method is called by the zombie when it wants to attack a human.
     * 
     * The attack will be unsuccesful if:
     * - The human is no longer outside (it has just entered the refuge)
     * - The human is marked (it has just been attacked by a zombie before)
     * - The human is already being attacked by another zombie
     * 
     * Otherwise, the human will be interrupted and it will handle the attack logic.
     * @param z The zombie that is attacking the human
     * @return true if the human was succesfully attacked (from that moment on, the human will handle the attack logic and the 
     * zombie will wait), false if the human could not be attacked
     */
    public boolean attackHuman(Zombie z){
        try{
            gatherLock.lock(); // Locks sensitive variables that determine if the human can be attacked
            if (!this.isOutside || this.isMarked || this.attackingZombie != null)
                return false;
            this.attackingZombie = z;
            // Keep in mind that it is the zombie who is running this method, so it needs the threadMap to know which thread to interrupt
            threadMap.get(this.getEntityId()).interrupt();
            return true;
        }
        finally{
            gatherLock.unlock();
        }

    }

    /**
     * goBack
     * 
     * This is just a helper method to make the code more readable.
     */
    private void goBack(){
        logger.log(id+" is getting back to the refuge through tunnel no. "+riskZoneNo);
        tunnels.enterTunnel(this, this.riskZoneNo, true);
        logger.log(id+" has left tunnel no. "+riskZoneNo+" and is now inside the refuge");
    }

    /**
     * dropFoodAndRest
     * 
     * This method represents the 5th action of the human according to
     * the requirements.
     */
    private void dropFoodAndRest(){
        refuge.dropFood(this.food);
        if(this.food > 0)
            logger.log(id + " has dropped " + food + " pieces of food in the refuge and is now going to rest");
        this.food = 0;

        logger.log(id + " is entering the rest zone");
        refuge.restGate(this, true);

        this.sleep(2000 + random.nextInt(2001));

        logger.log(id + " is leaving the rest zone");
        refuge.restGate(this, false);
    }

    /**
     * eat
     * 
     * This method represents the 6th action of the human according to
     * the requirements.
     */
    private void eat(){
        logger.log(id + " is entering the dining zone");
        refuge.diningGate(this, true);

        refuge.eat(); // If there is no food, the human will wait until food is available (see Refuge)
        logger.log(id + " has got a piece of food and is now eating");

        this.sleep(3000 + random.nextInt(2001));

        logger.log(id + " is leaving the dining zone");
        refuge.diningGate(this, false);
    }

    /**
     * heal
     * 
     * This method represents the 7th action of the human according to
     * the requirements.
     */
    private void heal(){
        logger.log(id + " is entering the rest zone for healing");
        refuge.restGate(this, true);
        this.sleep(3000 + random.nextInt(2001));
        this.isMarked = false;
        logger.log(id + " has healed and is leaving the rest zone");
        refuge.restGate(this, false);
    }
    
}
