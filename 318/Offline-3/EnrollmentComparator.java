import java.util.Comparator;

public class EnrollmentComparator implements Comparator<Course>{
    @Override
    public int compare(Course course1, Course course2) {
        return course2.getEnrolledTotal()-course1.getEnrolledTotal();
    }
}
