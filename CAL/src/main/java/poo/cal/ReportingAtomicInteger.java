package poo.cal;

import java.util.concurrent.atomic.AtomicInteger;

public class ReportingAtomicInteger extends AtomicInteger{
    private ConnHub hub;
    private String statName;

    public ReportingAtomicInteger(ConnHub hub, String statName) {
        super(0);
        this.hub = hub;
        this.statName = statName;
        hub.addStat(this);
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
