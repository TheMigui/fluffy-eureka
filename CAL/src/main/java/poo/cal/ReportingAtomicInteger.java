package poo.cal;

import java.util.concurrent.atomic.AtomicInteger;

public class ReportingAtomicInteger extends AtomicInteger{
    /**
     * ReportingAtomicInteger
     * 
     * This class extends AtomicInteger and adds the ability to report its value to the ConnHub.
     * 
     * Therefore, changes are immediately reported to the ConnHub, which will update the clients.
     * 
     * @see ConnHub
     */
    private ConnHub hub;
    private String statName; // The name of the statistic. This is used by the client to know which text field to update.

    public ReportingAtomicInteger(ConnHub hub, String statName) {
        super(0);
        this.hub = hub;
        this.statName = statName;
        hub.addStat(this); // Register this instance with the ConnHub, so it can be polled for new clients
    }
    


    public int incrementAndReport() {
        int value = super.incrementAndGet();
        hub.updateStat(statName, value);
        return value;
    }

    public int decrementAndReport() {
        int value = super.decrementAndGet();
        hub.updateStat(statName, value);
        return value;
    }

    public String getStatName() {
        return statName;
    }
}
