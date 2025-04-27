package poo.cal;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class GraphicArrayList<T> extends ArrayList<T> {

    private JTextArea textArea;
    public enum Direction{HORIZONTAL, VERTICAL};
    private Direction direction;
    
    public GraphicArrayList(JTextArea textArea, Direction direction) {
        super();
        this.textArea = textArea;
        this.direction = direction;
    }
    public GraphicArrayList(JTextArea textArea) {
        this(textArea, Direction.HORIZONTAL);
    }

    @Override
    public boolean add(T element) {
        boolean result = super.add(element);
        updateTextArea();
        return result;
    }

    @Override
    public boolean remove(Object element) {
        boolean result = super.remove(element);
        updateTextArea();
        return result;
    }

    private void updateTextArea() {
        StringBuilder sb = new StringBuilder();
        for (T element : this) {
            sb.append(element.toString()).append(direction == Direction.HORIZONTAL ? " " : "\n");
        }
        textArea.setText(sb.toString());
    }
}