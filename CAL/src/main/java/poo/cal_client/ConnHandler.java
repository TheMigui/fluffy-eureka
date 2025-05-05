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
    private String serverAddr;
    private int serverPort;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    
    private JButton connectButton;
    private JTextField connStatusTF;
    private JTextField simulationStatusTF;
    private JButton pauseButton;

    private HashMap<String, JTextComponent> statFields;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ConnHandler(String serverAddr, int serverPort, JButton connectButton, JTextField connStatusTF, JTextField simulationStatusTF, JButton pauseButton,HashMap<String, JTextComponent> statFields) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.connectButton = connectButton;
        this.connStatusTF = connStatusTF;
        this.simulationStatusTF = simulationStatusTF;
        this.pauseButton = pauseButton;
        this.statFields = statFields;
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
    @Override
    public void run(){
        try{
            socket = new Socket(serverAddr, serverPort);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            connectButton.setEnabled(true);
            connectButton.setText("Disconnect");
            connStatusTF.setText("Connected");
            synchronized(pauseButton){
                pauseButton.setEnabled(true);
            }
        }catch(IOException e){
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.isConnected.set(false);
            connStatusTF.setText("Disconnected");
            connectButton.setText("Connect");
            return;
        }
        try{
            while (isConnected.get()){
                String message = in.readUTF();
                
                if(message.equals("EXIT")){
                    isConnected.set(false);
                    JOptionPane.showMessageDialog(null, "Connection closed by server", "Info", JOptionPane.INFORMATION_MESSAGE);
                }else if(message.equals("PAUSE")){
                    synchronized(pauseButton){
                        simulationStatusTF.setText("Paused");
                        pauseButton.setText("Resume");
                    }

                }else if(message.equals("RESUME")){
                    synchronized(pauseButton){
                        simulationStatusTF.setText("Running");
                        pauseButton.setText("Pause");
                    }
                }else{
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
            if(isConnected.get()){
                JOptionPane.showMessageDialog(null, "Connection lost: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }finally{
            try {
                in.close();
                out.close();
                socket.close();

            } catch (IOException e) {
                if (!e.getMessage().contains("Socket closed")) {
                    e.printStackTrace();
                }
            }finally{
                connStatusTF.setText("Disconnected");
                connectButton.setText("Connect");
                synchronized(pauseButton){
                    simulationStatusTF.setText("---");
                    pauseButton.setEnabled(false);
                }
            }
        }
    }

    public void toggleConnection(){
        if (isConnected.get()){
            try {
                out.writeUTF("EXIT");
                out.flush();
                isConnected.set(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            isConnected.set(true);
            connectButton.setEnabled(false);
            (new Thread(this)).start();
        }
    }

    public void togglePause(){
        if (isConnected.get()){
            try {
                out.writeUTF(pauseButton.getText().toUpperCase());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
