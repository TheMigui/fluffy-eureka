package poo.cal;
import java.time.LocalDateTime;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
public class ApocalypseLogger {
    
    private boolean isOpen;
    private BufferedWriter writer;
    private GlobalLock gl = new GlobalLock();
    public ApocalypseLogger(GlobalLock gl) {
        this.gl = gl;
        try {
            File logsFolder = new File("logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdir();
            }
            writer = new BufferedWriter(new FileWriter("logs/log-"+LocalDateTime.now().toString().replace(':','-')+".txt", true));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                this.close();
            }));
            this.isOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void log(String content, boolean ignoreGL){
            if(!ignoreGL)
                gl.check();
            if(isOpen){
                try{
                    writer.write(LocalDateTime.now().toString()+": "+content+"\n");
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
    }

    public void log(String content){
        this.log(content, false);
    }

    public synchronized void close(){
            try {
                this.isOpen = false;
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
