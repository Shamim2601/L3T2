import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter input fileName(e.g: car-s-91): ");
        String inpFile = scanner.nextLine();
        System.out.print("Enter penalty strategy(1. Exponential  2. Linear) : ");
        int strategy = scanner.nextInt();
        System.out.print("Constructive heuristics:\n");
        System.out.println("1. LargestDegree\n2. SaturationDegree\n3. Largest enrollment\n4. Random Ordering");
        System.out.print("Enter choice: ");
        int consHeuristic = scanner.nextInt();
        System.out.print("How many iteration for Perturbative Heuristic(e.g: 2000): ");
        int iteration = scanner.nextInt();

        //System.out.println("file: "+ inpFile + "\nstrategy: "+strategy+ "\nheuristic: "+consHeuristic);

        if((strategy==1||strategy==2) && (consHeuristic>0 && consHeuristic<5)){
            runLocalSearch(inpFile, strategy, consHeuristic, iteration);
        }else{
            System.out.println("Invalid input. Exiting....");
            System.exit(0);
        }
        

        scanner.close();
    }

    private static void runLocalSearch(String inpFile, int strategy, int consHeuristic, int iteration) throws IOException {
        ArrayList<Course> courses = new ArrayList<>();
        ArrayList<Student> students = new ArrayList<>();
        Scanner scn;

        scn = new Scanner(new File("Toronto-dataset/"+ inpFile +".crs"));

        while(scn.hasNextLine()) {
            String[] temp = scn.nextLine().split(" ");
            courses.add(new Course(temp[0], Integer.parseInt(temp[1])));
        }
        scn.close();

        int[][] CourseConflicts = new int[courses.size()][courses.size()];
        for(int i=0; i<CourseConflicts.length; i++) {
            for(int j=0; j<CourseConflicts[0].length; j++) {
                CourseConflicts[i][j] = 0;
            }
        }

        scn = new Scanner(new File("Toronto-dataset/"+ inpFile +".stu"));
        int currentStudent = 0;
        while(scn.hasNextLine()) {
            String[] tempString = scn.nextLine().split(" ");
            int[] tempInteger = new int[tempString.length];
            for(int i=0; i<tempInteger.length; i++) {
                if(!tempString[i].equalsIgnoreCase("")){ tempInteger[i] = Integer.parseInt(tempString[i]); }
            }

            students.add(new Student());
            for(int i=0; i<tempInteger.length; i++) {
                if(tempInteger[i]!=0)students.get(currentStudent).addCourse(courses.get(tempInteger[i]-1));
            }
            currentStudent++;

            for(int i=0; i<tempInteger.length-1; i++) {
                for(int j=i+1; j<tempInteger.length; j++) {
                    if(CourseConflicts[tempInteger[i]-1][tempInteger[j]-1] == 0) {
                        CourseConflicts[tempInteger[i]-1][tempInteger[j]-1] = CourseConflicts[tempInteger[j]-1][tempInteger[i]-1] = 1;
                    }
                }
            }
        }
        scn.close();

        for(int i=0; i<CourseConflicts.length; i++) {
            for(int j=0; j<CourseConflicts[0].length; j++) {
                if(CourseConflicts[i][j] == 1) {
                    courses.get(i).addOverlappingCourse(courses.get(j));
                }
            }
        }

        int totalSlots = scheduleTimeTable(courses, consHeuristic);
        double avgPenalty = calcPenalty(students, strategy);

        System.out.println("\nAfter Largest Degree....");
        System.out.println("Time slots = "+totalSlots+"\tAverage Penalty = "+avgPenalty+"\n");

        reducePenaltyByKempeChain(courses, students, strategy, iteration);
        System.out.println("After Kempe Chain....");
        System.out.println("Average Penalty = "+calcPenalty(students, strategy)+ "\n");

        reducePenaltyByPairSwap(courses, students, strategy, iteration);
        System.out.println("After Pair Swap....");
        System.out.println("Average Penalty = "+calcPenalty(students, strategy)+ "\n");

        System.out.println("Writing day wise exam schedule to .sol file...");
        File outputDir = new File("Output");
        if(!outputDir.exists()){
            outputDir.mkdir();
        }
        File outputFile = new File("Output/"+ inpFile +".sol");
        if(outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();

        FileWriter fileWriter = new FileWriter(outputFile);
        Collections.sort(courses, new SlotComparator());
        for(int i=0; i<courses.size(); i++) {
            fileWriter.append(courses.get(i).toString()+"\n");
        }
        fileWriter.close();
    }

    private static int scheduleTimeTable(ArrayList<Course> courses, int heuristic) {
        int totalSlots = -1; 
        if(heuristic==1){
            totalSlots = largestDegree(courses);
        }else if(heuristic==2){
            totalSlots = SaturationDegree(courses);
        }else if(heuristic==3){
            totalSlots = largestEnrollment(courses);
        }else{
            totalSlots = randomOrdering(courses);
        }

        return totalSlots;
    }

    private static int largestDegree(ArrayList<Course> courses) {
        Collections.sort(courses, new DegreeComparator());
        return calcSlots(courses);
    }
    
    private static int SaturationDegree(ArrayList<Course> courses) {
        Collections.sort(courses, new DegreeComparator());
        courses.get(0).setTime_slot(0);

        int totalSlots = 1;
        for(int i=1; i<courses.size(); i++) {
            HashSet<Integer> temp, selected=null;
            int maxSatDegree=-1, maxIndex=-1;

            for(int j=0; j<courses.size(); j++) {
                if(courses.get(j).getTime_slot() == -1) {
                    temp = new HashSet<>();

                    Course[] overlappingCourses = courses.get(j).getOverlappedCourses();
                    for(int k=0; k<overlappingCourses.length; k++) {
                        if(overlappingCourses[k].getTime_slot() != -1) {
                            temp.add(overlappingCourses[k].getTime_slot());
                        }
                    }

                    if(temp.size()>maxSatDegree || (temp.size()==maxSatDegree && courses.get(j).getNumberOfOverlap()>courses.get(maxIndex).getNumberOfOverlap())) {
                        maxSatDegree = temp.size();
                        maxIndex = j;
                        selected = temp;
                    }
                }
            }

            maxSatDegree = 0;
            while(courses.get(maxIndex).getTime_slot() == -1) {
                if(!selected.contains(maxSatDegree)) {
                    courses.get(maxIndex).setTime_slot(maxSatDegree);
                    if(maxSatDegree == totalSlots) {
                        totalSlots++;
                    }
                } else {
                    maxSatDegree++;
                }
            }
        }
        return totalSlots;
    }

    private static int largestEnrollment(ArrayList<Course> courses) {
        Collections.sort(courses, new EnrollmentComparator());
        return calcSlots(courses);
    }

    private static int randomOrdering(ArrayList<Course> courses) {
        Collections.shuffle(courses);
        return calcSlots(courses);
    }

    private static int calcSlots(ArrayList<Course> courses) {
        int totalSlots = 0;

        for(int i=0; i<courses.size(); i++) {
            Course[] overlappingCourses = courses.get(i).getOverlappedCourses();
            int[] slotOccupied = new int[overlappingCourses.length];
            for(int j=0; j<slotOccupied.length; j++) {
                slotOccupied[j] = overlappingCourses[j].getTime_slot();
            }
            Arrays.sort(slotOccupied);

            int suitableSlot = 0;
            for(int j=0; j<slotOccupied.length; j++) {
                if(slotOccupied[j] != -1) {
                    if(suitableSlot == slotOccupied[j]) {
                        suitableSlot++;
                    }
                    if(suitableSlot < slotOccupied[j]) {
                        courses.get(i).setTime_slot(suitableSlot);
                    }
                }
            }
            if(courses.get(i).getTime_slot() == -1) {
                if(suitableSlot == totalSlots) {
                    courses.get(i).setTime_slot(totalSlots++);
                } else {
                    courses.get(i).setTime_slot(suitableSlot);
                }
            }
        }
        return totalSlots;
    }

    private static double calcPenalty(ArrayList<Student> students, int strategy) {
        double avgPenalty = 0;
        for(int i=0; i<students.size(); i++) {
            avgPenalty += students.get(i).getPenalty(strategy);
        }
        avgPenalty = avgPenalty/students.size();
        return avgPenalty;
    }

    private static void reducePenaltyByKempeChain(ArrayList<Course> courses, ArrayList<Student> students, int strategy, int iteration) {
        Random rand = new Random();

        for(int i=0;i<iteration;i++){
            int current = rand.nextInt(courses.size());
            Course[] overlappingCourses = courses.get(current).getOverlappedCourses();
            if(overlappingCourses.length != 0) {
                doKempeChain(courses, students, courses.get(current), overlappingCourses[rand.nextInt(overlappingCourses.length)].getTime_slot(), strategy);
            }
        }
    }

    private static void doKempeChain(ArrayList<Course> courses, ArrayList<Student> students, Course currentCourse, int neighbourTimeSlot, int strategy) {
        dfs(currentCourse, neighbourTimeSlot);

        double currentPenalty = calcPenalty(students, strategy);
        int currentTimeSlot = currentCourse.getTime_slot();

        for(int i=0; i<courses.size(); i++) {
            if(courses.get(i).getStatus()==2) {
                if(courses.get(i).getTime_slot() == currentTimeSlot) {
                    courses.get(i).setTime_slot(neighbourTimeSlot);
                } else {
                    courses.get(i).setTime_slot(currentTimeSlot);
                }
            }
        }

        if(currentPenalty <= calcPenalty(students, strategy)) { //need to undo
            for(int i=0; i<courses.size(); i++) {
                if(courses.get(i).getStatus()==2) {
                    if(courses.get(i).getTime_slot() == currentTimeSlot) {
                        courses.get(i).setTime_slot(neighbourTimeSlot);
                    } else {
                        courses.get(i).setTime_slot(currentTimeSlot);
                    }
                }
            }
        }

        for(int i=0; i<courses.size(); i++) {
            if(courses.get(i).getStatus()==2) {
                courses.get(i).setStatus(0);
            }
        }
    }

    private static void dfs(Course currentCourse, int neighbourTimeSlot) {
        currentCourse.setStatus(1);
        Course[] overlappingCourses = currentCourse.getOverlappedCourses();
        for(int i=0; i<overlappingCourses.length; i++) {
            if(overlappingCourses[i].getStatus()==0 && overlappingCourses[i].getTime_slot()==neighbourTimeSlot) {
                dfs(overlappingCourses[i], currentCourse.getTime_slot());
            }
        }
        currentCourse.setStatus(2);
    }

    private static void reducePenaltyByPairSwap(ArrayList<Course> courses, ArrayList<Student> students,int strategy, int iteration) {
        Random rand = new Random();

        for(int i=0;i<iteration;i++){
            doPairSwap(students, courses.get(rand.nextInt(courses.size())), courses.get(rand.nextInt(courses.size())), strategy);
        }
    }

    private static void doPairSwap(ArrayList<Student> students, Course course1, Course course2, int strategy) {
        int timeSlot1 = course1.getTime_slot();
        int timeSlot2 = course2.getTime_slot();

        if(timeSlot1 == timeSlot2) return;

        Course[] overlappingCourses = course1.getOverlappedCourses();
        for(int i=0; i<overlappingCourses.length; i++) {
            if(overlappingCourses[i].getTime_slot() == timeSlot2) {
                return ;
            }
        }

        overlappingCourses = course2.getOverlappedCourses();
        for(int i=0; i<overlappingCourses.length; i++) {
            if(overlappingCourses[i].getTime_slot() == timeSlot1) {
                return ;
            }
        }

        double currentPenalty = calcPenalty(students, strategy);
        course1.setTime_slot(timeSlot2);
        course2.setTime_slot(timeSlot1);

        if(currentPenalty <= calcPenalty(students, strategy)) { //need to undo
            course1.setTime_slot(timeSlot1);
            course2.setTime_slot(timeSlot2);
        }
    }
}