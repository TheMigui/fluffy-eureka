package poo.cal;


import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextPane;



public class Refuge  {
    /*
     * Refuge
     * 
     * This class represents the refuge where humans can go to rest, stay safe, and eat.
     * 
     * It has three areas: common area, rest area, and dining area, represented by GraphicArrayLists 
     * (which update the JTextPanes)
     * 
     * It also keeps track of the amount of food available in the refuge, and displays it in a JTextPane.
     * 
     * In addition, it keeps track of the number of humans in the refuge, and sends updates to the clients through the ConnHub.
     */
    private AtomicInteger food = new AtomicInteger(0);
    private GraphicArrayList<Human> commonArea;
    private GraphicArrayList<Human> restArea;
    private GraphicArrayList<Human> diningArea;
    private JTextPane foodTextPane;
    private ReportingAtomicInteger humansInRefuge;

    public Refuge(JTextPane commonTArea, JTextPane diningTArea, JTextPane restTArea, JTextPane foodTextPane, ConnHub hub) { 
        
        this.commonArea = new GraphicArrayList<>(commonTArea);
        this.restArea = new GraphicArrayList<>(restTArea);
        this.diningArea = new GraphicArrayList<>(diningTArea);
        this.foodTextPane = foodTextPane;
        this.foodTextPane.setText("0");
        this.humansInRefuge = new ReportingAtomicInteger(hub,"Refuge");
        
    }

    /**
     * enteringProcedure
     * 
     * This method is used to add or remove a human from one of the areas in the refuge.
     * 
     * GUI updates are handled by the GraphicArrayList class
     * @param list the list to add or remove the human from
     * @param h the human to add or remove
     * @param isEntering true if the human is entering the area, false if the human is leaving
     */
    private void enteringProcedure(GraphicArrayList<Human> list, Human h, boolean isEntering){
        if(isEntering){
            list.add(h);
            humansInRefuge.incrementAndReport();
        }else{
            list.remove(h);
            humansInRefuge.decrementAndReport();
        }
    }
    public synchronized void commonGate(Human h, boolean isEntering){
        enteringProcedure(commonArea, h, isEntering);
    }

    public  synchronized void restGate(Human h, boolean isEntering){
        enteringProcedure(restArea, h, isEntering);
    }

    public synchronized void diningGate(Human h, boolean isEntering){
        enteringProcedure(diningArea, h, isEntering);
    }

    /**
     * dropFood
     * 
     * This method is used to add food to the refuge, and is called by the humans when they arrive at the refuge.
     * @param food
     */
    public void dropFood(int food){
        synchronized(this.food){ 
            this.food.set(this.food.get() + food);
            foodTextPane.setText(Integer.toString(this.food.get()));
            this.food.notifyAll(); // Notify all humans waiting for food that food is available
        }
    }
    
    /**
     * eat
     * 
     * This method is used to eat food in the refuge, and is called by the humans when they are in the dining area.
     * 
     * If there is no food available, the human will wait until food is available using the wait from the food monitor.
     */
    public void eat(){
        synchronized(this.food){
            try{
                while(this.food.get() < 1){
                    this.food.wait();
                }
                this.food.decrementAndGet();
                this.foodTextPane.setText(Integer.toString(this.food.get()));
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        }
    }



}
