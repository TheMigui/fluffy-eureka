package poo.cal;
import java.util.ArrayList;
import javax.swing.JTextPane;

public class GraphicArrayList<T> extends ArrayList<T> {

    /*
     * GraphicArrayList
     * 
     * This class is just a wrapper for ArrayList, but it updates a JTextPane every time an element is added or removed.
     * It is used to display the contents of the ArrayList in a JTextPane.
     */

    private JTextPane textPane;
    public enum Direction{HORIZONTAL, VERTICAL}; // Direction of the text in the JTextPane
                                                 // HORIZONTAL = "a b c"
                                                 // VERTICAL = "a\nb\nc\n"
    private Direction direction;
    
    public GraphicArrayList(JTextPane textPane, Direction direction) {
        super();
        this.textPane = textPane;
        this.direction = direction;
    }

    public GraphicArrayList(JTextPane TextPane) {
        this(TextPane, Direction.HORIZONTAL); // Default direction is HORIZONTAL
    }

    @Override
    public boolean add(T element) {
        boolean result = super.add(element);
        updateTextPane();
        return result;
    }

    @Override
    public boolean remove(Object element) {
        boolean result = super.remove(element);
        updateTextPane();
        return result;
    }

    /**
        updateTextPane
        
        This method updates the JTextPane with the contents of the ArrayList.

        It is called every time an element is added or removed from the ArrayList.

        It is a synchronized method to ensure that only one thread can update the JTextPane at a time.
     */
    private synchronized void updateTextPane() {
        StringBuilder sb = new StringBuilder();
        for (T element : this) {
            sb.append(element.toString()).append(direction == Direction.HORIZONTAL ? " " : "\n");
        }
        String finalResult = sb.toString();
        textPane.setText(finalResult);
    }
}