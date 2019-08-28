import java.util.*;

public class DiGraphe extends MaxFlowProblem{


    public  DiGraphe(MaxFlowProblem mfp){ // This constructor creates the modified graph which will allow us to compute M node-disjoint paths. 
        this.s = mfp.s;
        this.t = 2*mfp.t-1;
        this.n = 2*mfp.n-2;
        this.adjacencyMatrix = new int[this.n][this.n];
        for (int i = 0; i<mfp.n-1; i++){
            for (int j = 0; j<mfp.n-1; j++){
                this.adjacencyMatrix[2*i][2*j+1]=mfp.adjacencyMatrix[i][j+1];
            }
            if (i+2<mfp.n)
                this.adjacencyMatrix[2*i+1][2*i+2]=1;
        }
        this.maxFlow=0;
        this.paths = new LinkedList<LinkedList<Integer>>();
    }

    public int maxNumberOfNodeDisjointPaths(){
        this.FordFulkerson();
        return this.maxFlow;
    }
    public LinkedList<LinkedList<Integer>> MaxNodeDisjointPaths(){
        this.FordFulkerson();
        return this.paths;
    }
    public static void main(String[] args){
        int[][] mat = new int[6][6];
        mat[0]= new int[]{0,16,13,0,0,0};
        mat[1] = new int[]{0,0,10,12,0,0};
        mat[2] = new int[]{0,4,0,0,14,0};
        mat[3] = new int[]{0,0,9,0,0,20};
        mat[4] = new int[]{0,0,0,7,0,4};
        mat[5]= new int[]{0,0,0,0,0,0};

        DiGraphe q1 = new DiGraphe(new MaxFlowProblem(6,mat,0,5));

        LinkedList<LinkedList<Integer>> paths = q1.MaxNodeDisjointPaths();
        while (!paths.isEmpty())
            System.out.println(paths.poll());
    }
};