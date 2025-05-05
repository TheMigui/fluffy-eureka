package poo.cal;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import poo.cal.GraphicArrayList.Direction;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Tunnel {
    /*
     * Tunnel
     * 
     * This class represents a tunnel in the simulation.
     * 
     * It has two areas: one for humans entering the refuge, and one for humans leaving the refuge.
     * 
     * The area for humans entering the refuge is represented by a GraphicArrayList, and Critical Regions are used 
     * whenever it is necessary to modify it
     * 
     * The area for humans leaving the refuge is divided into two parts: one for humans waiting for a group,
     * and one for humans already in a group.
     * 
     * A CyclicBarrier is used to form groups of 3 humans.
     * 
     * In order to properly manage these two parts, 2 ArrayLists, a Lock, and a Condition are used.
     * 
     * Since 2 ArrayLists are displayed in the same JTextPane, a custom method is used to update the JTextPane.
     * 
     * Humans already in a group are displayed on top in green, and humans waiting for a group are displayed in black.
     * 
     * ReentrantLocks with fair mode are used to ensure fairness in the order of access to the tunnel.
     * 
     * A Condition is used to handle the case when the tunnel is full, ensuring priority to humans entering the refuge.
     */
    private GraphicArrayList<Human> enteringRefugeList; //Outside waiting humans
    private ArrayList<Human> leavingRefugeList = new ArrayList<>(); //Inside waiting humans (already in a group)
    private ArrayList<Human> waitingForGroupList = new ArrayList<>(); //Inside waiting for a group

    private Human humanCrossing = null;
    private JTextPane crossingTextPane;

    private JTextPane leavingRefugeOrWaitingTextPane;

    private Style waitingForGroupStyle;
    private Style alreadyInGroupStyle;

    private CyclicBarrier barrier;

    private Lock arraysLock = new ReentrantLock(true);
    private Condition groupCondition = arraysLock.newCondition();

    private Lock crossingLock = new ReentrantLock(true);
    private Condition humansEnteringRefugeCondition = crossingLock.newCondition();
    
    private ReportingAtomicInteger humansInTunnel;

    /**
     * Tunnel constructor
     * 
     * This constructor initializes the tunnel with the given parameters.
     * 
     * It also creates the necessary styles.
     * 
     * In addition, it defines a lambda for the CyclicBarrier, which is triggered when 3 humans are awaiting the barrier.
     * @param id The id of the tunnel (for the ReportingAtomicInteger)
     * @param waitingInTextPane // TextPane for the humans waiting to enter the refuge
     * @param crossingTextPane // TextPane for the humans crossing the tunnel
     * @param waitingOutTextPane // TextPane for the humans waiting to form a group or leave the refuge
     * @param hub // ConnHub instance for the ReportingAtomicInteger
     */
    public Tunnel(int id, JTextPane waitingInTextPane, JTextPane crossingTextPane, JTextPane waitingOutTextPane, ConnHub hub) {
        this.enteringRefugeList = new GraphicArrayList<>(waitingOutTextPane, Direction.VERTICAL);
        this.crossingTextPane = crossingTextPane;
        this.leavingRefugeOrWaitingTextPane = waitingInTextPane;

        this.waitingForGroupStyle = waitingInTextPane.addStyle("WaitingForGroup", null);
        StyleConstants.setForeground(waitingForGroupStyle, java.awt.Color.BLACK);
        this.alreadyInGroupStyle = waitingInTextPane.addStyle("AlreadyInGroup", null);
        StyleConstants.setForeground(alreadyInGroupStyle, java.awt.Color.GREEN);

        this.barrier = new CyclicBarrier(3, () -> {
            arraysLock.lock(); // Locking the arraysLock to ensure that the leavingRefugeList is updated correctly
            for (int i = 0; i < 3; i++) {
                // Remove the first 3 humans from the waitingForGroupList and add them to the leavingRefugeList
                leavingRefugeList.add(waitingForGroupList.get(0));
                waitingForGroupList.remove(0);
                // Signal the groupCondition to wake up any waiting humans
                groupCondition.signal();
            }
            // Update the GUI with the new lists
            updateTunnelLeavingGui();
            
            arraysLock.unlock();
        });

        this.humansInTunnel = new ReportingAtomicInteger(hub, "Tunnel"+Integer.toString(id));
    }

    /**
     * enterTunnel
     * 
     * This method is called by a human when it enters the tunnel. It handles all the logic for crossing the tunnel.
     * @param h The human that is crossing the tunnel
     * @param isEnteringRefuge The direction of the crossing (true if entering the refuge, false if leaving the refuge)
     */
    public void enterTunnel(Human h, boolean isEnteringRefuge) {
        humansInTunnel.incrementAndReport();
        try{
            if(isEnteringRefuge){ // If the human is entering the refuge, it is added to the enteringRefugeList
                synchronized(this.enteringRefugeList){
                    enteringRefugeList.add(h);
                }
            }else{
                arraysLock.lock(); // Lock the arrays related to leaving the refuge

                waitingForGroupList.add(h);
                updateTunnelLeavingGui();

                // Since the lock must be dropped before entering the barrier, another thread may cut in line.
                // In order to avoid this, the thread must wait until it is one of the first 3 in the list.
                while(waitingForGroupList.indexOf(h) > 2){
                    groupCondition.await();
                }
                arraysLock.unlock();

                barrier.await();

            }
            // At this point, the human is either in the enteringRefugeList or in the leavingRefugeList
            // And it is ready to cross whenever the tunnel is free
            crossingLock.lock();
            if(isEnteringRefuge){ // If the human is entering the refuge, it is removed from the enteringRefugeList
                synchronized(enteringRefugeList){
                    enteringRefugeList.remove(h);
                }
            }else{ // If the human is leaving the refuge
                while(!enteringRefugeList.isEmpty()){ // If there are humans entering the refuge, they have priority
                    humansEnteringRefugeCondition.await();
                }

                // Remove the human from the leavingRefugeList and update the GUI
                arraysLock.lock(); // Lock the arrays related to leaving the refuge
                leavingRefugeList.remove(h);
                updateTunnelLeavingGui();
                arraysLock.unlock();
            }
            humanCrossing = h;
            crossingTextPane.setText(humanCrossing.getEntityId()+(isEnteringRefuge ? " (<-)" : " (->)"));

            humanCrossing.sleep(1000);

            crossingTextPane.setText("");
            humanCrossing = null;
            humansEnteringRefugeCondition.signal(); // Signal the humans waiting to leave the refuge
            
            
        }catch(InterruptedException | BrokenBarrierException e){
            e.printStackTrace();
        }finally{
            humansInTunnel.decrementAndReport();
            crossingLock.unlock();
        }
        
    }

    /**
     * updateTunnelLeavingGui
     * 
     * This method is used to update the GUI with the current state of the leavingRefugeList and waitingForGroupList.
     * 
     * This is really similar to the updateGui found at Riskzone
     * @see Riskzone
     * 
     * In this case, the leavingRefugeList is displayed on topin green and the waitingForGroupList is displayed in black.
     */
    private void updateTunnelLeavingGui(){
        try{
            // Humans already in a group
            StringBuilder sbLeavingRefuge = new StringBuilder();
            for (Human h : leavingRefugeList) {
                sbLeavingRefuge.append(h.getEntityId()).append("\n");
            }

            // Humans waiting for a group
            StringBuilder sbWaitingForGroup = new StringBuilder(); 
            for (Human h : waitingForGroupList) {
                sbWaitingForGroup.append(h.getEntityId()).append("\n");
            }

            
            StyledDocument doc = leavingRefugeOrWaitingTextPane.getStyledDocument();
            leavingRefugeOrWaitingTextPane.setText(""); // Clear the text pane before updating it to avoid getLength() issues

            // Insert the text for the humans already in a group
            doc.insertString(doc.getLength(), sbLeavingRefuge.toString(), alreadyInGroupStyle);
            // Insert the text for the humans waiting for a group
            doc.insertString(doc.getLength(), sbWaitingForGroup.toString(), waitingForGroupStyle);
        }catch(BadLocationException e){
            e.printStackTrace();
            leavingRefugeOrWaitingTextPane.setText("Error updating GUI: " + e.getMessage());
        }
    }

}
