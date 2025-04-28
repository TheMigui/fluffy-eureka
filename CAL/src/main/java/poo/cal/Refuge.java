package poo.cal;


import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextArea;

public class Refuge  {
    private AtomicInteger food = new AtomicInteger(0);
    private GraphicArrayList<Human> commonArea;
    private GraphicArrayList<Human> restArea;
    private GraphicArrayList<Human> diningArea;

    public Refuge(JTextArea commonTArea, JTextArea restTArea, JTextArea diningTArea) {
        this.commonArea = new GraphicArrayList<>(commonTArea);
        this.restArea = new GraphicArrayList<>(restTArea);
        this.diningArea = new GraphicArrayList<>(diningTArea);
    }
    private void enteringProcedure(GraphicArrayList<Human> list, Human h, boolean isEntering){
        //Para cuando un humano entra o sale de una zona
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
            notifyAll();
        }
    }
    public void eat(){
        synchronized(this.food){
            try{
                while(this.food.get() < 1){
                    wait();
                }
                this.food.decrementAndGet();
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        }
    }

}
