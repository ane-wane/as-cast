package fr.stack.peersampling;



public interface IMessage {

    public int fromID();

    public void setID(int id);
    
    public int getCounter();

    public void setCounter(int counter);
}
