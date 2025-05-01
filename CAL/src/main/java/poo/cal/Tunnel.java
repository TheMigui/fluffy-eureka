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
    private GraphicArrayList<Human> enteringRefugeList; //Outside waiting humans
    private ArrayList<Human> leavingRefugeList = new ArrayList<>(); //Inside waiting humans
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
    private Condition tunnelFullCondition = crossingLock.newCondition();
    


    public Tunnel(JTextPane waitingInTextPane, JTextPane crossingTextPane, JTextPane waitingOutTextPane) {
        this.enteringRefugeList = new GraphicArrayList<>(waitingOutTextPane, Direction.VERTICAL);
        this.crossingTextPane = crossingTextPane;
        this.leavingRefugeOrWaitingTextPane = waitingInTextPane;
        this.waitingForGroupStyle = waitingInTextPane.addStyle("WaitingForGroup", null);
        StyleConstants.setForeground(waitingForGroupStyle, java.awt.Color.BLACK);
        this.alreadyInGroupStyle = waitingInTextPane.addStyle("AlreadyInGroup", null);
        StyleConstants.setForeground(alreadyInGroupStyle, java.awt.Color.GREEN);
        this.barrier = new CyclicBarrier(3, () -> {
            arraysLock.lock();
            for (int i = 0; i < 3; i++) {
                leavingRefugeList.add(waitingForGroupList.get(0));
                waitingForGroupList.remove(0);
            }
            updateTunnelLeavingGui();
            groupCondition.signalAll();
            arraysLock.unlock();
        });
    }

    public void enterTunnel(Human h, boolean isEnteringRefuge) {
        
        try{
            if(isEnteringRefuge){
                synchronized(this.enteringRefugeList){
                    enteringRefugeList.add(h);
                }
            }else{
                arraysLock.lock();

                waitingForGroupList.add(h);
                updateTunnelLeavingGui();
                while(waitingForGroupList.indexOf(h) > 2){
                    groupCondition.await();
                }
                arraysLock.unlock();

                barrier.await();

            }
            crossingLock.lock();
            if(isEnteringRefuge){
                while(humanCrossing != null){
                    tunnelFullCondition.await();
                }
                synchronized(enteringRefugeList){
                    enteringRefugeList.remove(h);
                }
            }else{
                arraysLock.lock();
                groupCondition.signal();
                while(humanCrossing != null || (!isEnteringRefuge && !enteringRefugeList.isEmpty())){
                    arraysLock.unlock();
                    tunnelFullCondition.await();
                    arraysLock.lock();
                }
                leavingRefugeList.remove(h);
                updateTunnelLeavingGui();
                arraysLock.unlock();
            }
            humanCrossing = h;
            crossingTextPane.setText(humanCrossing.getEntityId());
            humanCrossing.sleep(1000);
            crossingTextPane.setText("");
            humanCrossing = null;
            tunnelFullCondition.signal();
            crossingLock.unlock();
            
        }catch(InterruptedException | BrokenBarrierException e){
            e.printStackTrace();
        }
        
    }
    public synchronized void updateTunnelLeavingGui(){
        try{
            leavingRefugeOrWaitingTextPane.setText("");
            StyledDocument doc = leavingRefugeOrWaitingTextPane.getStyledDocument();

            StringBuilder sb = new StringBuilder();
            for (Human h : leavingRefugeList) {
                sb.append(h.getEntityId()).append("\n");
            }
            
            doc.insertString(doc.getLength(), sb.toString(), alreadyInGroupStyle);
    
            sb = new StringBuilder();
            for (Human h : waitingForGroupList) {
                sb.append(h.getEntityId()).append("\n");
            }
            doc.insertString(doc.getLength(), sb.toString(), waitingForGroupStyle);
        }catch(BadLocationException e){
            e.printStackTrace();
            leavingRefugeOrWaitingTextPane.setText("Error updating GUI: " + e.getMessage());
        }
    }
}
