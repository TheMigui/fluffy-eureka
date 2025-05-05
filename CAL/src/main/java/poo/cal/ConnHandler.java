package poo.cal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnHandler implements Runnable{

    /*
     * ConnHandler
     * 
     * This class handles the connection with 1 client. It is responsible for sending statistics and receiving pause/resume requests from the client.
     */

    private ConnHub hub;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private AtomicBoolean keepAlive = new AtomicBoolean(true);
    private String clientAddr;



    /**
     * Constructor for ConnHandler.
     * It initializes the socket and the input/output streams, in order to prepare for communication with the client.
     * It also sets the client address for logging purposes.
     * 
     * @param socket The socket that will be used for communication with the client.
     * @param hub The ConnHub instance that will be used to manage the connection with the client (pause/resume requests will be sent to the hub).
     */
    public ConnHandler(Socket socket, ConnHub hub) {
        this.hub = hub;
        this.socket = socket;
        this.clientAddr = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * run
     * 
     * This method is called when the thread is started. It listens for messages from the client and handles them accordingly.
     */
    @Override
    public void run(){
        try{
            while(keepAlive.get()){
                String message = in.readUTF();
                if(message.equals("EXIT")){ // The client is closing the connection. 
                    hub.deleteConn(this); // Delete the connection from the hub, so statistics won't be sent to this client anymore.
                    keepAlive.set(false); // Set the keepAlive flag to false, so the thread will exit.
                }
                else if (message.equals("PAUSE")){ // The client is pausing the simulation.
                    hub.closeGL(clientAddr);
                }else if (message.equals("RESUME")){ // The client is resuming the simulation.
                    hub.openGL(clientAddr);
                }
            }
        } catch (IOException e) {
            if(keepAlive.get()){ //It means that the connection was closed by the client, so we don't want to print the stack trace.
                e.printStackTrace();
                keepAlive.set(false);
            }
        } finally { //When the thread is exiting, we want to close the connection properly.
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) { 
                if (!e.getMessage().contains("Socket closed")) { //Maybe the socket was already closed by the client, so we don't want to print the stack trace.
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void sendStat(String statName, String value) {
        try {
            out.writeUTF(statName + "|" + value);
        } catch (Exception e) {
            if(keepAlive.get()){
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendSimulationStatus(boolean isActive){
        try {
            if (isActive) {
                out.writeUTF("RESUME");
            } else {
                out.writeUTF("PAUSE");
            }
        } catch (Exception e) {
            if(keepAlive.get()){
                e.printStackTrace();
            }
        }
    }

    public synchronized void closeConn() {
        
        try {
            out.writeUTF("EXIT");
            in.close();
            out.close();
            keepAlive.set(false);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
