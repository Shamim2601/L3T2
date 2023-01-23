import java.util.Arrays;

public class LS_Board {
    int[][] initialBoard;
    int[][] currentBoard;
    int N, Nodes, bt;

    public LS_Board(int[][] init){
        N = init.length;
        initialBoard = new int[N][N];
        initialBoard = init;
        currentBoard = new int[N][N];
        currentBoard = init;
        Nodes = 0;
        bt = 0;
    }

    public void printCurrent(){
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                System.out.print(initialBoard[i][j]+"\t");
            }
            System.out.println();
        }
    }

    public boolean Solve(int solver, int vah) throws Exception{
        //done for solver = 0
        if(isComplete()) return true;

        int[] minEntry = new int[2]; //row,col
        int[] domain = new int[N+1];
        if (vah == 1) {
            minEntry = findVariableWithVAH1();
            for(int k=0;k<N;k++){
                if(currentBoard[minEntry[0]][k] != 0){
                    domain[currentBoard[minEntry[0]][k]] = -1;
                }

                if(currentBoard[k][minEntry[1]] != 0){
                    domain[currentBoard[k][minEntry[1]]] = -1;
                }
            }
            for(int m=0;m<=N;m++){
                if(domain[m]==0) domain[m] = m;
                //System.out.println(domain[m]);
            }

            // System.out.println(minEntry[0]+","+minEntry[1]);

            // System.exit(0);

        } else if (vah == 2) {
            minEntry = findVariableWithVAH2();
            for(int k=1;k<=N;k++){
                domain[k-1] = k;
            }
        } else if (vah == 3) {
            minEntry = findVariableWithVAH3();
            for(int k=1;k<=N;k++){
                domain[k-1] = k;
            }
        } else if (vah == 4) {
            minEntry = findVariableWithVAH4();
            for(int k=1;k<=N;k++){
                domain[k-1] = k;
            }
        }else if (vah == 5) {
            minEntry = findVariableWithVAH5();
            for(int k=1;k<=N;k++){
                domain[k-1] = k;
            }
        }
        
        Nodes += 1;
        
        if (Nodes == 40000) {
            System.out.println("checked 1000 nodes...\nCurrent square configuration:");
            printCurrent();
            System.out.println("Exiting...");
            System.exit(0);
        }
        
        for (int domainChoice : domain) {
            
            if(domainChoice!=-1 && domainChoice!=0){
                setVariableInBoard(minEntry[0], minEntry[1], domainChoice);
                //printCurrent();
                //System.exit(0);
            }
            else{
                continue;
            }
            
            if (Solve(solver, vah)) {
                return true;
            } else {
                resetVariableInBoard(minEntry[0], minEntry[1]);
                bt += 1;
            }
        }
        
        return false;
    }

    private int[] findVariableWithVAH1() {
        int minrow = 0, mincol = 0;
        int minDomainlength = 999;
        for(int i=0;i<N; i++){
            for(int j=0;j<N;j++){
                if(currentBoard[i][j]==0 && getDomLength(i,j)<minDomainlength){
                    minDomainlength = getDomLength(i,j);
                    minrow = i;
                    mincol = j;
                }
            }
        }
        int[] minEntry = {minrow, mincol};
        return minEntry;
    }

    private int getDomLength(int i, int j) {
        int[] domain = new int[N+1];
        Arrays.fill(domain, -1);
        int len = 0;
        for(int k=0;k<N;k++){
            domain[currentBoard[i][k]] = currentBoard[i][k];
            domain[currentBoard[k][j]] = currentBoard[k][j];
            //if(currentBoard[k][j]!=0) len--;
        }

    
        for(int m=1;m<=N;m++){
            if(domain[m]!=-1) len++;
        }

        return len;
    }

    private int[] findVariableWithVAH2() {
        int[] minEntry = {0,0};
        return minEntry;
    }

    private int[] findVariableWithVAH3() {
        int[] minEntry = {0,0};
        return minEntry;
    }

    private int[] findVariableWithVAH4() {
        int[] minEntry = {0,0};
        return minEntry;
    }

    private int[] findVariableWithVAH5() {
        int[] minEntry = {0,0};
        return minEntry;
    }

    private void setVariableInBoard(int i, int j, int domainChoice) {
        currentBoard[i][j] = domainChoice;

        //System.out.println("Setting "+domainChoice+" at {"+i+","+j+"}");
    }

    private void resetVariableInBoard(int i, int j) {
        currentBoard[i][j] = 0;
    }

    public boolean isComplete() {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (currentBoard[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }


    
}
