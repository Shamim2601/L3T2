import java.util.Comparator;

public class SlotComparator implements Comparator<Course>{

    @Override
    public int compare(Course c1, Course c2) {
        return c1.getTime_slot()-c2.getTime_slot();
    }
    
}
