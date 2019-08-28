import java.util.*;
import java.io.*; // pour la lecture de fichiers
import java.lang.*;

public class CoverageGraph extends MaxFlowProblem{
    int k;
    int maxCap;
    int maximumKRouteFlow;

    public CoverageGraph(){
    }

    public  CoverageGraph(int n, int k, double xmax, double ymax, double r, double[] x_cor, double[] y_cor, int[] c){ 
        this.s = 0;
        this.t = n+1;
        this.n = n+2;
        this.k = k;
        this.adjacencyMatrix = new int[n+2][n+2];
        for (int i = 1; i<n+1;i++){
            for (int j = 1; j<n+1;j++){
                if (Math.hypot(x_cor[i-1]-x_cor[j-1],y_cor[i-1]-y_cor[j-1])<=2*r)
                    this.adjacencyMatrix[i][j]=Math.min(c[i-1],c[j-1]);
                    this.adjacencyMatrix[j][i]=Math.min(c[i-1],c[j-1]);
            }
        }
        for (int i = 1; i<n+1;i++){
            if (x_cor[i-1]<=r){
                this.adjacencyMatrix[0][i]=c[i-1];
                this.adjacencyMatrix[i][0]=c[i-1];
            }
            if (x_cor[i-1]>=xmax-r){
                this.adjacencyMatrix[n+1][i]=c[i-1];
                this.adjacencyMatrix[i][n+1]=c[i-1];
            }
        }
        maxCap=0;
        for (int i = 0; i<n; i++){
            maxCap = Math.max(maxCap, c[i]);
        }
        this.maxFlow=0;
        this.maximumKRouteFlow=0;
        this.paths = new LinkedList<LinkedList<Integer>>();
    }
    public CoverageGraph(String filename){ // construit un graphe de couverture 
                                           // à partir d'un fichier .txt 
 
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int n = (int) Double.parseDouble(reader.readLine());
            int k = (int) Double.parseDouble(reader.readLine());
            int maxCap=0;
            double xmax = Double.parseDouble(reader.readLine());
            double ymax = Double.parseDouble(reader.readLine());
            double r = Double.parseDouble(reader.readLine());
            String test = reader.readLine();
            String[] xcor_str = test.split("   ");
            String[] ycor_str = reader.readLine().split("   ");
            String[] c_str = reader.readLine().split("   ");
            double[] x_cor = new double[n];
            double[] y_cor = new double[n];
            int[] c = new int[n];
            for (int i =0;i<n;i++){
                x_cor[i]= Double.parseDouble(xcor_str[i+1]);
                y_cor[i]= Double.parseDouble(ycor_str[i+1]);
                c[i]= (int) Double.parseDouble(c_str[i+1]);;
                maxCap = Math.max(maxCap,c[i]);
            }
            reader.close();

            this.s = 0;
            this.t = n+1;
            this.n = n+2;
            this.k = k;
            this.maxCap=maxCap;
            this.adjacencyMatrix = new int[n+2][n+2];
            for (int i = 1; i<n+1;i++){
                for (int j = i+1; j<n+1;j++){
                    if (Math.hypot(x_cor[i-1]-x_cor[j-1],y_cor[i-1]-y_cor[j-1])<=2*r){
                        this.adjacencyMatrix[i][j]=Math.min(c[i-1],c[j-1]);
                        this.adjacencyMatrix[j][i]=Math.min(c[i-1],c[j-1]);
                    }
                }
            }
            for (int i = 1; i<n+1;i++){
                if (x_cor[i-1]<=r){
                    this.adjacencyMatrix[0][i]=c[i-1];
                    this.adjacencyMatrix[i][0]=c[i-1];
                }
                if (x_cor[i-1]>=xmax-r){
                    this.adjacencyMatrix[n+1][i]=c[i-1];
                    this.adjacencyMatrix[i][n+1]=c[i-1];
                }
            }
        System.out.println("{");
        for (int i = 0; i<this.n;i++){
            System.out.print("{");
            for (int j = 0;j<this.n;j++){
                System.out.print(this.adjacencyMatrix[i][j]);
                if (j!=this.n-1){
                    System.out.print(",");
                }
            }

            System.out.println("},");
        }
        this.maxFlow=0;
        this.maximumKRouteFlow=0;
        this.paths = new LinkedList<LinkedList<Integer>>();
        System.out.println(r);
        System.out.println(xmax);
        System.out.println(ymax);
        System.out.println(Arrays.toString(x_cor));
        System.out.println(Arrays.toString(y_cor));
        System.out.println(Arrays.toString(c));
        }
        catch (Exception e){
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
        }

    }
    public  CoverageGraph PowerGraph(int p){
        CoverageGraph pg = new CoverageGraph();
        pg.s = this.s;
        pg.t = this.t;
        pg.n = this.n;
        pg.k = this.k;
        pg.adjacencyMatrix = new int[this.n][this.n];
        pg.maxFlow = 0;
        pg.maximumKRouteFlow = 0;
        pg.paths = new LinkedList<LinkedList<Integer>>();
        for (int i = 0; i<this.n;i++){
            for (int j = 0; j<this.n;j++){
                pg.adjacencyMatrix[i][j] = Math.min(p,this.adjacencyMatrix[i][j]);
            }
        }
        return pg;
    }
    public int[][] maxKRouteFlow(){
        // On raisonne par dichotomie, car on sait que le p cherché se situe entre
        // 0 et maxCap.
        int a = 0;
        int b = maxCap+1;
        int p = (a+b)/2;
        CoverageGraph pg;
        int[][] flow;
        int psi;

        while (Math.abs(b-a)>1){
            pg = this.PowerGraph(p);
            pg.FordFulkerson();
            psi = pg.maxFlow-k*p;
            if (psi>=0)
                a=p;
            else
                b = p;
            p = (a+b)/2;
        }
        pg = this.PowerGraph(a);
        flow = pg.FordFulkerson();
        this.maximumKRouteFlow = pg.maxFlow;
        return flow;
    }
    public int floor(int a, int b){ // computes the floor function of a/b where a and b are positive integers, b>0 and a<=b.
        if (a<b)
            return 0;
        else return 1;
    }
    public int ceil(int a, int b){ // computes the ceiling function of a/b where a and b are negative integers, b>0 and a<=b.
        if (a==0)
            return 0;
        else return 1;
    }
    public int[][] getBinary(){
        int[][] mat = new int[this.n][this.n];
        int[][] demands = new int[this.n][this.n];
        int[][] resu = new int[this.n][this.n];
        assert ((this.maxCap>0)&&(this.k>0)&&(this.maximumKRouteFlow>0)): "ligne 159 CoverageGraph";
        int[][] f = this.maxKRouteFlow(this.maxCap,this.k);
        int nu = this.maximumKRouteFlow/this.k;
        for (int i = 0; i<this.n;i++){
            for (int j = 0;j<this.n;j++){
                mat[i][j]=ceil(f[i][j],nu);
                demands[i][j]= floor(f[i][j],nu);
            }
        }
        MaxFlowProblemWithDemands mfpwd = new MaxFlowProblemWithDemands(this.s,this.t,this.n,mat,demands);
        mfpwd.feasibleFlow(resu);
        return resu;
    }
    public Pair<ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>>,Integer> robustBarrierCoverage(){
        // The idea is to create a list of 3-tuples, each containing as first element a list representing a set of K node-disjoint paths.
        // We call such a list a K-covering configuration. 
        // The second and third elements of each 3-tuple will dictate the scheduling of each K-covering configuration. 
        ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>> resu = new ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>>();
        // The first step consists on computing the K-Route Flow decomposition of our coverage graph. 
        Pair<ArrayList<Pair<Integer,int[][]>>,Integer> step1 = this.KRouteFlowDecomposition(this.maxCap,this.k);
        ArrayList<Pair<Integer,int[][]>> elementary_k_flows_and_their_lifespan = step1.getLeft();
        int network_lifetime = step1.getRight()/this.k;
        int lifespan, time = 0;
        int[][] elementary_k_flow;
        LinkedList<LinkedList<Integer>> disjoints_paths;
        MaxFlowProblem flow_problem;

        for (int i = 0; i<elementary_k_flows_and_their_lifespan.size();i++){
            lifespan = elementary_k_flows_and_their_lifespan.get(i).getLeft();
            elementary_k_flow = elementary_k_flows_and_their_lifespan.get(i).getRight();
            flow_problem = new MaxFlowProblem(this.n,elementary_k_flow,this.s,this.t);
            disjoints_paths = flow_problem.MaxNodeDisjointPaths();
            resu.add(new Triplet(disjoints_paths,time,time+lifespan));
            time+=lifespan;
        }
        return new Pair(resu,network_lifetime);
    }
    public static void main(String[] args){

        //Question 4:
        long startTimeMs = System.currentTimeMillis();

        CoverageGraph cg = new CoverageGraph("sensornetwork0.txt");
        LinkedList<LinkedList<Integer>> paths = cg.MaxNodeDisjointPaths();
        int m = cg.maxFlow;
        System.out.println();
        System.out.println("Maximum number of node-disjoint paths: "+m);
        System.out.println();
        System.out.println("Corresponding node-disjoint paths: ");
        System.out.println();
        while (!paths.isEmpty()){
            System.out.println("                   "+paths.poll());
        }
        System.out.println();
        int network_lifetime = m/cg.k;
        System.out.println("Network lifetime: "+ network_lifetime);
        System.out.println();

        long taskTimeMs = System.currentTimeMillis()-startTimeMs;
        System.out.println("CPU time: "+taskTimeMs + " ms");
        System.out.println();

        // Question 5: on teste avec le graphe proposé dans l'énoncé.
        // Notons qu'ici on a utilisé la méthode maxKRouteFlow de MaxFlowProblem
        // car le graphe proposé par l'énoncé a un format beaucoup plus simple que
        // les graphes de la classe CoverageGraph. 
        int[][] mat = new int[4][4];
        mat[0]= new int[]{0,2,3,5};
        mat[1] = new int[]{0,0,1,0};
        mat[2] = new int[]{0,0,0,5};
        mat[3] = new int[]{0,0,0,0};
        MaxFlowProblem mfp = new MaxFlowProblem(4,mat,0,3);
        int[][] f = mfp.maxKRouteFlow(4,2);
        System.out.println("Value of maximum K-route flow = "+mfp.maxFlow);
        System.out.println();
        System.out.println("Maximum K-route flow = ");
        for (int i = 0; i<4;i++){
            for (int j = 0 ;j<4;j++){
                System.out.print(f[i][j]+"        ");
            }
            System.out.println();
        }
        System.out.println();
        // Succès pour ce test: on trouve bien le flot maximal prédit par la théorie.

        // Question 7: tests.

        int[][] newmat0 = {
            {0,22,5,0,0,0,0,0},
            {0,0,0,11,5,0,0,0},
            {0,0,0,0,7,0,0,0},
            {0,0,0,0,0,7,5,0},
            {0,0,0,0,0,0,8,0},
            {0,0,0,0,0,0,0,6},
            {0,0,0,0,0,0,0,11},
            {0,0,0,0,0,0,0,0}
        };
        int[][] d0 = {
            {0,8,3,0,0,0,0,0},
            {0,0,0,4,3,0,0,0},
            {0,0,0,0,3,0,0,0},
            {0,0,0,0,0,6,5,0},
            {0,0,0,0,0,0,6,0},
            {0,0,0,0,0,0,0,2},
            {0,0,0,0,0,0,0,9},
            {0,0,0,0,0,0,0,0}
        };
        int[][] resu = new int[8][8];
        int val;
        MaxFlowProblemWithDemands mfpwd = new MaxFlowProblemWithDemands(0,7,8,newmat0,d0);
        boolean bool = mfpwd.feasibleFlow(resu);
        val = mfpwd.maxFlow;
        System.out.println("Is there a feasible flow? "+bool);
        if (bool){
            System.out.println();
            System.out.println("Its matrix is: ");
            for (int i = 0; i<8;i++){
                for (int j = 0 ;j<8;j++){
                    System.out.print(resu[i][j]+"        ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println("Its value is "+mfpwd.maxFlow);
        }

        //Question 8: Edmonds-Karp with demands.
        mfpwd = new MaxFlowProblemWithDemands(0,7,8,newmat0,d0);
        resu = mfpwd.EdmondsKarpWithDemands(resu,val);
        System.out.println();
        System.out.println("The corresponding maximum flow is : ");
        for (int i = 0; i<8;i++){
            for (int j = 0 ;j<8;j++){
                System.out.print(resu[i][j]+"        ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Its value is "+mfpwd.maxFlow);

        // In this example, the feasible flow was already a maximum flow.
        // Let's look for an example where the feasible flow found is not maximal. 
        int[][] newmattest = {
            {0,44,10,0,0,0,0,0},
            {0,0,0,22,10,0,0,0},
            {0,0,0,0,14,0,0,0},
            {0,0,0,0,0,14,10,0},
            {0,0,0,0,0,0,16,0},
            {0,0,0,0,0,0,0,12},
            {0,0,0,0,0,0,0,22},
            {0,0,0,0,0,0,0,0}
        };


        int[][] dtest = {
            {0,8,3,0,0,0,0,0},
            {0,0,0,4,3,0,0,0},
            {0,0,0,0,3,0,0,0},
            {0,0,0,0,0,6,5,0},
            {0,0,0,0,0,0,6,0},
            {0,0,0,0,0,0,0,2},
            {0,0,0,0,0,0,0,9},
            {0,0,0,0,0,0,0,0}
        };
        resu = new int[8][8];
        mfpwd = new MaxFlowProblemWithDemands(0,7,8,newmattest,dtest);
        bool = mfpwd.feasibleFlow(resu);
        val = mfpwd.maxFlow;
        System.out.println("Is there a feasible flow? "+bool);
        if (bool){
            System.out.println();
            System.out.println("Its matrix is: ");
            for (int i = 0; i<8;i++){
                for (int j = 0 ;j<8;j++){
                    System.out.print(resu[i][j]+"        ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println("Its value is "+mfpwd.maxFlow);
        }
        resu = mfpwd.EdmondsKarpWithDemands(resu,val);
        System.out.println();
        System.out.println("The corresponding maximum flow is : ");
        for (int i = 0; i<8;i++){
            for (int j = 0 ;j<8;j++){
                System.out.print(resu[i][j]+"        ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Its value is "+mfpwd.maxFlow);

/*        int[][] newmat1 = {
            {0,1,1,0,0,0,0,0},
            {0,0,0,1,1,0,0,0},
            {0,0,0,0,1,0,0,0},
            {0,0,0,0,0,1,1,0},
            {0,0,0,0,0,0,1,0},
            {0,0,0,0,0,0,0,1},
            {0,0,0,0,0,0,0,1}
        };
        int[][] d1 = {
            {0,1,0,0,0,0,0,0},
            {0,0,0,1,1,0,0,0},
            {0,0,0,0,0,0,0,0},
            {0,0,0,0,0,1,0,0},
            {0,0,0,0,0,0,1,0},
            {0,0,0,0,0,0,0,1},
            {0,0,0,0,0,0,0,0},
        };*/
        // Question 9, algortithm 1: binary flow
        mat = new int[4][4];
        mat[0]= new int[]{0,2,3,5};
        mat[1] = new int[]{0,0,1,0};
        mat[2] = new int[]{0,0,0,5};
        mat[3] = new int[]{0,0,0,0};
        MaxFlowProblem mp = new MaxFlowProblem(4,mat,0,3);
        f = mp.getBinary(mp.maxKRouteFlow(4,2),8,4,2);
        System.out.println();
        System.out.println("Binary K-flow = ");
        for (int i = 0; i<4;i++){
            for (int j = 0 ;j<4;j++){
                System.out.print(f[i][j]+"        ");
            }
            System.out.println();
        }
        System.out.println();

        // Question 9, algorithm 2: K-route flow decomposition
        MaxFlowProblem mp2 = new MaxFlowProblem(4,mat,0,3);
        Pair<ArrayList<Pair <Integer, int[][]>>,Integer> resu93 =mp2.KRouteFlowDecomposition(4,2);
        ArrayList<Pair <Integer, int[][]>> resu92 = resu93.getLeft();
        int networkLT = resu93.getRight();
        System.out.println(networkLT);
        int m2 = 0;
        Pair <Integer, int[][]> p;
        System.out.println();
        System.out.println("K-flow decomposition");
        for (int k= 0; k<resu92.size();k++){
            p=resu92.get(k);
            f=p.getRight();
            m2 = p.getLeft();
            System.out.println((k+1)+"-ème composante élémentaire, de poids "+m2+" dans la combinaison linéaire: ");
            for (int i = 0; i<4;i++){
                for (int j = 0;j<4;j++){
                    System.out.print(f[i][j]+"       ");
                }
                System.out.println();
            }
        }

                System.out.println("PENULTIME TEST: ");
        MaxFlowProblem mp3 = new MaxFlowProblem(4,mat,0,3);

        Pair<ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>>,Integer> penultime = mp3.robustBarrierCoverage(4,2);
        ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>> paths_and_schedules_penultime = penultime.getLeft();
        int network_lifetime_penultime = penultime.getRight();
        System.out.println("The sensor network as a lifetime of "+network_lifetime_penultime);
        System.out.println();
        System.out.println();
        System.out.println();
        Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer> current_triplet_penultime;
        LinkedList<LinkedList<Integer>> lili_penultime;
        for (int i = 0; i<paths_and_schedules_penultime.size();i++){
            current_triplet_penultime = paths_and_schedules_penultime.get(i);
            lili_penultime = current_triplet_penultime.getLeft();
            System.out.println("The following K-covering configuration is active between "+current_triplet_penultime.getMiddle()+" and "+current_triplet_penultime.getRight()+":");
            while (!lili_penultime.isEmpty()){
                System.out.println(lili_penultime.poll());
            }

            System.out.println();
            System.out.println();
        }

        System.out.println();

        System.out.println("TEST FINAL: ");
        cg = new CoverageGraph("sensornetwork1.txt");
        Pair<ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>>,Integer> question11 = cg.robustBarrierCoverage();
        ArrayList<Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer>> paths_and_schedules = question11.getLeft();
        network_lifetime = question11.getRight();
        System.out.println("The sensor network as a lifetime of "+network_lifetime);
        System.out.println();
        System.out.println();
        System.out.println();
        Triplet<LinkedList<LinkedList<Integer>>,Integer, Integer> current_triplet;
        LinkedList<LinkedList<Integer>> lili;
        for (int i = 0; i<paths_and_schedules.size();i++){
            current_triplet = paths_and_schedules.get(i);
            lili = current_triplet.getLeft();
            System.out.println("The following K-covering configuration is active between "+current_triplet.getMiddle()+" and "+current_triplet.getRight()+":");
            while (!lili.isEmpty()){
                System.out.println(lili.poll());
            }

            System.out.println();
            System.out.println();
        }
        System.out.println(Math.hypot(-4,3));
    }

};