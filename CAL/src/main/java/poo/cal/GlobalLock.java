package poo.cal;


public class GlobalLock {
    private boolean isOpen = true;

    public synchronized void check(){
        while(!isOpen){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public synchronized boolean isOpen() {
        return isOpen;
    }
    public synchronized void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
        if(isOpen){
            this.notifyAll();
        }
    }
    public void open() {
        this.setOpen(true);
    }

    public void close() {
        this.setOpen(false);
    }
    
}
