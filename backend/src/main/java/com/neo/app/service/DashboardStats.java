package com.neo.app.service;

import java.util.Map;

public class DashboardStats {
    private long totalLogs;
    private long logsLast24h;
    private long failedLogsLast24h;
    private long activeUsersLast24h;
    private Map<String, Long> mostFrequentActions;
    private Map<String, Long> mostAccessedResourceTypes;

    // Getters et Setters
    public long getTotalLogs() { return totalLogs; }
    public void setTotalLogs(long totalLogs) { this.totalLogs = totalLogs; }

    public long getLogsLast24h() { return logsLast24h; }
    public void setLogsLast24h(long logsLast24h) { this.logsLast24h = logsLast24h; }

    public long getFailedLogsLast24h() { return failedLogsLast24h; }
    public void setFailedLogsLast24h(long failedLogsLast24h) { this.failedLogsLast24h = failedLogsLast24h; }

    public long getActiveUsersLast24h() { return activeUsersLast24h; }
    public void setActiveUsersLast24h(long activeUsersLast24h) { this.activeUsersLast24h = activeUsersLast24h; }

    public Map<String, Long> getMostFrequentActions() { return mostFrequentActions; }
    public void setMostFrequentActions(Map<String, Long> mostFrequentActions) { this.mostFrequentActions = mostFrequentActions; }

    public Map<String, Long> getMostAccessedResourceTypes() { return mostAccessedResourceTypes; }
    public void setMostAccessedResourceTypes(Map<String, Long> mostAccessedResourceTypes) { this.mostAccessedResourceTypes = mostAccessedResourceTypes; }
}

