package poo.cal;



import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.util.ArrayList;
import java.util.Random;

public class RiskZone {

    /*
     * RiskZone
     * 
     * This class represents a risk zone in the simulation.
     * 
     * It contains a list of humans and another one of zombies that are in the risk zone.
     * 
     * It also contains a list of humans and zombies that are currently involved in an attack.
     * 
     * The humans and zombies in these lists are displayed in the GUI using JTextPanes.
     * 
     * The lists with humans and zombies involved in an attack are displayed first, and in red.
     * 
     * All methods are synchronized in order to protect the integrity of the lists.
     * 
     * The exception is updateGui, since it will always be called from a synchronized method.
     * 
     * Two ReportingAtomicIntegers are used to keep track of the number of humans and zombies in the risk zone.
     */
    private ArrayList<Human> humanList = new ArrayList<>();
    private ArrayList<Human> humanBeingAttackedList = new ArrayList<>();
    private ArrayList<Zombie> zombieList = new ArrayList<>();
    private ArrayList<Zombie> zombieAttackingList = new ArrayList<>();

    private JTextPane humanTextPane;
    private JTextPane zombieTextPane;

    private Style normalStyle;
    private Style attackStyle;

    private Random random = new Random();

    private ReportingAtomicInteger humansInRiskZone;
    private ReportingAtomicInteger zombiesInRiskZone;

    /**
     * RiskZone constructor
     * 
     * It initializes the JTextPanes and the styles used to display the humans and zombies in the risk zone.
     * 
     * It also sets up the ReportingAtomicIntegers used to keep track of the number of humans and zombies in the risk zone.
     * @param id the id of the risk zone (used to specify the statistic name of the ReportingAtomicIntegers)
     * @param humanTextPane // JTextPane used to display the humans in the risk zone (both from humansList and humanBeingAttackedList)
     * @param zombieTextPane // JTextPane used to display the zombies in the risk zone (both from zombiesList and zombieAttackingList)
     * @param hub // ConnHub used to send the statistics to the clients (used for the ReportingAtomicIntegers)
     */
    public RiskZone(int id, JTextPane humanTextPane, JTextPane zombieTextPane, ConnHub hub) {
        this.humanTextPane = humanTextPane;
        this.zombieTextPane = zombieTextPane;
        this.normalStyle = humanTextPane.addStyle("Normal", null);
        StyleConstants.setForeground(normalStyle, java.awt.Color.BLACK);
        this.attackStyle = humanTextPane.addStyle("Attack", null);
        StyleConstants.setForeground(attackStyle, java.awt.Color.RED);

        this.humansInRiskZone = new ReportingAtomicInteger(hub, "RiskZoneHumans"+Integer.toString(id));
        this.zombiesInRiskZone = new ReportingAtomicInteger(hub, "RiskZoneZombies"+Integer.toString(id));
    }

    /**
     * enter
     * 
     * This method is used to add a human or zombie to the risk zone. It will be added to its corresponding list.
     * 
     * The GUI will also be updated to reflect the changes in the lists.
     * @param e the entity to be added to the risk zone (either a human or a zombie)
     */
    public synchronized void enter(Entity e) {
           if (e instanceof Human) {
                humanList.add((Human) e);
                humansInRiskZone.incrementAndReport();
            } else if (e instanceof Zombie) {
                zombieList.add((Zombie) e);
                zombiesInRiskZone.incrementAndReport();
            }
            updateGui();
    }

    /**
     * leave
     * 
     * This method is the opposite of the enter method.
     * @param e the entity to be removed from the risk zone (either a human or a zombie)
     */
    public synchronized void leave(Entity e) {
            if (e instanceof Human) {
                humanList.remove((Human) e);
                humansInRiskZone.decrementAndReport();
            } else if (e instanceof Zombie) {
                zombieList.remove((Zombie) e);
                zombiesInRiskZone.decrementAndReport();
            }
            updateGui();
    }

