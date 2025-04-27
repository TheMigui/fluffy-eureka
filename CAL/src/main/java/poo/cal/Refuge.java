package poo.cal;

import java.util.concurrent.atomic.AtomicInteger;

public class Refuge  {
    AtomicInteger food = new AtomicInteger(0);

    public synchronized void commonGate(Human h, boolean isEntering){

    }

    public  synchronized void restGate(Human h, boolean isEntering){}

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
