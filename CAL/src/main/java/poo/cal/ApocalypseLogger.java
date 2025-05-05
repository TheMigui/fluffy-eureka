package poo.cal;
import java.time.LocalDateTime;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
public class ApocalypseLogger {
    /*
     * AppocalypseLogger is a singleton class that handles logging for the simulation.
     * All threads that wish to log something should call the synchronized log method of this class.
     */
    
    private boolean isOpenToLog; //In order to avoid logging when the simulation is over
    private BufferedWriter writer; 
    private GlobalLock gl;
    /**
     * Constructor for ApocalypseLogger.
     * Creates a new log file in the logs folder with the current date and time.
     * @param gl GlobalLock instance to be used for pausing/resuming (logs won't be written when paused).
     */
    public ApocalypseLogger(GlobalLock gl) {
        this.gl = gl;
        this.gl.setApocalypseLogger(this);

        try {
            // Create logs folder if it doesn't exist
            File logsFolder = new File("logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdir();
            }

            //Create the new log file
            writer = new BufferedWriter(new FileWriter("logs/log-"+LocalDateTime.now().toString().replace(':','-')+".txt", true));
            
            //Add a shutdown hook to properly close the writer when the program is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                this.close();
            }));
            this.isOpenToLog = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * log
     * It checks if the logger is open to log and if so, writes the content to the log file.
     * It is a synchronized method to ensure that only one thread can log at a time,
     *  thus preventing concurrent access issues.
     * 
     * @param content The content to be logged.
     * @param ignoreGL If true, the global lock will not be checked before logging. This is useful for logging pause/resume events.
     */
    public synchronized void log(String content, boolean ignoreGL){
            if(!ignoreGL)
                gl.check(); //If the GlobalLock is closed, the thread will wait to log, unless it's a pause/resume event.
            if(isOpenToLog){
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


    /**
     * close
     * Closes the BufferedWriter and sets the isOpenToLog flag to false.
     * Therefore, all log contents are stored properly before closing the program.
     * This method is called by the shutdown hook when the program is terminated.
     */
    public synchronized void close(){
            try {
                this.isOpenToLog = false;
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
