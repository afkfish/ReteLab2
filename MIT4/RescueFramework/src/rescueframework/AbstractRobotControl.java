package rescueframework;

import world.Robot;
import world.RobotPercepcion;

/**
 * Abstract class for numbering the robot control objects
 */
public abstract class AbstractRobotControl {
    // ID of the next robot 
    static int nextInstanceID = 0;
    
    // ID of the current robot
    protected int instanceId;
    
    // Percepcion of the agent
    protected RobotPercepcion percepcion;
    
    // The robot object in the world
    protected Robot robot;
    
    
    /**
     * Default constructor 
     * 
     * @param robot         The robot object in the world
     * @param percepcion    The percepcion of the robots of the world
     */
    public AbstractRobotControl(Robot robot, RobotPercepcion percepcion) {
        // Save the next instance ID for the current robot
        instanceId = nextInstanceID;
        
        // Increase instance ID for the next robot
        nextInstanceID++;
        
        // Save percepcion and robot objects
        this.percepcion = percepcion;
        this.robot = robot;
    }
    
    /**
     * Abstract step method to determine the moving direction of the robot
     * 
     * @return  Returns null to stay in place or 0-3 as a moving direction
     */
    public abstract Integer step();
}
