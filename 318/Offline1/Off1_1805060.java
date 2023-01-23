import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Off1_1805060{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int k;
        System.out.print("Enter grid size, k = ");
        k = scanner.nextInt();

        int[][] boardInitial = new int[k][k];
        ArrayList<Integer> values = new ArrayList<>(k*k);

        System.out.println("Enter initial configuration (0 for blank):");
        int i=0,j=0, blankX = -1, blankY = -1;
        while(scanner.hasNext()){
            int input = scanner.nextInt();

            if(input>=0 && input<(k*k)){
                if(input==0){
                    blankX = i;
                    blankY = j;
                }
                boardInitial[i][j++] = input;
                if(values.contains(input)){
                    System.out.println("Duplicate input!");
                    System.exit(1);
                }
                values.add(input);
            }else{
                System.out.println("Invalid input!");
                System.exit(1);
            }

            if(j==k){
                i++;
                j=0;
            }
            if(i==k)break;
        }

        int[][] boardFinal = new int[k][k];
        int val = 0;
        for(int p=0;p<k;p++){
            for(int q=0;q<k;q++){
                if(p==k-1 && q==k-1) boardFinal[p][q] = 0;
                else boardFinal[p][q] = ++val;
            }
        }

        if(!isSolvable(boardInitial, blankX, blankY)){
            System.out.println("This grid configuration is not solvable.");
        }else{
            int choice;
            System.out.println("This grid configuration is solvable.");
            while(true){
                System.out.print("Choose function [ 1. Hamming  2. Manhattan  0. Exit ]  : ");
                choice = scanner.nextInt();
                if(choice==1){
                    System.out.println("Solution with Hamming distance heuristic:");
                    solvePuzzle(1, boardInitial, boardFinal, blankX, blankY);
                }else if(choice==2){
                    System.out.println("Solution with Manhattan distance heuristic:");
                    solvePuzzle(2, boardInitial, boardFinal, blankX, blankY);
                }else{
                    System.exit(0);
                }
            }
        }

        scanner.close();
    }

    private static class Node{
        int bx, by;
        Node parent;
        int[][] board;
        int cost;
        int moves;
    }

    public static Node makeNode(Node parent, int[][] matrix, int bX, int bY, int newX, int newY, int moves){
        Node node = new Node();
        node.parent = parent;
        int k = matrix.length;
        node.board = new int[k][k];
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++){
                node.board[i][j] = matrix[i][j];
            }
        }

        int t = node.board[bX][bY];
        node.board[bX][bY] = node.board[newX][newY];
        node.board[newX][newY] = t;
        node.bx = newX;
        node.by = newY;

        node.moves = moves;
        node.cost = 999999;

        return node;
    }

    private static void solvePuzzle(int choice, int[][] boardInitial, int[][] boardFinal, int blankX, int blankY) {
        Node root = makeNode(null, boardInitial, blankX, blankY, blankX, blankY, 0);
        root.cost = total_cost(choice, root.board, boardFinal, root.moves);

        PriorityQueue<Node> openList = new PriorityQueue<>(new Compare());
        openList.add(root);
        int exploredNodes = 1;
        int expandedNodes = 0;

        ArrayList<Node> closedList = new ArrayList<>();

        while(!openList.isEmpty()){
            Node minimumNode = openList.peek();
            openList.poll();
            if(!identical(closedList, minimumNode)){
                closedList.add(minimumNode);
                expandedNodes++;
                if(minimumNode.cost==minimumNode.moves){
                    printSolution(minimumNode);
                    System.out.println("Optimal cost = "+minimumNode.cost);
                    System.out.println("Number of explored Nodes = "+exploredNodes);
                    System.out.println("Number of expanded Nodes = "+expandedNodes);
                    System.out.println("----------------------------------------\n");
                    return;
                }

                int[] rowIdx = {1, 0, -1, 0};
                int[] colIdx = {0, -1, 0, 1};
                for(int i=0;i<4;i++){
                    int rowVal = minimumNode.bx + rowIdx[i];
                    int colVal = minimumNode.by + colIdx[i];
                    int k = minimumNode.board.length;
                    if(rowVal>=0 && rowVal<k && colVal>=0 && colVal<k){
                        Node next = makeNode(minimumNode, minimumNode.board, minimumNode.bx, minimumNode.by, rowVal, colVal, minimumNode.moves+1);
                        next.cost = total_cost(choice, next.board, boardFinal, next.moves);

                        if(!identical(closedList, next)){
                            openList.add(next);
                            exploredNodes++;
                            if(exploredNodes>200){
                                System.out.println("Could not reach final configuration!");
                                System.out.println("Number of nodes explored : "+exploredNodes);
                                System.exit(0);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean identical(ArrayList<Node> closedList, Node next) {
        for(Node node : closedList){
            int flag = 1;
            int k = next.board.length;
            for(int i=0;i<k;i++){
                for(int j=0;j<k;j++){
                    if(next.board[i][j]!=node.board[i][j]){
                        flag = 0;
                        break;
                    }
                }
                if(flag==0) break;
            }
            if(flag==1) return true;
        }

        return false;
    }

    private static void printSolution(Node node) {
        if(node == null){
            return;
        }

        printSolution(node.parent);
        show(node.board);
        System.out.println("");
    }

    public static class Compare implements Comparator<Node>{
		@Override
		public int compare(Node node1, Node node2){
			return (node1.cost) > (node2.cost)?1:-1;
		}
	}

    private static int total_cost(int choice, int[][] board, int[][] boardFinal, int moves) {
        if(choice==1){
            return moves+ hamming(board, boardFinal);
        }else if(choice==2){
            return moves+manhattan(board, boardFinal);
        }
        return 0;
    }

    private static int manhattan(int[][] board, int[][] boardFinal) {
        int k = board.length;
        int sum = 0;
        for(int m=0;m<k;m++){
            for(int n=0;n<k;n++){
                int val = board[m][n];
                if(val!=0){
                    //System.out.println(sum);
                    sum+= (int) Math.abs((val-1)/k - m);
                    //System.out.println(sum);
                    sum+= (int) Math.abs(Math.abs((val-1)%k) - n);
                    //System.out.println(sum);
                    //if(m==1 && n==1)System.exit(0);
                }
            }
        }
        return sum;
    }

    private static int hamming(int[][] board, int[][] boardFinal) {
        int cost = 0;
		for (int i = 0; i < board.length; i++)
		for (int j = 0; j < board.length; j++)
			if (board[i][j]!=0 && board[i][j] != boardFinal[i][j])
			cost++;
		return cost;
    }

    public static void show(int[][] matrix){
        //System.out.println("Current grid configuration:");
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix.length;j++){
                if(matrix[i][j]>0)System.out.print(matrix[i][j]+"  ");
                else System.out.print("*  ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static boolean isSolvable(int[][] mat, int blankX, int blankY) {
        int numOfInversions = 0;
        int idx = 0;
        int k = mat.length;
        int[] linearMat = new int[k*k];
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++){
                linearMat[idx++] = mat[i][j];
            }
        }

        for(int i=0;i<linearMat.length-1;i++){
            for(int j=i+1;j<linearMat.length;j++){
                if(linearMat[i]!=0 && linearMat[j]!=0 && linearMat[i]>linearMat[j]){
                    numOfInversions++;
                }
            }
        }

        if(k%2==1 && numOfInversions%2 == 0) return true;
        
        if(k%2==0 && ((blankX%2==0 && numOfInversions%2==1) || (blankX%2==1 && numOfInversions%2==0))) return true;

        return false;
    }
}