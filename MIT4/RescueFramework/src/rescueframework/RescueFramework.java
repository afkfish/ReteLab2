package rescueframework;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import world.Map;

/**
 * Main class of the application
 */
public class RescueFramework {
    // The GUI frame
    static MainFrame mainFrame = null;
    // The map the simulator uses
    static Map map = null;

    /**
     * Main method of the application
     * @param args      The command line arguments
     */
    public static void main(String[] args) {
        Settings.load();
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
    
    /**
     * Refresh the GUI
     */
    public static void refresh() {
        if (mainFrame != null)  mainFrame.refresh();
    }
    
    /**
     * Pause the autstep thread
     */
    public static void pause() {
        if (mainFrame != null)  mainFrame.pause();
    }
    
    /**
     * Resume the autstep thread
     */
    public static void resume() {
        if (mainFrame != null)  mainFrame.resume();
    }
    
    /**
     * Log a message with timestamp to the console
     * @param message       The message to log to the console
     */
    public static void log(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        System.out.println("["+timeStamp+"] "+message);
    }
    
}
