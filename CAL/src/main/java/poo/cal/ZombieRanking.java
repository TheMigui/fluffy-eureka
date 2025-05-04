package poo.cal;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ZombieRanking {
    private ConnHub hub;
    private String statName = "ZombieRanking";
    PriorityQueue<Zombie> top3zombies = new PriorityQueue<>((z1, z2) -> Integer.compare(z1.getKillCount(), z2.getKillCount()));

    public ZombieRanking(ConnHub hub) {
        this.hub = hub;
        this.hub.addZombieRanking(this);
    }

    public synchronized void applyForRanking(Zombie z){
        if(top3zombies.contains(z)){
            updateRanking();
        } else if (top3zombies.size() < 3) {
            top3zombies.add(z);
            updateRanking();
        } else if (top3zombies.peek().getKillCount() < z.getKillCount()) {
            top3zombies.poll();
            top3zombies.add(z);
            updateRanking();
        }
    }
    public synchronized String getRanking(){
        ArrayList<Zombie> zombies = new ArrayList<>(top3zombies);
        zombies.sort((z1, z2) -> Integer.compare(z2.getKillCount(), z1.getKillCount()));
        StringBuilder ranking = new StringBuilder();
        for (Zombie z : zombies) {
            ranking.append(z.getEntityId()).append(" - ").append(z.getKillCount()).append("\n");
        }
        return ranking.toString();
    }
    public void updateRanking(){
        hub.updateStat(statName, this.getRanking());
    }
    public String getStatName() {
        return statName;
    }
}
