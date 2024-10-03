package world;

import rescueagents.RobotControl;
import rescueframework.AbstractRobotControl;
import rescueframework.RescueFramework;

/**
 * Robot on the map
 */
public class Robot {
    // Location of the robot
    private Cell location;
    
    // The injured being carried by the robot
    private Injured injured = null;
    
    // The control object of th robot
    private AbstractRobotControl control;
    
    
    /**
     * Default constructor
     * 
     * @param startCell     The start cell of the robot
     * @param percepcion    Percepcion of the robot
     */
    public Robot(Cell startCell, RobotPercepcion percepcion) {
        location = startCell;
        control = new RobotControl(this, percepcion);
    }
    
    /**
     * Return the robot location
     * @return      The robot location
     */
    public Cell getLocation() {
        return location;
    }
    
    /**
     * Set the location of the robot
     * @param newLocation       The new location of the robot
     */
    public void setCell(Cell newLocation) {
        location = newLocation;
    }
    
    /**
     * Return true if the robot is currently carrying an injured
     * 
     * @return      True if the robot is carrying an injured
     */
    public boolean hasInjured() {
        return injured != null;
    }
    
    /**
     * Pick up an injured if the robot does not carries any
     */
    public void pickupInjured() {
        if (injured == null) {
            injured = location.getInjured();
            RescueFramework.log("Picking up injured: "+injured.toString());
            location.setInjured(null);
        } else {
            RescueFramework.log("Unable to pick up inured: already has one.");
        }
    }
    
    /**
     * Drop the injured if the robot carries one
     * 
     * @return      Return the injured that is dropped
     */
    public Injured dropInjured() {
        RescueFramework.log("Dropping injured "+injured.toString()+" at "+location.toString());
        Injured result = injured;
        injured = null;
        return result;
    }
    
    /**
     * Return the injured being carried by the robot
     * 
     * @return  The injured being carried by the robot
     */
    public Injured getInjured() {
        return injured;
    }
    
    /**
     * Call the AbstractRobotControl to decide the next step of the robot
     * 
     * @return      Stepping direction of the robot or NULL to stay in place
     */
    public Integer step() {
        if (control == null) return null;
        return control.step();
    }
}
