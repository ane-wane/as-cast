package fr.stack.partitioners;



public interface IPartitioner {

    public MonitorMessages getMonitor();

    public double getBestDistance();

    public long getBestPartition();
}
