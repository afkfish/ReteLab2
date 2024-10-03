package world;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Path object representing a path between two cells
 */
public class Path {
    // The cells building up the path
    private ArrayList<Cell> path = new ArrayList<>();
    // Display color of the path
    private Color color = Color.MAGENTA;

    /**
     * Return the color setting of the path
     * @return          The color setting of the path
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color setting of the path
     * @param color     The new color of the path
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /** 
     * Return cells building up the path
     * @return          Cells on the path
     */
    public ArrayList<Cell> getPath() {
        return path;
    }
    
    /**
     * Return the first cell along the path
     * @return          The first cell along the path
     */
    public Cell getFirstCell() {
        if (path.size()>1) return path.get(1);
        else return null;
    }
    
    /**
     * Return the start cell of the path
     * @return          The start cell of the path
     */
    public Cell getStartCell() {
        if (path.size()>0) return path.get(0);
        else return null;
    }
    
    /**
     * Add new cell to the end of the path
     * @param cell      The cell to add
     */
    public void addLastCell(Cell cell) {
        path.add(cell);
    }
    
    /**
     * Add new cell to the beginning of the cell
     * @param cell      The new cell to add
     */
    public void addFirstCell(Cell cell) {
        path.add(0, cell);
    }
    
    /**
     * Return the length of the path
     * @return          The length of the path
     */
    public int getLength() {
        return path.size();
    }
}
