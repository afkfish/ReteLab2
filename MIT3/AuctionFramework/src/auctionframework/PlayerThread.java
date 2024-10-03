package auctionframework;

/**
 * Auto-play thread of the simulation.
 */
public class PlayerThread extends Thread {
    // The thread runs as long as it is enabled to do so
    private boolean enabled = true;
    
    
    /**
     * Run the thread.
     */
    public void run() {
        // Terminate once the thread is disabled.
        while (enabled) {
            if (!AuctionFramework.stepAuction()) {
                // Once there are no more steps the main loop should terminate
                AuctionFramework.gui.stopAutoPlay();
                break;
            }
            
            // Sleep based on the GUI setting
            try {
                Thread.sleep(101-AuctionFramework.gui.getSpeedSliderValue()+50);
            } catch (Exception e) {
                // Do nothing
            }
        }
    }
       
    /**
     * Disable the thread, thus the main loop in the run() function will quit.
     */
    public void stopThread() {
        enabled = false;
    }
    
}
