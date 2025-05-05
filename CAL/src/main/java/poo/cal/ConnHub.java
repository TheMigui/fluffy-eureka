package poo.cal;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnHub extends Thread{
    
    private GlobalLock gl;
    private ApocalypseLogger logger;
    private ArrayList<ConnHandler> connHandlers = new ArrayList<>();
    private ExecutorService connectionHandlers= Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private HashMap<String, ReportingAtomicInteger> stats = new HashMap<>();
    private ZombieRanking zombieRanking;

    public ConnHub(GlobalLock gl, ApocalypseLogger logger) {
        this.gl = gl;
        this.gl.setConnHub(this);
        this.logger = logger;
        try{
            serverSocket = new ServerSocket(5050);
        }catch (Exception e){
            logger.log("Error creating server socket: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllConnections();
        }));
    }

    @Override
    public void run(){
        while(true){
            try{
                ConnHandler connHandler = new ConnHandler(serverSocket.accept(), this);
                synchronized (connHandlers) {
                    connHandlers.add(connHandler);
                }
                connectionHandlers.execute(connHandler);
                connHandler.sendSimulationStatus(this.gl.isOpen());
                synchronized (stats) {
                    for (ReportingAtomicInteger stat : stats.values()) {
                        connHandler.sendStat(stat.getStatName(), Integer.toString(stat.get()));
                    }
                }
                connHandler.sendStat(zombieRanking.getStatName(), zombieRanking.getRanking());
            }catch (Exception e){
                logger.log("Error accepting connection: " + e.getMessage());
            }
        }
    }

    public void addStat(ReportingAtomicInteger stat) {
        synchronized (stats) {
            stats.put(stat.getStatName(), stat);
        }
    }
    public void addZombieRanking(ZombieRanking ranking) {
        this.zombieRanking = ranking;
    }

    public void closeGL(String addr){
        gl.close("by " + addr);
    }
    public void openGL(String addr){
        gl.open("by " + addr);
    }
    public void deleteConn(ConnHandler connHandler){
        synchronized (connHandlers) {
            connHandlers.remove(connHandler);
        }
    }

    public void updateStat(String statName, int value) {
        this.updateStat(statName, Integer.toString(value));
    }

    public synchronized void updateStat(String statName, String value) {
        synchronized (connHandlers) {
            for (ConnHandler connHandler : connHandlers) {
                connHandler.sendStat(statName, value);
            }
        }
    }

    public synchronized void updateSimulationStatus(boolean isActive) {
        synchronized (connHandlers) {
            for (ConnHandler connHandler : connHandlers) {
                connHandler.sendSimulationStatus(isActive);
            }
        }
    }


    public synchronized void closeAllConnections() {
        for (ConnHandler connHandler : connHandlers) {
            connHandler.closeConn();
        }
        connectionHandlers.shutdownNow();
        try {
            serverSocket.close();
        } catch (Exception e) {
            logger.log("Error closing server socket: " + e.getMessage());
        }
    }
}
