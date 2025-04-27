package poo.cal;
import java.time.LocalDateTime;
public class ApocalypseLogger {
    

    public synchronized void log(String content){
        System.out.println(LocalDateTime.now().toString()+": "+content);
    }
}
