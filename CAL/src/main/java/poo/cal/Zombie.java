package poo.cal;

import java.util.concurrent.atomic.AtomicInteger;
public class Zombie extends Entity{

    /**
     * Zombie
     * 
     * This class represents a Zombie in the simulation.
     * 
     * Zombies are threads that run in parallel to humans, following a set of predefined actions.
     */
    RiskZones riskZones;
    private int riskZoneNo;
    private AtomicInteger killCount = new AtomicInteger(0);
    private int attackDuration = 0;
    private ZombieRanking zombieRanking;

    /**
     * Zombie constructor
     * 
     * This constructor initializes the Zombie instance with the given parameters.
     * @param id The id of the Zombie ("Z0001", "Z1234"...)
     * @param gl GlobalLock instance used for checking if the simulation is paused (see Entity)
     * @param logger ApocalypseLogger instance used for logging events
     * @param riskZones RiskZones instance used for entering and leaving risk zones
     * @param zombieRanking ZombieRanking instance used for updating the Zombie ranking at the clients' GUI
     * @param riskZoneNo Initial risk zone number (1-4) where the Zombie will start (it depends on where the human died)
     */
    public Zombie(String id, GlobalLock gl, ApocalypseLogger logger, RiskZones riskZones, ZombieRanking zombieRanking, int riskZoneNo){ 
        super(id, gl, logger);
        this.riskZones = riskZones;
        this.riskZoneNo = riskZoneNo;
        this.zombieRanking = zombieRanking;
    }

    public int getAttackDuration() { // Although the human handles the logic of the attack, the specifications
                                     // say that the Zombie should be the one to determine the attack duration.
                                     // Therefore, the human needs access to said duration
        return attackDuration;
    }
    public ZombieRanking getZombieRanking() { // This is used to share the ZombieRanking with new Zombies when creating them
                                              // inside Human.zombieAttackSequence
        return zombieRanking;
    }
    public int getKillCount() {
        return killCount.get();
    }
    @Override

    /**
     * run
     * 
     * This method is called when the thread is started.
     * 
     * In order to improve readability, the method is divided into several smaller methods.
     */
    public void run(){
        // Initial setup
        super.run();
        enterRiskZone();

        // In order to avoid an excessive murder chain, zombies wait for a while when created (although this was not specified).
        this.sleep(2000 + random.nextInt(1001));
        leaveRiskZone();
        
        // Main loop
        while(true){
            enterRiskZone();
            lookForHumans();
            leaveRiskZone();
        }
    }

    /**
     * enterRiskZone
     * 
     * The zombie enters the Risk Zone chosen randomly inside leaveRiskZone
     */
    private void enterRiskZone(){
      riskZones.enter(this,riskZoneNo);
    }

    /**
     * lookForHumans
     * 
     * After entering a risk zone, the zombie looks for humans using RiskZone.getRandomHuman and passing itself as a parameter
     * 
     * It will also generate a new random attack duration
     * 
     * If a human gets successfully attacked, the zombie will wait using its monitor until the attack is over.
     * 
     * Either way, the zombie will wait 2-3 seconds before leaving the risk zone.
     */
    private void lookForHumans(){
        logger.log(this.id + " is inside Risk Zone no. " + riskZoneNo + " and is looking for fresh meat");

        attackDuration = random.nextInt(1001) + 500; // Generate a new random attack duration

        Human prey = riskZones.getRandomHuman(riskZoneNo, this);
        if (prey == null){ // This means that there are no humans in the risk zone, or no humans are eligible for being attacked
            logger.log(this.id + " found no one and is waiting to leave");
        }else{ // This means that the zombie has attacked a human
            synchronized(this){
                try{
                    this.wait(); // Wait for the attack to finish
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.sleep(2000 + random.nextInt(1001)); // Wait 2-3 seconds before leaving the risk zone

      }

    /**
     * endAttack
     * 
     * This method is called by the human thread when the attack is over.
     * 
     * It notifies the zombie to continue its execution.
     * 
     * The method is synchronized in order to notify the zombie thread
     */
    public synchronized void endAttack(){
        this.notify();
    }

    /**
     * incrementKillCount
     * 
     * When a human is killed, it calls this method to increment the kill count of the zombie.
     * 
     * It will also call the ZombieRanking instance to update the ranking at the clients' GUI.
     */
    public void incrementKillCount(){   
        killCount.incrementAndGet();
        zombieRanking.applyToRanking(this);
    }

    /**
     * leaveRiskZone
     * 
     * The zombie leaves the risk zone and chooses a new one randomly.
     * 
     * The method will ensure that the zombie will pick a new risk zone
     */
    private void leaveRiskZone(){
        riskZones.leave(this,riskZoneNo);
        int previousZone = riskZoneNo;
        while (previousZone == riskZoneNo){
            riskZoneNo = 1 + random.nextInt(4);
        }
    }






}
