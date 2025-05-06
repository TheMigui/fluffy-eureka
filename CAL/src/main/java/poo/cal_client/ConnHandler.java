package poo.cal_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class ConnHandler implements Runnable{

    /*
     * ConnHandler (Connection Handler) (not to be confused with the server's ConnHandler)
     * 
     * ConnHandler is a class that handles the connection to the server.
     * 
     * Its purpose is to:
     * - Connect to the server
     * - Receive messages from the server and update the GUI accordingly
     * - Send the client's pause/resume requests to the server
     */
    private String serverAddr = "localhost";
    private int serverPort = 5050;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    
    private JButton connectButton;
    private JTextField connStatusTF;
    private JTextField simulationStatusTF;
    private JButton pauseButton;

    // This is a map that connects stat names to their corresponding JTextComponent in the GUI.
    private HashMap<String, JTextComponent> statFields;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;


    /**
     * ConnHandler constructor
     * 
     * This constructor initializes the ConnHandler instance with the given parameters.
     * 
     * It also adds a shutdown hook to gracefully close the connection when the program is terminated.
     * @param connectButton The button used to connect/disconnect from the server
     * @param connStatusTF The text field used to display the connection status
     * @param simulationStatusTF The text field used to display the simulation status
     * @param pauseButton The button used to pause/resume the simulation. Since it is accessed from other threads, it must be protected
     * @param statFields The map that connects stat names to their corresponding JTextComponent in the GUI
     */
    public ConnHandler(JButton connectButton, JTextField connStatusTF, JTextField simulationStatusTF, JButton pauseButton,HashMap<String, JTextComponent> statFields) {
        this.connectButton = connectButton;
        this.connStatusTF = connStatusTF;
        this.simulationStatusTF = simulationStatusTF;
        this.pauseButton = pauseButton;
        this.statFields = statFields;

        // This shutdown hook is used to gracefully close the connection when the program is terminated,
        // in order to avoid errors
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (isConnected.get()) {
                    out.writeUTF("EXIT");
                    out.flush();

                    in.close();
                    out.close();
                    socket.close();

                    isConnected.set(false);
                }
            } catch (IOException e) {
                if (!e.getMessage().contains("Socket closed")) {
                    e.printStackTrace();
                }
            }
        }));
    }
    

    public synchronized String getServerAddr() {
        return serverAddr;
    }


    public synchronized void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }


    public synchronized int getServerPort() {
        return serverPort;
    }


    public synchronized void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


    @Override

    /**
     * run
     * 
     * This method is called when the thread is started. It opens the connection to the server, sets the input and
     * output streams, and then it stays in a loop, waiting for messages from the server.
     * 
     * It also updates the GUI accordingly and handles connection errors
     */
    public void run(){
        try{
            socket = new Socket(this.getServerAddr(), this.getServerPort());
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // The connection has been successful, update GUI accordingly
            connectButton.setEnabled(true);
            connectButton.setText("Disconnect");

            connStatusTF.setText("Connected");

            synchronized(pauseButton){
                pauseButton.setEnabled(true);
            }
        }catch(IOException e){

            // If there was an error, it gets shown through the GUI and the buttons / textfields are set accordingly
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.isConnected.set(false);
            connStatusTF.setText("Disconnected");
            connectButton.setText("Connect");
            connectButton.setEnabled(true);
            return;
        }
        try{
            while (isConnected.get()){
                String message = in.readUTF(); // Wait for a message from the server
                
                if(message.equals("EXIT")){ // Handles the shutdown of the server
                    isConnected.set(false);
                    JOptionPane.showMessageDialog(null, "Connection closed by server", "Info", JOptionPane.INFORMATION_MESSAGE);
                }else if(message.equals("PAUSE")){ // The simulation has been paused
                    synchronized(pauseButton){
                        simulationStatusTF.setText("Paused");
                        pauseButton.setText("Resume");
                    }

                }else if(message.equals("RESUME")){ // The simulation has been resumed
                    synchronized(pauseButton){
                        simulationStatusTF.setText("Running");
                        pauseButton.setText("Pause");
                    }
                }else{
                    // In all other cases, messages received from the server can be expected to be an instruction
                    // to update the Text Component of one of the statistics

                    // The messages have the following structure: "<Statistic Name>|<Text to be displayed>"
                    String[] parts = message.split("\\|", 2);
                    String statName = parts[0];
                    String value = parts[1];

                    if(statFields.containsKey(statName)){
                        JTextComponent field = statFields.get(statName);
                        field.setText(value);
                    }
                }
            }
        }catch(IOException e){
            // If isConnected is false, it means that the exception was probably caused by the shutdown itself
            if(isConnected.get()){
                JOptionPane.showMessageDialog(null, "Connection lost: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }finally{
            try {
                in.close();
                out.close();
                socket.close();

            } catch (IOException e) {
                // We don't want the stack trace to be printed if the error's cause it that another method has already closed
                // the socket
                if (!e.getMessage().contains("Socket closed")) {
                    e.printStackTrace();
                }
            }finally{

                // Update GUI to reflect that the client is no longer connected to the server
                connStatusTF.setText("Disconnected");
                connectButton.setText("Connect");
                synchronized(pauseButton){
                    simulationStatusTF.setText("---");
                    pauseButton.setEnabled(false);
                }
            }
        }
    }

    /**
     * toggleConnection
     * 
     * This method is called when the connect button is pressed. It toggles the connection to the server,
     * and creates a new thread for receiving messages when needed.
     * 
     */
    public void toggleConnection(){
        if (isConnected.get()){ // This means that the client wishes to disconnect from the server
            try {
                out.writeUTF("EXIT"); // Send exit message to close gracefully
                out.flush(); // Flush the output stream to ensure that the message is sent

                // Shutdown the connection (the run thread will handle the rest)
                isConnected.set(false);
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{ // This means that the client wishes to connect to the server

            // Presume that the server is connected, the run thread will roll back the changes if it fails
            isConnected.set(true);
            connectButton.setEnabled(false);

            // Create and run a new thread to make the connection
            Thread newThread = new Thread(this);
            newThread.setDaemon(true);
            newThread.start();
        }
    }

    /**
     * togglePause
     * 
     * This method is called when the pause button is pressed. It sends a pause/resume request to the server.
     */
    public void togglePause(){
        if (isConnected.get()){ // It should only work if the client is connected to the server
            try {
                synchronized(pauseButton){ // Since the button contains the action command, we need to synchronize it
                    out.writeUTF(pauseButton.getText().toUpperCase());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
