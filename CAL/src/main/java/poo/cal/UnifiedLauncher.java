package poo.cal;
import poo.cal_client.CAL_client;
public class UnifiedLauncher {
    public static void main(String[] args) {
        try {
            
            Process serverProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal.CAL")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) 
                .redirectError(ProcessBuilder.Redirect.INHERIT) 
                .start();


            int howManyClients = 1;
            Process clients[] = new Process[howManyClients];
            for (int i = 0; i < howManyClients; i++){
                Process clientProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal_client.CAL_client")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) 
                .redirectError(ProcessBuilder.Redirect.INHERIT) 
                .start();
                clients[i] = clientProcess;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                serverProcess.destroy();
                for (int i = 0; i < howManyClients; i++){
                    clients[i].destroy();
                }
            }));
            serverProcess.waitFor();
            for (int i = 0; i < howManyClients; i++){
                clients[i].waitFor();
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
