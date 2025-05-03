package poo.cal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnHandler implements Runnable{
    private ConnHub hub;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private AtomicBoolean keepAlive = new AtomicBoolean(true);
    private String clientAddr;

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

    @Override
    public void run(){
        try{
            while(keepAlive.get()){
                String message = in.readUTF();
                if(message.equals("EXIT")){
                    hub.deleteConn(this);
                    keepAlive.set(false);
                }
                else if (message.equals("PAUSE")){
                    hub.closeGL(clientAddr);
                }else if (message.equals("RESUME")){
                    hub.openGL(clientAddr);
                }
            }
        } catch (IOException e) {
            if(keepAlive.get()){
                e.printStackTrace();
                keepAlive.set(false);
            }
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                if (!e.getMessage().contains("Socket closed")) {
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
