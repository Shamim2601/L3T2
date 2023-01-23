import java.util.ArrayList;

public class Course {
    private String ID;
    private int enrolledTotal;
    private int time_slot;
    private ArrayList<Course> overlappedCourses;

    private int conflict;
    private int Status;  // 0(not explored), 1(in stack), 2(visited)

    public Course(String id, int total_enroll) {
        this.ID = id;
        this.enrolledTotal = total_enroll;
        time_slot = -1;
        overlappedCourses = new ArrayList<>();
    }

    public void addOverlappingCourse(Course course) {
        overlappedCourses.add(course);
        return ;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public int getEnrolledTotal() {
        return enrolledTotal;
    }

    public void setEnrolledTotal(int enrolledTotal) {
        this.enrolledTotal = enrolledTotal;
    }

    public int getTime_slot() {
        return time_slot;
    }

    public void setTime_slot(int time_slot) {
        this.time_slot = time_slot;
    }

    public Course[] getOverlappedCourses() {
        Course[] ovCourses = new Course[overlappedCourses.size()];
        for(int i=0;i<overlappedCourses.size(); i++){
            ovCourses[i] = overlappedCourses.get(i);
        }
        return ovCourses;
    }

    public int getNumberOfOverlap(){
        return overlappedCourses.size();
    }

    public void setOverlappedCourses(ArrayList<Course> overlappedCourses) {
        this.overlappedCourses = overlappedCourses;
    }

    public int getConflict() {
        return conflict;
    }

    public void setConflict(int conflict) {
        this.conflict = conflict;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @Override
    public String toString() {
        return ID+" "+time_slot;
    }
    
}
