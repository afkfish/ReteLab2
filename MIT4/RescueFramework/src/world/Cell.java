package world;

/**
 * Cell object that the map is built up from
 */
public class Cell {    
    // Coordinates of the cell
    private int x,y;
    // Walls in all four directions
    private boolean[] walls = new boolean[4];
    // Image file of the obstacle on the cell
    private String obstacleImage = "";
    // True if the cell is already discovered by the robots
    private boolean discovered = true;
    // The injured on this cell
    private Injured injured = null;
    // True if the robot currently sees this cell
    private boolean robotSees = false;
    // Type of the cell (0 = default; 1 = exit; 2 = start)
    private int cellType = 0;
    // Color code index of the floor
    private int floorColorIndex = -1;
    // True if the cell is a door (stopping flooded floor coloring)
    private boolean door = false;
    // List of neighbour cell is all directions
    Cell layoutNeigbours[] = new Cell[4];
    // List of accessible cells in all direction
    Cell accessNeigbours[] = new Cell[4];

    
    /**
     * Constructor of the cell
     * @param x         X coordinate of the cell on the map
     * @param y         Y coordinate of the cell on the map
     * @param wallCode  Binary representation of the walls around this cell
     */
    public Cell (int x, int y, String wallCode) {
        this.x = x;
        this.y = y;
        int wallCodeInt = -1;
        
        // Determine special cell marks or wall code
        if (wallCode.equals("X")) {
            // Exit cell - type=1
            wallCodeInt = 0;
            cellType = 1;
        } else if (wallCode.equals("S")) {
            // Start cell - type=2
            wallCodeInt = 0;
            cellType = 2;
        } else if (wallCode.equals("_")) {
            // Door cell
            door = true;
            wallCodeInt = 0;
        } else if (wallCode.equals(".")) {
            wallCodeInt = 0;
        } else {
            wallCodeInt = Integer.parseInt(wallCode,16); 
        }
        
        // Set the wall code based on bitmask compare
        walls[0] = (wallCodeInt & 1)>0;
        walls[1] = (wallCodeInt & 2)>0;
        walls[2] = (wallCodeInt & 4)>0;
        walls[3] = (wallCodeInt & 8)>0;
    }
    
    /**
     * Returns true if the cell has wall in the provided direction
     * @param direction     The direction to check
     * @return              True if there is a wall in the selected direction
     */
    public boolean hasWall(int direction) {
        // Normalize direction value
        while (direction<0) direction+=4;
        while (direction>3) direction-=4;
        // Return the wall
        return walls[direction];
    }
    
    /**
     * Return the type code of the cell
     * @return      The type code of the cell 
     */
    public int getCellType() {
        return cellType;
    }
    
    /**
     * Exchange walls between cells next to each other
     * @param top       Top neighbour cell
     * @param right     Right neighbour cell
     * @param bottom    Bottom neighbour cell
     * @param left      Left neighbour cell
     */
    public void shareWalls(Cell top, Cell right, Cell bottom, Cell left) {
        // Save neighbours
        layoutNeigbours[0] = top;
        layoutNeigbours[1] = right;
        layoutNeigbours[2] = bottom;
        layoutNeigbours[3] = left;
        
        // Add local walls to neighbours
        if (walls[0] && top!=null) top.addWall(2);
        if (walls[1] && right!=null) right.addWall(3);
        if (walls[2] && bottom!=null) bottom.addWall(0);
        if (walls[3] && left!=null) left.addWall(1);
    }
    
    /**
     * Determine accessible neighbour cells
     */
    public void updateAccessibleNeighbours() {
        for (int i=0; i<4; i++)
            if (!walls[i]) accessNeigbours[i] = layoutNeigbours[i];
        
    }
    
    /**
     * Add wall to cell
     * @param direction     The direction to add the wall to
     */
    public void addWall(int direction) {
        walls[direction] = true;
    }
    
    /**
     * Return the accessible neighbour if exists
     * @param direction         The direction requested
     * @return                  The neighbour if exists
     */
    public Cell getAccessibleNeigbour(int direction) {
        return accessNeigbours[direction];
    }
    
    /**
     * Return true if the cell is a door
     * @return                  True if the cell is a door 
     */
    public boolean isDoor() {
        return door;
    }
    
    /**
     * Set the floor color index
     * @param colorIndex        The floor color index
     */
    public void setFloorColorIndex(int colorIndex) {
        floorColorIndex = colorIndex;
        if (colorIndex>-1) discovered = false;
    }
    
    /**
     * Returns the floor color index
     * @return                  The floor color index
     */
    public int getFloorColorIndex() {
        return floorColorIndex;
    }
    
    /**
     * Set new obstacle image name
     * @param obstacleImage     The new obstacle image name
     */
    public void setObstacleImage(String obstacleImage) {
        this.obstacleImage = obstacleImage;
    }
    
    /**
     * Return the obstacle image name
     * @return                  The obstacle image name
     */
    public String getObstacleImage() {
        return obstacleImage;
    }
    
    /**
     * Mark the cell as discovered
     */
    public void discover() {
        discovered = true;
    }
    
    /**
     * Return true if the cell is discovered by the robots
     * @return                  True if the cell is alredy discovered
     */
    public boolean isDiscovered() {
        return discovered;
    }
    
    /**
     * Add injured to the cell
     * @param injured           The injured to be put on the cell
     */
    public void setInjured(Injured injured) {
        this.injured = injured;
    }
    
    /**
     * Return the injured on the cell
     * @return                  The injured on the cell
     */
    public Injured getInjured() {
        return injured;
    }
    
    /**
     * Change the robot visibility value of the cell
     * @param newValue          The new value
     */
    public void setRobotVisibility(boolean newValue) {
        // Save the new value
        robotSees = newValue;
        
        // Update the cell and injured discovered status upon discovery
        if (newValue) {
            discovered = true;
            if (injured != null) {
                injured.setDiscovered(true);
            }
        }
    }
    
    /**
     * Return true if a robot actually sees this cell
     * @return                  True if a robot actually sees this cell 
     */
    public boolean robotSeesIt() {
        return robotSees;
    }
    
    /**
     * Returns true if the cell has an obstacle on it
     * @return                  True if the cell has an obstacle on it
     */
    public boolean hasObstacle() {
        return !obstacleImage.isEmpty();
    }
    
    /**
     * Returns true if the cell has an injured on it
     * @return                  True if the cell has an injured on it
     */
    public boolean hasInjured() {
        return injured != null;
    }
    
    /**
     * Returns true if the cell is an exit cell
     * @return                  True if the cell is an exit cell
     */
    public boolean isExit() {
        return cellType == 1;
    }
    
    /**
     * Compare cell equality by checking coordinates
     * @param other             The other cell to compare to
     * @return 
     */
    public boolean equals(Cell other) {
        return other.x == x && other.y == y;
    }
    
    /**
     * Calculate direction from an other cell
     * @param otherCell         The other cell to calculate directions from
     * @return 
     */
    public int directionFrom(Cell otherCell) {
        if (otherCell.y>y) return 0;
        if (otherCell.y<y) return 2;
        if (otherCell.x>x) return 3;
        return 1;
    }
    
    /**
     * Display cell coordinates as string
     * @return                  Cell coordinates as string
     */
    public String toString() {
        return x+"x"+y;
    }
    
    /**
     * Returns the X coordinate of the cell
     * @return                  X coordinate of the cell
     */
    public int getX() {
        return x;
    }
    
    /**
     * Returns the Y coordinate of the cell
     * @return                  Y coordinate of the cell
     */
    public int getY() {
        return y;
    }
}
