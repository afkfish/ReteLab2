package auctionframework;

import javax.swing.SwingUtilities;

/**
 * Main executable class of the framework.
 */
public class AuctionFramework {
    // The current auction the framework runs.
    static Auction auction;
    
    // The GUI of the application.
    static AuctionFrameworkGUI gui;
    
    // Price increase between rounds.
    public static int priceIncreseStep = 100;

    /**
     * Main function of the application
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create and display the GUI
        gui = new AuctionFrameworkGUI();
        gui.setVisible(true);
    }
    
    /**
     * Start a new auction
     * 
     * @param fileName              Auction catalo file name
     * @param agent1                Number of Agent1 participants
     * @param agent2                Number of Agent2 participants
     * @param agent3                Number of Agent3 participants
     * @param moneyMultiplier       The ratio of money each agent has at the 
     *                              beginning compared to the total value of all
     *                              the items offered.
     */
    public static void startAuction(String fileName, int agent1, int agent2, int agent3, float moneyMultiplier) {
        auction = new Auction(fileName, agent1, agent2, agent3, moneyMultiplier);
        AuctionFramework.updateGUI();
    }
    
    /**
     * Move to the next step in the auction.
     * 
     * @return                      True if there are more steps to execute in
     *                              the current auction.
     */
    public static boolean stepAuction() {
        return auction.step();
    }
    
    /**
     * Display log message on the console and on the GUI console too.
     * 
     * @param source                Source of the message displayed between []s
     * @param message               Content of the messages
     */
    public static void log(String source, String message) {
        String line = "["+source+"] "+message;
        gui.addLogLine(line);
        System.out.println(line);
    }
    
    /**
     * Display framwork log message.
     * 
     * @param message               The content of the message.
     */
    public static void log(String message) {
        log("FW",message);
    }
    
    /**
     * Schedule a GUI update executed on the GUI thread.
     */
    public static void updateGUI() {
        try {
            SwingUtilities.invokeLater(new AuctionFrameworkGUIUpdate(auction,gui));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
