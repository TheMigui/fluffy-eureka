package poo.cal;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnHub extends Thread{
    
    private GlobalLock gl; // GlobalLock instance to be used for pausing/resuming requests
    private ApocalypseLogger logger;
    private ArrayList<ConnHandler> connHandlers = new ArrayList<>(); // List of all active connections
    private ExecutorService connectionHandlers= Executors.newCachedThreadPool(); // Thread pool for handling all ConnHandlers
    private ServerSocket serverSocket;
    private HashMap<String, ReportingAtomicInteger> statMap = new HashMap<>(); //Map of all ReportingAtomicIntegers to be sent to
                                                                             // the clients (key = stat name,value = stat value).
                                                                             // Upon initialization, ReportingAtomicIntegers add
                                                                             // themselves to this map.
                                                                             // This is used for the sending the stats
                                                                             // when a client has just connected.
    private ZombieRanking zombieRanking; // This serves the same purpose as statMap
    private AtomicBoolean isAlive = new AtomicBoolean(true); // This is used to check if the ConnHub is alive or not.

    /**
     * ConnHub constructor.
     * 
     * It initializes the GlobalLock and the ApocalypseLogger instances, and opens the server socket on port 5050.
     * 
     * It also adds a shutdown hook to close all connections when the program is terminated.
     * @param gl GlobalLock instance to be used for pausing/resuming requests
     * @param logger ApocalypseLogger instance to be used for logging pause/resume events
     */
    public ConnHub(GlobalLock gl, ApocalypseLogger logger) {
        this.gl = gl;
        this.gl.setConnHub(this); // The GlobalLock needs to know the ConnHub to send pause/resume updates to the clients.
        this.logger = logger;
        try{
            serverSocket = new ServerSocket(5050);
        }catch (Exception e){
            System.err.println(("Error creating server socket: " + e.getMessage()));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllConnections(); // Close all connections when the program is terminated
        }));
    }


    @Override

    /**
     * run
     * 
     * This method is called when the thread is started. It listens for new connections and creates a new ConnHandler for each 
     * connection. It also stores the new connection in a list of active connections, in order to send data later.
     * 
     * Since ConnHandlers are managed by a thread pool, multiple clients can connect at the same time.
     * 
     * Usually, statistics are sent to the client when their value changes. However, when a client connects, all statistics are 
     * polled and sent to the client.
     */
    public void run(){
        while(isAlive.get()){
            try{
                ConnHandler connHandler = new ConnHandler(serverSocket.accept(), this); // The program will wait here until a 
                                                                                        // new connection is made.

                logger.log("New connection from " + connHandler.getClientAddr(), true);
                synchronized (connHandlers) { // Protect the list of connections from concurrent access issues.
                    connHandlers.add(connHandler); // Add the new connection to the list of active connections
                }
                connectionHandlers.execute(connHandler); // Start the ConnHandler thread (for message reception)

                //Send the current simulation status and all statistics to the new client by polling them.
                connHandler.sendSimulationStatus(this.gl.isOpen());
                synchronized (statMap) {  // Protect the statMap from concurrent access issues.
                    for (ReportingAtomicInteger stat : statMap.values()) {
                        connHandler.sendStat(stat.getStatName(), Integer.toString(stat.get()));
                    }
                }
                connHandler.sendStat(zombieRanking.getStatName(), zombieRanking.getRanking());
            }catch (Exception e){
                if (isAlive.get()) { // If the ConnHub is not alive, it means that the server socket was closed and the program 
                                     // is terminating, so we don't want to print the stack trace.
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * addStat
     * 
     * This method is called by each ReportingAtomicInteger instance when it is created.
     * 
     * It adds the ReportingAtomicInteger instance to the statMap.
     * 
     * Usually, statMap is not used, however, there has to be a way of polling the statistics when a client connects.
     * 
     * statmap is synchronized to ensure that only one thread can modify it at a time.
     * @param stat The ReportingAtomicInteger instance to be added to the map.
     */
    public void addStat(ReportingAtomicInteger stat) {
        synchronized (statMap) {
            statMap.put(stat.getStatName(), stat);
        }
    }

    /**
     * addZombieRanking
     * 
     * Basically the same as addStat, but for the ZombieRanking instance.
     * @param ranking The ZombieRanking instance to be added to the ConnHub.
     */
    public void addZombieRanking(ZombieRanking ranking) {
        this.zombieRanking = ranking;
    }

    /**
     * closeGL
     * 
     * This method is called by a ConnHandler which has received a pause request from the client.
     * @param addr The address of the client that sent the pause request.
     */
    public void closeGL(String addr){
        gl.close("by " + addr);
    }

    /**
     * openGL
     * 
     * This method is called by a ConnHandler which has received a resume request from the client.
     * @param addr The address of the client that sent the resume request.
     */
    public void openGL(String addr){
        gl.open("by " + addr);
    }

    /**
     * deleteConn
     * 
     * This method is called by a ConnHandler when it is closed, so it can be removed from the list of active connections.
     * @param connHandler The ConnHandler instance to be removed from the list of active connections.
     */
    public void deleteConn(ConnHandler connHandler){
        synchronized (connHandlers) { // Protect the list of connections from concurrent access issues.
            connHandlers.remove(connHandler);
        }
    }

    /**
     * updateStat
     * This method is called by ReportingAtomicInteger and ZombieRanking instances when their value changes.
     * 
     * It sends the new value to all active connections.
     * @param statName The name of the statistic to be sent. Each name corresponds to a TextComponent in the client's GUI.
     * @param value The value of the statistic to be sent. This is the value that will be displayed in the TextComponent.
     */
    public synchronized void updateStat(String statName, String value) {
        synchronized (connHandlers) { // Protect the list of connections from concurrent access issues.
            for (ConnHandler connHandler : connHandlers) { // Iterate over all active connections
                connHandler.sendStat(statName, value);
            }
        }
    }
    public void updateStat(String statName, int value) {
        this.updateStat(statName, Integer.toString(value));
    }

    /**
     * updateSimulationStatus
     * 
     * This method is called by the GlobalLock instance when it is paused or resumed.
     * It sends the new simulation status to all active connections.
     * @param isActive True if the simulation is active, false if it is paused.
     */
    public synchronized void updateSimulationStatus(boolean isActive) {
        synchronized (connHandlers) { // Protect the list of connections from concurrent access issues.
            for (ConnHandler connHandler : connHandlers) { // Iterate over all active connections
                connHandler.sendSimulationStatus(isActive);
            }
        }
    }

    /**
     * closeAllConnections
     * 
     * This method is called by the shutdown hook when the program is terminated.
     * It closes all active connections and shuts down the thread pool.
     */
    public void closeAllConnections() {
        isAlive.set(false); // Set the ConnHub to not alive, so it will stop accepting new connections.
        synchronized (connHandlers) { // Protect the list of connections from concurrent access issues.
            for (ConnHandler connHandler : connHandlers) { // Iterate over all active connections
                connHandler.closeConn(); // Close each connection
            }
        }
        connectionHandlers.shutdownNow(); // Shut down the thread pool
        try {
            serverSocket.close();
        } catch (Exception e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
}
