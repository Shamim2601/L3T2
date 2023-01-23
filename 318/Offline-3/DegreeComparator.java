import java.util.Comparator;

public class DegreeComparator implements Comparator<Course>{

    @Override
    public int compare(Course c1, Course c2) {
        return c2.getNumberOfOverlap() - c1.getNumberOfOverlap();
    }
    
}

