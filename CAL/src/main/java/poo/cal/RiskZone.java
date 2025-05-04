package poo.cal;



import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.util.ArrayList;
import java.util.Random;

public class RiskZone {
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

    public synchronized void notifyAttack(Human h, Zombie z, boolean isAttacking) {
            if (isAttacking) {
                humanBeingAttackedList.add(h);
                zombieAttackingList.add(z);
                humanList.remove(h);
                zombieList.remove(z);

            } else {
                humanBeingAttackedList.remove(h);
                zombieAttackingList.remove(z);
                zombieList.add(z);
            }
            updateGui();
    }

    private void updateGui() {
        try{

            StringBuilder sbHumansBeingAttacked = new StringBuilder();
            for (Human h : humanBeingAttackedList) {
                sbHumansBeingAttacked .append(h.getEntityId()).append("\n");
            }

            StringBuilder sbHumans = new StringBuilder();
            for (Human h : humanList) {
                sbHumans.append(h.getEntityId()).append("\n");
            }

            StringBuilder sbZombiesAttacking = new StringBuilder();
            for (Zombie z : zombieAttackingList) {
                sbZombiesAttacking.append(z.getEntityId()).append("\n");
            }

            StringBuilder sbZombies = new StringBuilder();
            for (Zombie z : zombieList) {
                sbZombies.append(z.getEntityId()).append("\n");
            }

            humanTextPane.setText("");
            StyledDocument humanDoc = humanTextPane.getStyledDocument();
            humanDoc.insertString(humanDoc.getLength(), sbHumansBeingAttacked .toString(), attackStyle);
            humanDoc.insertString(humanDoc.getLength(), sbHumans.toString(), normalStyle);

            
            StyledDocument zombieDoc = zombieTextPane.getStyledDocument();
            zombieTextPane.setText("");
            zombieDoc.insertString(zombieDoc.getLength(), sbZombiesAttacking.toString(), attackStyle);
            zombieDoc.insertString(zombieDoc.getLength(), sbZombies.toString(), normalStyle);
            
        }catch (BadLocationException e) {
            e.printStackTrace();
            humanTextPane.setText("Error updating GUI");
            zombieTextPane.setText("Error updating GUI");
        }
    }

    public synchronized Human getRandomHuman(Zombie z){
            if(humanList.isEmpty()){
                return null;
            }
            int randomIndex = random.nextInt(humanList.size());
            int lastIndex = randomIndex;
            boolean success = humanList.get(randomIndex).attackHuman(z);
            randomIndex = (randomIndex + 1) % humanList.size();
            while(!success && randomIndex != lastIndex){
                success = humanList.get(randomIndex).attackHuman(z);
                randomIndex = (randomIndex + 1) % humanList.size();
            }
            return success ? humanList.get(randomIndex) : null;
    }

    

}
