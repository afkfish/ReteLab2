package auctionframework;

import java.util.ArrayList;

/**
 * AbstractAgent is the base class for developing custom agents participating
 * in auctions.
 * 
 */
public class AbstractAgent {

    // Name of the agent
    protected String name = "Unnamed agent";

    // The auction the agent is participating in
    protected Auction auction;
    
    // The amount of money the agent has
    private int money;
    
    // Items owned by the agent
    private ArrayList<AuctionItem> items = new ArrayList<AuctionItem>();
    
    // Total starting price of the items owner by the agent
    private int totalItemStartingPrice = 0;
    
    // Total buying prcie of the items owned by the agent
    private int totalItemBuyingPrice = 0;
    
    // The DisplayPanel object on the GUI displaying the state of the agent
    private ParticipantPanel displayPanel;
    
    // The state of the agent (0 = out of money; 1 = not bidding; 2 = bidding)
    private int state = 0;
    
    /**
     * Default constructor.
     * 
     * @param name          Name of the agent
     * @param auction       The auction the agent is participating in
     * @param money         The amount of money the agent has
     */
    public AbstractAgent(String name, Auction auction, int money) {
        this.name = name;
        this.auction = auction;
        this.money = money;
        
        // Create new DisplayPanel for the agent
        displayPanel = new ParticipantPanel(auction,this);
    }
    
   
    /**
     * Display log message on the GUI console.
     * 
     * @param message          The message to display.
     */
    public void log(String message) {
        AuctionFramework.log(name, message);
    }
    
    /**
     * Ask the agent about bidding for an itme
     * 
     * @param item              The item the agent is asked about
     * @return                  True if the agent bids for the current price.
     */
    public boolean ask(AuctionItem item) {
        return false;
    }
       
    /**
     * After winning the bidding the agent pays for the item and registers it
     * as its own.
     * 
     * @param item              The item the agent wins the bidding for.
     */
    public void addItem(AuctionItem item) {
        items.add(item);
        money -= item.getPrice();
        totalItemStartingPrice += item.getStartingPrice();
        totalItemBuyingPrice += item.getPrice();
        item.setOwner(this);
    }
    
    /**
     * Update the DisplayPanel belonging to the agent on the GUI.
     */
    public void updateGui() {
        displayPanel.updatePanel();
    }
    
    /**
     * Returns the name of the agent.
     * 
     * @return                  Name of the agent.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the amount of money the agent has.
     * 
     * @return                  The amount of money the agent has.
     */
    public int getMoney() {
        return money;
    }
    
    /**
     * Returns the state of the agent.
     * 
     * @return                  0 = not bidding; 1 = bidding; -1 = out of money.
     */
    public int getState() {
        return state;
    }
    
    /**
     * Returns an ArrayList of the items the agent owns.
     * 
     * @return                  An ArrayList of the items the agent owns.
     */
    public ArrayList<AuctionItem> getItems() {
        return items;
    }
    
    /**
     * Returns the DisplayPanel belonging to the agent.
     * 
     * @return                  The DisplayPanel belonging to the agent.
     */
    public ParticipantPanel getDisplayPanel() {
        return displayPanel;
    }
    
    /**
     * Sets the state of the agent
     * 
     * @param state             0 = not bidding; 1 = bidding; -1 = out of money.
     */
    public void setState(int state) {
        this.state = state;
    }
   
    /**
     * Returns the summed starting price of all the items the agent owns.
     * 
     * @return                  Summed starting price of all the items.
     */
    public int getTotalStartingPrice() {
        return totalItemStartingPrice;
    }
    
    /**
     * Returns the summed buying price of all the items the agent owns.
     * 
     * @return                  Summed buying price of all the items.
     */
    public int getTotalBuyingPrice() {
        return totalItemBuyingPrice;
    }
    
    /**
     * Returns the total efficiency of the agent.
     * 
     * @return                  Total efficiency of the agent.
     */
    public int getTotalEfficiency() {
        if (totalItemBuyingPrice == 0) return 0;
        return (int)Math.round(((float)totalItemStartingPrice/(float)totalItemBuyingPrice*100));
    }  
}
