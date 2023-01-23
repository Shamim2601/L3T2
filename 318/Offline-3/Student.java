import java.util.ArrayList;
import java.util.Collections;

public class Student {
    private ArrayList<Course> coursesEnrolled;

    public Student() {
        coursesEnrolled = new ArrayList<>();
    }

    public void addCourse(Course course) {
        coursesEnrolled.add(course);
    }

    public Course[] getEnrolledCourses() {
        Course[] enrolledCourses = new Course[this.coursesEnrolled.size()];
        for(int i=0; i<coursesEnrolled.size(); i++) {
            enrolledCourses[i] = this.coursesEnrolled.get(i);
        }
        return enrolledCourses;
    }

    public double getPenalty(int strategy) {
        double penalty = 0;
        Collections.sort(coursesEnrolled, new SlotComparator());

        for(int i=0; i< (coursesEnrolled.size()-1); i++) {
            for(int j=i+1; j<coursesEnrolled.size(); j++) {
                int n = coursesEnrolled.get(j).getTime_slot() - coursesEnrolled.get(i).getTime_slot();
                if(n <= 5) {
                    if(strategy==1){
                        penalty += Math.pow(2, 5-n);
                    }
                    else{
                        penalty += 2* (5-n);
                    }
                }
            }
        }
        return penalty;
    }
    
}
