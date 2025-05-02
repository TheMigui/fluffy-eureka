package poo.cal;


import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextPane;



public class Refuge  {
    private AtomicInteger food = new AtomicInteger(0);
    private GraphicArrayList<Human> commonArea;
    private GraphicArrayList<Human> restArea;
    private GraphicArrayList<Human> diningArea;
    private JTextPane foodTextPane;
    public Refuge(JTextPane commonTArea, JTextPane diningTArea, JTextPane restTArea, JTextPane foodTextPane){ 
        
        this.commonArea = new GraphicArrayList<>(commonTArea);
        this.restArea = new GraphicArrayList<>(restTArea);
        this.diningArea = new GraphicArrayList<>(diningTArea);
        this.foodTextPane = foodTextPane;
        this.foodTextPane.setText("0");
        
    }
    private void enteringProcedure(GraphicArrayList<Human> list, Human h, boolean isEntering){
        if(isEntering){
            list.add(h);
        }else{
            list.remove(h);
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

    public void dropFood(int food){
        synchronized(this.food){
            this.food.set(this.food.get() + food);
            foodTextPane.setText(Integer.toString(this.food.get()));
            this.food.notifyAll();
        }
    }
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
