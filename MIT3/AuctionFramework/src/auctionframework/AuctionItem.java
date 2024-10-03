package auctionframework;

import java.awt.Color;
import javax.swing.JPanel;

/**
 * An item to be sold on the auction.
 */
public class AuctionItem {
    // Category of the item
    private final String category;
    
    // Name of the item
    private final String name;
    
    // Starting price of the item
    private final int startingPrice;
    
    // The GUI panel of the item
    private final JPanel itemPanel;
    
    // The (future) owner of the item
    private AbstractAgent owner = null;
    
    // The actual price of the item
    private int price = 0;
    
    /**
     * Create new AuctionItem.
     * 
     * @param category              Category of the new item
     * @param name                  Name of the new item
     * @param startingPrice         Starting price of the new item
     */
    public AuctionItem(String category, String name, int startingPrice) {
        // Save item properties
        this.category = category;
        this.name = name;
        this.startingPrice = startingPrice;
        
        // Create GUI panel for the item
        itemPanel = new JPanel();
        itemPanel.setBackground(Color.BLUE);
        itemPanel.setSize(30, 30);
        itemPanel.setToolTipText("<html>"+getName()+"<br>Starting price: "+getStartingPrice());
        AuctionFramework.gui.getOfferedItemsPanel().add(itemPanel);
    }

    /**
     * Return the actual price of the item.
     * 
     * @return              The actual price of the item
     */
    public int getPrice() {
        return price;
    }
    
    /**
     * Set the actual price of the item.
     * 
     * @param price         The actual price of the item
     */
    public void setPrice(int price) {
        this.price = price;
    }
    
    /**
     * Return the actual utility of the item.
     * 
     * @return              The actual utility of the item
     */
    public float getUtility() {
        if (startingPrice == 0) return 0;
        return ((float)price-(float)(startingPrice))/(float)(startingPrice);
    }
    
    /**
     * Return the category of the item.
     * 
     * @return              The category of the item
     */
    public String getCategory() {
        return category;
    }

    /**
     * Return the name of the item.
     * 
     * @return              The name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Return the starting price of the item.
     * 
     * @return              The starting price of the item
     */
    public int getStartingPrice() {
        return startingPrice;
    }
 
    /**
     * Return the owner of the item.
     * 
     * @return              The owner of the item
     */
    public AbstractAgent getOwner() {
        return owner;
    }
    
    /**
     * Set the owner of the item.
     * 
     * @param owner         The owner of the item
     */
    public void setOwner(AbstractAgent owner) {
        this.owner = owner;
    }

    /**
     * Set the status of the item: 0 = gray (no agent is interested); 1 = red 
     * (the agent is selected for bidding); 2 = green (the item is sold)
     * 
     * @param status        The status of the item
     */
    public void setStatus(int status) {
        switch (status) {
            case 0:
                itemPanel.setBackground(Color.gray);
                break;
            case 1:
                itemPanel.setBackground(Color.red);
                break;
            case 2:
                itemPanel.setBackground(Color.green);
                break;
            default:
                break;
        }
    }
}