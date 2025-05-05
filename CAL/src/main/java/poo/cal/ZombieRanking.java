package poo.cal;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ZombieRanking {
    /*
     * ZombieRanking
     * 
     * ZombieRanking is a class that manages the ranking of Zombies based on their kill count.
     * 
     * The top 3 zombies are stored in the ranking and sent to clients.
     * 
     * Every time a Zombie kills a human, it will call the applyToRanking method to check if the ranking needs to be updated.
     * 
     * If it does, the ranking will be updated and sent to the ConnHub, which will then send it to the clients.
     */

    private ConnHub hub;
    private String statName = "ZombieRanking";

    // A priority queue is used to keep track of the top 3 Zombies.
    PriorityQueue<Zombie> top3zombies = new PriorityQueue<>((z1, z2) -> Integer.compare(z1.getKillCount(), z2.getKillCount()));

    public ZombieRanking(ConnHub hub) {
        this.hub = hub;
        this.hub.addZombieRanking(this); // Register the ZombieRanking instance with the ConnHub, so it can poll the ranking
                                         // when a new client connects.
    }

    public String getStatName() {
        return statName;
    }

    /**
     * applyToRanking
     * 
     * This method is called by a Zombie when it kills a human.
     * 
     * The ranking is reevaluated with the new information, and updated if necessary.
     * 
     * The method is synchronized to ensure that only one thread can update the ranking at a time and keep consistency.
     * @param z The Zombie whose kill count has increased.
     */
    public synchronized void applyToRanking(Zombie z){
        if(top3zombies.contains(z)){ // If the zombie is already in the ranking, we just need to update the String being sent
            updateRanking();
        } else if (top3zombies.size() < 3) { // If the ranking is not full, we just add the zombie to the ranking
            top3zombies.add(z);
            updateRanking();
        } else if (top3zombies.peek().getKillCount() < z.getKillCount()) { // If the ranking is full, we check if the new zombie 
                                                                           // has more kills than the one with the least kills
            
            // If the new zombie has more kills than the one with the least kills...
            top3zombies.poll(); // Remove the zombie with the least kills
            top3zombies.add(z); // Add the new zombie to the ranking
            updateRanking(); // Update the ranking and send it to the ConnHub
        }
    }

    /**
     * getRanking
     * 
     * This method returns the ranking of the Zombies as a String, ready for the clients to display.
     * 
     * It is synchronized because there are 2 ways to call it: from applyToRanking or from the ConnHub when a new client 
     * connects.
     * @return The ranking of the Zombies as a String.
     */
    public synchronized String getRanking(){

        // Turn the priority queue into an ArrayList and sort it in descending order
        ArrayList<Zombie> zombies = new ArrayList<>(top3zombies);
        zombies.sort((z1, z2) -> Integer.compare(z2.getKillCount(), z1.getKillCount()));

        // Create a StringBuilder to create the ranking string
        StringBuilder ranking = new StringBuilder();
        for (Zombie z : zombies) {
            ranking.append(z.getEntityId()).append(" - ").append(z.getKillCount()).append("\n");
        }
        return ranking.toString();
    }

    /**
     * updateRanking
     * 
     * This method is called by applyToRanking to send the updated ranking to all the clients through the ConnHub.
     */
    public void updateRanking(){
        hub.updateStat(statName, this.getRanking());
    }

}
