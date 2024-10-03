package auctionframework;

/**
 * Execute a GUI update to be run on the AWT event handler thread.
 */
public class AuctionFrameworkGUIUpdate implements Runnable {
    // The active auction as datasource
    private Auction auction;
    
    // The GUI to update
    private AuctionFrameworkGUI gui;
    
    /**
     * Initialize the object with the auction and the GUI.
     * 
     * @param auction       The active auction as datasource
     * @param gui           The GUI to update
     */
    public AuctionFrameworkGUIUpdate(Auction auction, AuctionFrameworkGUI gui) {
        this.auction = auction;
        this.gui = gui;
        
    }
    
    /**
     * Update the active AuctionItem and all agent panels on the GUI.
     */
    public void run() {
        gui.setItem(auction.getActiveItem());
        for (int i=0; i<auction.participants.size(); i++) {
            auction.participants.get(i).updateGui();
        }
        gui.revalidate();
    }
    
}
