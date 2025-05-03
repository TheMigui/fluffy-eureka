package poo.cal;


public class GlobalLock {
    private boolean isOpen = true;
    private ApocalypseLogger logger;
    public void setApocalypseLogger(ApocalypseLogger logger) {
        this.logger = logger;
    }
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
    public synchronized void setOpen(boolean isOpen, String responsible) {
        logger.log("GL has been " + (isOpen ? "opened" : "closed") + " " + responsible, true);
        this.isOpen = isOpen;
        if(isOpen){
            this.notifyAll();
        }
    }
    public void open(String responsible) {
        this.setOpen(true, responsible);
    }

    public void close(String responsible) {
        this.setOpen(false, responsible);
    }
    
}
