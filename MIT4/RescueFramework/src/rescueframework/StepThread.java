package rescueframework;

import javax.swing.SwingUtilities;

/**
 * Autostep thread of the simulation
 */
public class StepThread extends Thread{
    // The thread does not change the time while false
    private boolean enabled = false;
    // Time left of the current step
    private int timeLeft = 0;
    // The time of a whole step
    private int timeStep = 20;
    
    /**
     * Main method of the thread
     */
    public void run() {
        // Endless loop
        while (true) {
            // Sleep first
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
            
            // Only change time when enabled
            if (enabled) {
                // Decrease time left
                timeLeft--;
                
                // Initiate repaint if no repaint is in progress
                if (timeLeft<=0 && (!PaintPanel.paintingInProgress)) {
                    PaintPanel.paintingInProgress = true;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RescueFramework.map.stepTime(true);
                        }
                      });
                    timeLeft = timeStep;
                }
            }
        }
    }
    
    /**
     * Enable the step thread
     */
    public void enable() {
        enabled = true;
        timeLeft = 0;
    }
    
    /**
     * Disable the step thread
     */
    public void disable() {
        enabled = false;
    }
    
    /**
     * Returns true if the thread is enabled
     * @return      True if the thread is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Change the time of a single step
     * @param stepTime      The new duration of a single step
     */
    public void setStepTime(int stepTime) {
        this.timeStep = stepTime;
        if (timeLeft>timeStep) timeLeft = timeStep;
    }
}