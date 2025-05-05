package poo.cal;

public class UnifiedLauncher {
    /*
     * UnifiedLauncher
     * 
     * This class is used to launch the server and client processes at the same time.
     * 
     * An arbitrary number of clients can be launched by tweaking the howManyClients variable.
     * 
     * All text output is redirected to the console, although only error messages should show up.
     */
    public static void main(String[] args) {
        try {
            
            Process serverProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal.CAL")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) 
                .redirectError(ProcessBuilder.Redirect.INHERIT) 
                .start();


            int howManyClients = 1; // Change this to the number of clients you want to launch

            Process clients[] = new Process[howManyClients];
            for (int i = 0; i < howManyClients; i++){
                Process clientProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal_client.CAL_client")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) 
                .redirectError(ProcessBuilder.Redirect.INHERIT) 
                .start();
                clients[i] = clientProcess;
            }
            // If the UnifiedLauncher is killed, kill the server and clients as well
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                serverProcess.destroy();
                for (int i = 0; i < howManyClients; i++){
                    clients[i].destroy();
                }
            }));

            // Wait for the server and clients to finish, so the output is not lost
            serverProcess.waitFor();
            for (int i = 0; i < howManyClients; i++){
                clients[i].waitFor();
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
