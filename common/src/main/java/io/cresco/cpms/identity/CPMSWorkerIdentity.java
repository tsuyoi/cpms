package io.cresco.cpms.identity;

public class CPMSWorkerIdentity {
    private final String region;
    private final String agent;
    private final String plugin;

    public CPMSWorkerIdentity(String region, String agent, String plugin) {
        this.region = region;
        this.agent = agent;
        this.plugin = plugin;
    }

    public String getRegion() {
        return region;
    }

    public String getAgent() {
        return agent;
    }

    public String getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Worker (");
        if (getRegion() != null)
            sb.append(String.format("R:%s", getRegion()));
        if (getAgent() != null)
            sb.append(String.format(",A:%s", getAgent()));
        if (getPlugin() != null)
            sb.append(String.format(",P:%s", getPlugin()));
        sb.append(")");
        return sb.toString();
    }
}