    /**
     * getRandomHuman
     * 
     * This method assists the zombies in attacking humans.
     * 
     * A random index is generated, and the human at that index is attacked by the zombie.
     * 
     * If the attack cannot be performed, the next human in the list is tried, and so on.
     * 
     * If there are no humans left in the risk zone, or no humans can be attacked, null is returned to tell the zombie to give up
     * @param z the zombie that is trying to attack
     * @return the human that was attacked (if its attackHuman returned true), or null if no human could be attacked
     */
    public synchronized Human getRandomHuman(Zombie z){
        if(humanList.isEmpty()){ // No humans means that the zombie must give up
            return null;
        }

        int randomIndex = random.nextInt(humanList.size());
        int lastIndex = randomIndex; // The ArrayList is accessed in a circular way, so we need to keep track of the last index

        boolean success = humanList.get(randomIndex).attackHuman(z); // Try attacking the human at the random index
        randomIndex = (randomIndex + 1) % humanList.size();

        while(!success && randomIndex != lastIndex){ // Continue attacking the next human until one returns true or we have tried all humans
            success = humanList.get(randomIndex).attackHuman(z);
            randomIndex = (randomIndex + 1) % humanList.size();
        }
        return success ? humanList.get(randomIndex) : null;
    }

    /**
     * notifyAttack
     * 
     * This method is used to notify the risk zone that a human is being attacked by a zombie, so that the GUI can be updated.
     * 
     * As specified before, the human and zombie are removed from their respective lists when the attack is taking place and
     * added into special lists that display on top of the JTextPane and in red.
     * @param h the human that is being attacked
     * @param z the zombie that is attacking the human
     * @param isAttacking true if the attack is taking place, false if the attack has ended
     */
    public synchronized void notifyAttack(Human h, Zombie z, boolean isAttacking) {
            if (isAttacking) { // The notification is about an attack that is taking place
                humanBeingAttackedList.add(h);
                zombieAttackingList.add(z);
                humanList.remove(h);
                zombieList.remove(z);

            } else { // The notification is about an attack that has ended
                humanBeingAttackedList.remove(h);
                zombieAttackingList.remove(z);
                zombieList.add(z);
                // No need to add the human back, it will be leaving the risk zone anyways
            }
            updateGui();
    }

    /**
     * updateGui
     * 
     * This method is used to update the GUI with the contents of the lists.
     * 
     * There is a JTextPane for humans and another one for zombies.
     * 
     * Since there are 2 JTextPanes for 4 lists, and some of them have special font conditions GraphicArrayList is not used here.
     * 
     * In every JTextPane, humans and zombies that are involved in an attack are displayed first, and in red.
     * 
     * Then, the rest of the humans and zombies are displayed in black.
     */
    private void updateGui() {
        try{
            // Humans being attacked
            StringBuilder sbHumansBeingAttacked = new StringBuilder();
            for (Human h : humanBeingAttackedList) {
                sbHumansBeingAttacked .append(h.getEntityId()).append("\n");
            }

            // Humans in the risk zone
            StringBuilder sbHumans = new StringBuilder();
            for (Human h : humanList) {
                sbHumans.append(h.getEntityId()).append("\n");
            }

            // Zombies attacking
            StringBuilder sbZombiesAttacking = new StringBuilder();
            for (Zombie z : zombieAttackingList) {
                sbZombiesAttacking.append(z.getEntityId()).append("\n");
            }

            // Zombies in the risk zone
            StringBuilder sbZombies = new StringBuilder();
            for (Zombie z : zombieList) {
                sbZombies.append(z.getEntityId()).append("\n");
            }

            
            StyledDocument humanDoc = humanTextPane.getStyledDocument();
            humanTextPane.setText(""); // Clear the JTextPane so getLength works as intended

            // Insert the humans and zombies into the JTextPane, using the styles defined before
            humanDoc.insertString(humanDoc.getLength(), sbHumansBeingAttacked .toString(), attackStyle);
            humanDoc.insertString(humanDoc.getLength(), sbHumans.toString(), normalStyle);

            
            StyledDocument zombieDoc = zombieTextPane.getStyledDocument();
            zombieTextPane.setText(""); // Clear the JTextPane so getLength works as intended

            // Insert the humans and zombies into the JTextPane, using the styles defined before
            zombieDoc.insertString(zombieDoc.getLength(), sbZombiesAttacking.toString(), attackStyle);
            zombieDoc.insertString(zombieDoc.getLength(), sbZombies.toString(), normalStyle);
            
        }catch (BadLocationException e) {
            e.printStackTrace();
            humanTextPane.setText("Error updating GUI");
            zombieTextPane.setText("Error updating GUI");
        }
    }

}
