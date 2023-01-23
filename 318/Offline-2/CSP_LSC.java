import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CSP_LSC {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter input file name(e.g. d-10-01): ");
        String filename = scanner.nextLine() + ".txt";
        System.out.print("Enter #VAH{1/2/3/4/5} : ");
        int vah = scanner.nextInt();
        System.out.print("Enter solver {0 = bt/1 = fc} : ");
        int solverChoice = scanner.nextInt();
        int N = 3;
        int[][] initialBoard = new int[N][N];

        File inpfile = new File("data/" + filename);
        if(!inpfile.exists()){
            System.out.println("Error! Input file does not exist.");
        }

        Scanner fr;
        try {
            fr = new Scanner(inpfile);
            String firstLine = fr.nextLine();
            firstLine = firstLine.replace(";", "");
            N = Integer.parseInt(firstLine.split("=")[1]);
            initialBoard = new int[N][N];
            fr.nextLine();
            fr.nextLine();

            int i = 0;
            while(fr.hasNextLine()){
                String line = fr.nextLine();
                line = line.replace("|", "").replace("]", "").replace(";", "").replace(" ", "");
                for(int j=0;j<N;j++){
                    initialBoard[i][j] = Integer.parseInt(line.split(",")[j]);
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
        }       
        scanner.close();
        //taking input done...

        //solving for given choice of solver and vah
        LS_Board lsBoard = new LS_Board(initialBoard);
        if(lsBoard.Solve(solverChoice, vah)){
            System.out.println("\nSolved! #Nodes = "+ lsBoard.Nodes + "\t#BT = "+ lsBoard.bt);
            lsBoard.printCurrent();
        }
        else{
            System.out.println("Could not solve...");
        }
        
    }
}