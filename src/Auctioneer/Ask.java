package Auctioneer;


public class Ask implements Comparable  {

	public String agentName;
	public int agentID;
	public double price;
	public double amount;
	
	public Ask(){
		this.agentName = "Default";
		this.agentID = 0;
		this.price = Integer.MIN_VALUE;
		this.amount = 0;
	}
	
	public Ask(String name, int id, double price, double amount){
		this.agentName = name;
		this.agentID = id;
		this.price = price;
		this.amount = amount;
	}
	
	@Override
	public int compareTo(Object o) {
		if(this.price > ((Ask)o).price)
			return 1;
		else if(this.price < ((Ask)o).price)
			return -1;
		else 
			return 0;
	}
	
	@Override
    public String toString() {
        return "[ agentName=" + this.agentName + ", id=" + agentID + ", price=" + price + ", amount=" + amount + "]";
    }
}
