package poo.cal;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextArea;

public class Refuge  {
    private AtomicInteger food = new AtomicInteger(0);
    private ArrayList<Human> commonArea = new ArrayList<>();
    private ArrayList<Human> restArea = new ArrayList<>();
    private ArrayList<Human> diningArea = new ArrayList<>();

    private void enteringProcedure(ArrayList<Human> list, JTextArea t, Human h, boolean isEntering){
        if(isEntering){
            list.add(h);
        }else{
            list.remove(h);
        }
        //t
    }
    public synchronized void commonGate(Human h, boolean isEntering){
        if (isEntering){
            commonArea.add(h);
        }
        else{
            commonArea.remove(h);
        }
        //grafico
    }

    public  synchronized void restGate(Human h, boolean isEntering){

    }

    public synchronized void diningGate(Human h, boolean isEntering){}

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
