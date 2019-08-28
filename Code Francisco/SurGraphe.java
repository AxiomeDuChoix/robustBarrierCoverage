import java.util.*;

public class SurGraphe extends MaxFlowProblem{

    static int MAX = Integer.MAX_VALUE;
    public  SurGraphe(int s, int t, int n, int[][] mat, int[][] d){ 
        this.s = 0;
        this.t = t+2;
        this.n = n+2;
        this.adjacencyMatrix = new int[this.n+2][this.n+2];
        int vs1, vs2;
        for (int i = 0; i<n; i++){
            vs1=0;
            vs2=0;
            for (int j = 0; j<n; j++){
                this.adjacencyMatrix[i+1][j+1]=mat[i][j]-d[i][j];
                vs1+=d[j][i];
                vs2+=d[i][j];
            }
            this.adjacencyMatrix[this.s][i+1]=vs1;
            this.adjacencyMatrix[i+1][this.t]=vs2;
        }

        this.adjacencyMatrix[n][1]=MAX;
        this.maxFlow=0;
        this.paths = new LinkedList<LinkedList<Integer>>();
    }
    public  SurGraphe(MaxFlowProblemWithDemands mfp){ 
        this.s = 0;
        this.t = mfp.t+2;
        this.n = mfp.n+2;
        this.adjacencyMatrix = new int[this.n+2][this.n+2];
        int vs1, vs2;
        for (int i = 0; i<mfp.n; i++){
            vs1=0;
            vs2=0;
            for (int j = 0; j<mfp.n; j++){
                this.adjacencyMatrix[i+1][j+1]=mfp.adjacencyMatrix[i][j]-mfp.d[i][j];
                vs1+=mfp.d[j][i];
                vs2+=mfp.d[i][j];
            }
            this.adjacencyMatrix[this.s][i+1]=vs1;
            this.adjacencyMatrix[i+1][this.t]=vs2;
        }
        this.adjacencyMatrix[mfp.n][1]=MAX;
        this.maxFlow=0;
        this.paths = new LinkedList<LinkedList<Integer>>();
    }
};