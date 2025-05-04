package poo.cal;
import poo.cal_client.CAL_client;
public class UnifiedLauncher {
    public static void main(String[] args) {
        try {
            // Ejecutar el servidor como un proceso separado
            Process serverProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal.CAL")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) // Redirigir salida estándar
                .redirectError(ProcessBuilder.Redirect.INHERIT) // Redirigir salida de error
                .start();

            // Ejecutar el cliente como otro proceso separado
            int howManyClients = 1;
            Process clients[] = new Process[howManyClients];
            for (int i = 0; i < howManyClients; i++){
                Process clientProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "poo.cal_client.CAL_client")
                .redirectOutput(ProcessBuilder.Redirect.INHERIT) // Redirigir salida estándar
                .redirectError(ProcessBuilder.Redirect.INHERIT) // Redirigir salida de error
                .start();
                clients[i] = clientProcess;
            }
            serverProcess.waitFor();
            for (int i = 0; i < howManyClients; i++){
                clients[i].waitFor();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                serverProcess.destroy();
                for (int i = 0; i < howManyClients; i++){
                    clients[i].destroy();
                }
            }));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
