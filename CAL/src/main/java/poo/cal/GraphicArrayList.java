package poo.cal;
import java.util.ArrayList;
import javax.swing.JTextPane;

public class GraphicArrayList<T> extends ArrayList<T> {

    private JTextPane textPane;
    public enum Direction{HORIZONTAL, VERTICAL};
    private Direction direction;
    
    public GraphicArrayList(JTextPane textPane, Direction direction) {
        super();
        this.textPane = textPane;
        this.direction = direction;
    }
    public GraphicArrayList(JTextPane TextPane) {
        this(TextPane, Direction.HORIZONTAL);
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

    private synchronized void updateTextPane() {
        StringBuilder sb = new StringBuilder();
        for (T element : this) {
            sb.append(element.toString()).append(direction == Direction.HORIZONTAL ? " " : "\n");
        }
        String finalResult = sb.toString();
        textPane.setText(finalResult);
    }
}