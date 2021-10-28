package org.example.pojo;

public class Data {
    private long userTime;
    private long systemTime;
    private long childUserTime;
    private long childSystemTime;
    private long startTime;
    private long priority;
    private long nice;
    private long noOfThreads;
    private long resident;
    private long dataAndStack;
    private double memUsage;
    private double cpuUsage;

    public void setUserTime(long userTime){
        this.userTime = userTime;
    }

    public long getUserTime() {
        return userTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setChildUserTime(long childUserTime) {
        this.childUserTime = childUserTime;
    }

    public long getChildUserTime() {
        return childUserTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setChildSystemTime(long childSystemTime) {
        this.childSystemTime = childSystemTime;
    }

    public long getChildSystemTime() {
        return childSystemTime;
    }


    public void setPriority(long priority) {
        this.priority = priority;
    }

    public long getPriority() {
        return priority;
    }
    public void setNice(long nice) {
        this.nice = nice;
    }

    public long getNice() {
        return nice;
    }

    public void setNoOfThreads(long noOfThreads) {
        this.noOfThreads = noOfThreads;
    }

    public long getNoOfThreads() {
        return noOfThreads;
    }

    public void setResident(long resident) {
        this.resident = resident;
    }

    public long getResident() {
        return resident;
    }

    public void setDataAndStack(long dataAndStack) {
        this.dataAndStack = dataAndStack;
    }

    public long getDataAndStack() {
        return dataAndStack;
    }

    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    @Override
    public String toString() {
        return "Data{" +
                "userTime=" + userTime +
                ", systemTime=" + systemTime +
                ", childUserTime=" + childUserTime +
                ", childSystemTime=" + childSystemTime +
                ", startTime=" + startTime +
                ", priority=" + priority +
                ", nice=" + nice +
                ", noOfThreads=" + noOfThreads +
                ", resident=" + resident +
                ", dataAndStack=" + dataAndStack +
                ", memUsage=" + memUsage +
                ", cpuUsage=" + cpuUsage +
                '}';
    }
}
