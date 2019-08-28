import java.util.*;
import java.io.*;

public class Barrier {
	static int max=Integer.MAX_VALUE;
	static int inf=max/2; //Pour éviter de passer dans les négatifs si on fait inf+1 par exemple
	float[][] sensors; //(sensors[5][0],sensors[5][1]) contient les coordonnées du 5e capteur
	int nbSensors; //nombre de capteurs
	float R; //portée des catpeurs
	int[] ltSensors; //temps de vie des capteurs
	float xmax; 
	float ymax;
	int s;
	int t;
	int[][] Graphe_adjacence;
	int K;
	int cMax;
	int lifetimeMax;
	
	Barrier(float[][] sensors1,int R1,int[] ltSensors1, int xmax1,int ymax1){
		this.sensors=sensors1;
		this.R=R1;
		this.ltSensors=ltSensors1;
		this.xmax=xmax1;
		this.ymax=ymax1;
		this.nbSensors=sensors.length;
		this.s=nbSensors;
		this.t=nbSensors+1;
		Graphe_adjacence=new int[nbSensors+2][nbSensors+2];
		for (int i=0;i<nbSensors;i++){
			for(int j=i+1;j<nbSensors;j++){
				if(distance(sensors[i],sensors[j])<=2*R){
					Graphe_adjacence[i][j]=1;
					Graphe_adjacence[j][i]=1;
				}
				if(sensors[i][0]<=R){
					Graphe_adjacence[i][s]=1;
					Graphe_adjacence[s][i]=1;
				}
				if(xmax-sensors[i][0]<=R){
					Graphe_adjacence[i][t]=1;
					Graphe_adjacence[t][i]=1;
				}
			}
		}
		
	}
	
	Barrier(String filename){
		try {
			FileReader fileReader=new FileReader(filename);
		    BufferedReader bufferedReader=new BufferedReader(fileReader);
		    this.nbSensors=Math.round(Float.parseFloat(bufferedReader.readLine()));
			this.K=Math.round(Float.parseFloat(bufferedReader.readLine()));
			this.xmax=Float.parseFloat(bufferedReader.readLine());
			this.ymax=Float.parseFloat(bufferedReader.readLine());
			this.R=Float.parseFloat(bufferedReader.readLine());
			this.sensors=new float[nbSensors][2];
			String[] line=bufferedReader.readLine().split("   ");
			for(int i=0;i<nbSensors;i++){
				this.sensors[i][0]=Float.parseFloat(line[i]);
			}
			line=bufferedReader.readLine().split("   ");
			for(int i=0;i<nbSensors;i++){
				this.sensors[i][1]=Float.parseFloat(line[i]);
			}
			this.ltSensors=new int[nbSensors];
			line=bufferedReader.readLine().split("   ");
			for(int i=0;i<nbSensors;i++){
				this.ltSensors[i]=Math.round(Float.parseFloat(line[i]));
			}
			bufferedReader.close();
			this.cMax=0;
			this.s=nbSensors;
			this.t=nbSensors+1;
			Graphe_adjacence=new int[nbSensors+2][nbSensors+2];
			int poids_arete;
			for (int i=0;i<nbSensors;i++){
				if(sensors[i][0]<=R){
					Graphe_adjacence[s][i]=ltSensors[i];
					cMax=Integer.max(cMax,ltSensors[i]);
				}
				if(xmax-sensors[i][0]<=R){
					Graphe_adjacence[i][t]=ltSensors[i];
					cMax=Integer.max(cMax,ltSensors[i]);
				}
				for(int j=i+1;j<nbSensors;j++){
					if(distance(sensors[i],sensors[j])<=2*R){
						poids_arete=Integer.min(ltSensors[i],ltSensors[j]);
						Graphe_adjacence[i][j]=poids_arete;
						Graphe_adjacence[j][i]=poids_arete;
						cMax=Integer.max(cMax,poids_arete);
					}
				}
			}
	    } catch (IOException e) {
	        e.printStackTrace(); 
	    }
	}
	
	Barrier(int[][] c,int K){
		this.nbSensors=c.length-2;
		this.Graphe_adjacence=new int[nbSensors+2][nbSensors+2];
		this.cMax=0;
		for(int i=0;i<nbSensors+2;i++){
			for(int j=0;j<nbSensors+2;j++){
				Graphe_adjacence[i][j]=c[i][j];
				cMax=Integer.max(cMax,c[i][j]);
			}
		}
		this.s=nbSensors;
		this.t=nbSensors+1;
		this.K=K;
	}
	public void print(){
		for(int i=0;i<nbSensors+2;i++){
			for(int j=0;j<nbSensors+2;j++){
				System.out.print(Graphe_adjacence[i][j]+", ");
			}
			System.out.println();
		}
	}
	public static double distance(float[] pt1,float[] pt2){
		return Math.sqrt(((pt1[0]-pt2[0])*(pt1[0]-pt2[0])+(pt1[1]-pt2[1])*(pt1[1]-pt2[1])));
	}
	public static LinkedList<Integer> BFS(int[][] G, int start, int goal){
		int l=G.length;
		int[] pred=new int[l];
		boolean[] vus=new boolean[l];
		pred[start]=start;
		vus[start]=true;
		LinkedList<Integer> file=new LinkedList<Integer>();
		file.addLast(start);
		int sommet;
		while(!vus[goal]&&!file.isEmpty()){
			sommet=file.removeFirst();
			for(int voisin=0;voisin<l;voisin++){
				if(!vus[voisin]&&G[sommet][voisin]>0){
					vus[voisin]=true;
					pred[voisin]=sommet;
					file.addLast(voisin);
				}
			}
		}
		LinkedList<Integer> res=new LinkedList<Integer>();
		if(!vus[goal])return res;
		while(pred[goal]!=goal){
			res.addFirst(goal);
			goal=pred[goal];
		}
		res.addFirst(goal);
		return res;
	}

	public static int[][] maxFlow(int[][] cEnvoi, int s, int t){
		int l=cEnvoi.length;
		int[][] c=new int[l][l];
		int[][] f=new int[l][l];
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				c[i][j]=cEnvoi[i][j];
			}
		}
		LinkedList<Integer> chemin=BFS(c,s,t);
		ListIterator<Integer> parcours;
		int sommet,sommet2,capaMax=max;
		while(!chemin.isEmpty()){
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				capaMax=Integer.min(capaMax,c[sommet][sommet2]);
				sommet=sommet2;
			}
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				f[sommet][sommet2]+=capaMax;
				f[sommet2][sommet]-=capaMax;
				c[sommet][sommet2]-=capaMax;
				c[sommet2][sommet]+=capaMax;
				sommet=sommet2;
			}
			chemin=BFS(c,s,t);
			capaMax=max;
		}
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				f[i][j]=Integer.max(0,f[i][j]);
			}
		}
		return f;
	}
	public static int[][] maxFlowWithBound(int[][] c, int bound){
		int l=c.length;
		int oldSource=l-2,newSource=l-1,oldTarget=l-1,newTarget=l;
		int[][] c2=new int[l+1][l+1];
		int[][] f2;
		int[][] f=new int[l][l];
		for(int i=0;i<l-1;i++){
			for(int j=0;j<l-1;j++){
				c2[i][j]=c[i][j];
			}
			c2[i][newTarget]=c[i][oldTarget];
			c2[newTarget][i]=c[oldTarget][i];
		}
		c2[newSource][oldSource]=bound;
		f2=maxFlow(c2,l-1,l);
		if(f2==null)return null;
		for(int i=0;i<l-1;i++){
			for(int j=0;j<l-1;j++){
				f[i][j]=f2[i][j];
			}
			f[i][oldTarget]=f2[i][newTarget];
			f[oldTarget][i]=f2[newTarget][i];
		}
		return f;
	}
	public ArrayList<LinkedList<Integer>> cheminsDisjoints(){
		ArrayList<LinkedList<Integer>> chemins=new ArrayList<LinkedList<Integer>>();
		int[][] G2=new int[2*nbSensors+2][2*nbSensors+2];
		for(int i=0;i<nbSensors;i++){
			G2[i][i+nbSensors+2]=1;
			G2[s][i]=Graphe_adjacence[s][i];
			G2[i+nbSensors+2][t]=Graphe_adjacence[i][t];
			for(int j=0;j<nbSensors;j++){
				G2[i+nbSensors+2][j]=Graphe_adjacence[i][j];
			}
		}
		int l=G2.length;
		int[] pred=new int[l];
		pred[s]=s;
		LinkedList<Integer> lastPred=new LinkedList<Integer>();
		int[][] f=new int[l][l];
		LinkedList<Integer> chemin=BFS(G2,s,t);
		ListIterator<Integer> parcours;
		int sommet,sommet2,capaMax=1;
		while(!chemin.isEmpty()){
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				f[sommet][sommet2]+=capaMax;
				f[sommet2][sommet]-=capaMax;
				G2[sommet][sommet2]-=capaMax;
				G2[sommet2][sommet]+=capaMax;
				if(f[sommet][sommet2]>0){
					pred[sommet2]=sommet;
					if(sommet2==t){
						lastPred.add(sommet);
					}
				}
				sommet=sommet2;
			}
			chemin=BFS(G2,s,t);
		}
		ListIterator<Integer> i=lastPred.listIterator();
		int compteur=0;
		while(i.hasNext()){
			chemins.add(new LinkedList<Integer>());
			chemins.get(compteur).addFirst(t);
			sommet=i.next();
			sommet=pred[sommet];
			while(pred[sommet]!=sommet){
				chemins.get(compteur).addFirst(sommet);
				sommet=pred[sommet];
				sommet=pred[sommet];
			}
			chemins.get(compteur).addFirst(sommet);
			compteur++;
		}
		return chemins;
	}
	public int[][] Gp(int p){
		int[][] g=new int[nbSensors+2][nbSensors+2];
		for(int i=0;i<nbSensors+2;i++){
			for(int j=0;j<nbSensors+2;j++){
				g[i][j]=Integer.min(p,Graphe_adjacence[i][j]);
			}
		}
		return g;
	}
	public static int value(int[][]flow){
		int l=flow.length;
		int sum=0;
		for(int i=0;i<l;i++){
			sum+=flow[i][l-1];
		}
		return sum;
	}
	public int[][] KrouteFlow(){
		int gauche=0,centre=cMax/2,droite=cMax;
		int[][] gCentre=Gp(centre);
		int[][] f = null;
		int v=0;
		while(droite-gauche>1){
			f=maxFlow(gCentre, s,t);
			v=value(f)-K*centre;
			if(v>=0){
				gauche=centre;
				centre+=(droite-gauche)/2;
			}
			else{
				droite=centre;
				centre-=(droite-gauche)/2;
			}
			gCentre=Gp(centre);
		}
		int p;
		int vf=value(maxFlow(Gp(droite),s,t));
		v=vf-K*droite;
		if(v>=0)p=droite;
		else p=gauche;
		return maxFlowWithBound(Gp(p),K*(vf/K));
		
	}
	public static int[][] feasibleFlow(int[][]G, int[][] d, int s,int t){
		int l=G.length;
		int[][] G2=new int[l+2][l+2];
		int s2=l, t2=l+1;
		int somme1=0,somme2=0;
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				G2[i][j]=G[i][j]-d[i][j];
				somme1+=d[j][i];
				somme2+=d[i][j];
			}
			G2[s2][i]=somme1;
			G2[i][t2]=somme2;
			somme1=0;
			somme2=0;
		}
		G2[t][s]=inf;
		int[][] f=maxFlow(G2,s2,t2);
//		System.out.println(Arrays.deepToString(f));
		boolean saturatingFlow=true;
		for(int i=0;i<l;i++){
			saturatingFlow&=f[s2][i]==G2[s2][i];
			saturatingFlow&=f[i][t2]==G2[i][t2];
		}
		if(!saturatingFlow){
			return null;
		}
		int[][] f2= new int[l][l];
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				if(G[i][j]>0)f2[i][j]=f[i][j]+d[i][j];
			}
		}
		return f2;
	}
	public static int[][] maxFlowWithDemands(int[][] c, int[][] d){
		//CONVENTION : la source est l'avant-dernier sommet, le but est le dernier
		int l1=c.length;
		int s=l1-2,t=l1-1;
		int[][] f0=feasibleFlow(c,d,s,t);
		if(f0==null)return null;
		
		int[][] G2=new int[2*l1-2][2*l1-2];
		int l=G2.length;
		int[][] f=new int[l][l];
		for(int i=0;i<l1-2;i++){
			G2[s][i]=c[s][i]-f0[s][i];
			f[s][i]=f0[s][i];
			//Pas besoin de considérer les arêtes dans l'autre 
			//sens car on ne repassera pas par là
			
			G2[i+l1][t]=c[i][t]-f0[i][t];
			f[i+l1][t]=f0[i][t];
			//Idem
			for(int j=0;j<l1-2;j++){
				G2[i+l1][j]=c[i][j]-f0[i][j];
				f[i+l1][j]=f0[i][j];
				G2[j][i+l1]=f[i+l1][j]-d[i][j];
				f[j][i+l1]=d[i][j]-f[i+l1][j];
			}
			G2[i][i+l1]=inf;
			G2[i+l1][i]=inf;
		}
		LinkedList<Integer> chemin=BFS(G2,s,t);
		ListIterator<Integer> parcours;
		int sommet,sommet2,capaMax=max;
		while(!chemin.isEmpty()){
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				capaMax=Integer.min(capaMax,G2[sommet][sommet2]);
				sommet=sommet2;
			}
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				f[sommet][sommet2]+=capaMax;
				f[sommet2][sommet]-=capaMax;
				G2[sommet][sommet2]-=capaMax;
				G2[sommet2][sommet]+=capaMax;
				sommet=sommet2;
			}
			chemin=BFS(G2,s,t);
			capaMax=max;
		}
		//On va réutiliser f0 pour renvoyer le résultat
		for(int i=0;i<l1-2;i++){
			f0[s][i]=f[s][i];
			f0[i][t]=f[i+l1][t];
			for(int j=i+1;j<l1-2;j++){
				f0[i][j]=f[i+l1][j];
				f0[j][i]=f[j+l1][i];
			}
		}
		return f0;
	}
	public static int[][] maxFlowWithDemandsAndBound(int[][] c,int[][] d, int bound){
		int l=c.length;
		int oldSource=l-2,newSource=l-1,oldTarget=l-1,newTarget=l;
		int[][] c2=new int[l+1][l+1];
		int[][] d2=new int[l+1][l+1];
		int[][] f2;
		int[][] f=new int[l][l];
		for(int i=0;i<l-1;i++){
			for(int j=0;j<l-1;j++){
				c2[i][j]=c[i][j];
				d2[i][j]=d[i][j];
			}
			c2[i][newTarget]=c[i][oldTarget];
			c2[newTarget][i]=c[oldTarget][i];
			d2[i][newTarget]=d[i][oldTarget];
			d2[newTarget][i]=d[oldTarget][i];
		}
		c2[newSource][oldSource]=bound;
		f2=maxFlowWithDemands(c2,d2);
		if(f2==null)return null;
		for(int i=0;i<l-1;i++){
			for(int j=0;j<l-1;j++){
				f[i][j]=f2[i][j];
			}
			f[i][oldTarget]=f2[i][newTarget];
			f[oldTarget][i]=f2[newTarget][i];
		}
		return f;
	}
	public static ArrayList<int[][]> decomposition(int[][] Kflow,int K){
		ArrayList<int[][]> res=new ArrayList<int[][]>();
		int l=Kflow.length;
		int[][] Gf=new int[l][l];
		int[][] Df=new int[l][l];
		int[][] gFlow=new int[l][l];
		int[][]f=new int[l][l];
		int delta1=inf,delta2=inf,delta;
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				f[i][j]=Kflow[i][j];
			}
		}
		int v=value(f)/K;
		while(v!=0){
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					Gf[i][j]=(int)Math.ceil(((double)f[i][j])/((double)v));
					Df[i][j]=(int)Math.floor(((double)f[i][j])/((double)v));
				}
			}
			gFlow=maxFlowWithDemandsAndBound(Gf,Df,K);
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					if(gFlow[i][j]==0)delta1=Integer.min(delta1,v-f[i][j]);
					else delta2=Integer.min(delta2,f[i][j]);
				}
			}
			delta=Integer.min(delta1,delta2);
			v-=delta;
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					f[i][j]-=delta*gFlow[i][j];
				}
			}
			delta1=inf;
			delta2=inf;
			res.add(new int[l][l]);
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					res.get(res.size()-1)[i][j]=delta*gFlow[i][j];
				}
			}
		}
		return res;
	}
	
	public HashMap<LinkedList<Integer>,LinkedList<Integer>> scheduling(){
		HashMap<LinkedList<Integer>,LinkedList<Integer>> map=new HashMap<LinkedList<Integer>, LinkedList<Integer>>();
		ArrayList<int[][]> decomposition=decomposition(KrouteFlow(),K);
		ArrayList<LinkedList<Integer>> paths;
		Iterator<LinkedList<Integer>> it;
		LinkedList<Integer> path;
		int[][] elementaryKflow;
		int value;
		int time=0;
		for(int i=0;i<decomposition.size();i++){
			elementaryKflow=decomposition.get(i);
			value=value(elementaryKflow);
			paths=pathsKflow(elementaryKflow);
			it=paths.iterator();
			while(it.hasNext()){
				path=it.next();
				for(int t1=time;t1<time+value;t1++){
					if(!map.containsKey(path))map.put(path,new LinkedList<Integer>());
					map.get(path).add(t1);
				}
			}
			time+=value;
		}
		lifetimeMax=time;
		return map;
	}
	public static ArrayList<LinkedList<Integer>> pathsKflow(int[][] Kflow){
		ArrayList<LinkedList<Integer>>res=new ArrayList<LinkedList<Integer>>();
		int l=Kflow.length;
		int pere,rang,indice;
		for(int i=0;i<l;i++){
			if(Kflow[i][l-1]>0){
				pere=i;
				res.add(new LinkedList<Integer>());
				rang=res.size()-1;
				res.get(rang).addFirst(l-1);
				while(pere!=l-2){
					res.get(rang).addFirst(pere);
					indice=0;
					while(Kflow[indice][pere]==0){
						indice++;
					}
					pere=indice;
				}
				res.get(rang).addFirst(pere);
			}
		}
		return res;
	}
	public static void question4(){
		long t=System.currentTimeMillis();
		Barrier b=new Barrier("sensornetwork0.doc");
		ArrayList<LinkedList<Integer>> res=b.cheminsDisjoints();
		System.out.println("Temps : "+(System.currentTimeMillis()-t));
		System.out.println(res.toString());
		System.out.println((res.size())/b.K);
		System.out.println(value(maxFlow(b.Graphe_adjacence,b.s,b.t)));
//		b.print();
	}
	public static void printTab(int[][] tab){
		if(tab==null)return;
		int l1=tab.length,l2=tab[0].length;
		System.out.println();
		for(int i=0;i<l1;i++){
			System.out.print("{");
			for(int j=0;j<l2;j++){
				System.out.print(tab[i][j]+",");
			}
			System.out.println("},");
		}
		System.out.println();
	}
	public static void printArrayList(ArrayList<int[][]> array){
		Iterator<int[][]> it=array.iterator();
		while(it.hasNext()){
			printTab(it.next());
			}
	}
	public void question11(){
		System.out.println("Chemins et temps d'allumage : ");
		System.out.println(scheduling());
		System.out.println("TOTAL LIFETIME : "+lifetimeMax);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[][] c1={
				{0,1,0,3,0,0},
				{2,0,0,0,0,1},
				{0,3,0,0,0,0},
				{0,0,0,0,0,3},
				{1,0,3,0,0,0},
				{0,0,0,0,0,0}
				};
		int[][] d1={
				{0,1,0,0,0,0},
				{0,0,0,0,0,0},
				{0,0,0,0,0,0},
				{0,0,0,0,0,0},
				{0,0,0,0,0,0},
				{0,0,0,0,0,0}
				};
		int[][] c2={
				{0,1,1,0,0,1,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,0,1,0,1,1,1,1,1,1,0,0},
				{1,0,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0},
				{1,1,0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,0,0},
				{0,1,1,0,1,0,1,0,0,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0},
				{0,0,1,1,0,0,1,0,0,0,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0},
				{1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,0,0},
				{0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,1,1,1,1,1,0,1,0,0,0,0},
				{1,1,1,0,0,1,0,0,1,1,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,1,1,1,1,1,0,1},
				{1,1,1,0,0,1,0,1,0,1,1,1,1,1,1,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,0,0},
				{1,1,1,0,0,1,0,1,1,0,1,0,0,1,1,1,1,0,0,0,1,0,0,0,0,0,1,0,1,1,0,1},
				{1,1,1,1,1,1,0,1,1,1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,0,0},
				{1,1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0},
				{1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0},
				{1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0},
				{1,1,1,0,0,1,0,1,1,1,1,1,1,1,0,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,0,0},
				{1,1,1,0,0,1,0,1,1,1,1,1,1,1,1,0,1,0,0,0,1,0,0,0,0,1,1,1,1,1,0,1},
				{1,1,1,0,0,1,0,1,1,1,1,0,0,1,1,1,0,0,0,0,1,0,0,0,0,0,1,0,1,1,0,1},
				{0,0,1,1,1,0,1,0,0,0,0,1,1,0,0,0,0,0,1,1,0,1,1,1,1,1,0,1,0,0,0,0},
				{0,0,1,1,1,0,1,0,0,0,1,1,1,0,0,0,0,1,0,1,1,1,1,1,1,1,0,1,0,0,0,0},
				{0,0,0,1,1,0,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,1,1,1,1,1,0,1,0,0,0,0},
				{1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1,0,0,1,1,1,1,1,1,1,1,1,0,0},
				{0,1,1,1,1,0,1,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,1,1,1,1,0,1,0,0,0,0},
				{1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,0,0,1,1,1,1,1,0,1,1,1,0,1,0,0,0,0},
				{0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,0,0,1,1,1,1,1,1,0,1,1,0,1,0,0,0,0},
				{1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,0,0,1,1,1,1,1,1,1,0,1,0,1,0,0,0,0},
				{1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,0,0},
				{1,1,1,0,0,1,0,1,1,1,1,0,1,1,1,1,1,0,0,0,1,0,0,0,0,1,0,1,1,1,0,1},
				{1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,0,0,1,0,0},
				{1,1,0,0,0,1,0,1,1,1,1,0,0,1,1,1,1,0,0,0,1,0,0,0,0,0,1,0,0,1,0,1},
				{1,1,1,0,0,1,0,1,1,1,1,0,1,1,1,1,1,0,0,0,1,0,0,0,0,1,1,1,1,0,0,0},
				{0,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
				};
		int[][] c4={
					{0,3,0,1},
					{0,0,0,2},
					{3,0,0,0},
					{0,0,0,0}};
		int[][] d4={
				{0,5,0,0},
				{0,0,0,0},
				{0,0,0,0},
				{0,0,0,0}};
		int[][] c5={
				{0,0,5},
				{3,0,0},
				{0,0,0}};
		int[][] d5={
				{0,0,0},
				{0,0,0},
				{0,0,0}};
		int start=4;
		int goal=5;
		Barrier b1=new Barrier("sensornetwork1.doc");
		Barrier b2=new Barrier(c5,1);
//		System.out.println(Arrays.deepToString(feasibleFlow(c1,d1,start,goal)));
//		printTab(maxFlowWithBound(c4,2));
//		printTab(maxFlowWithDemands(c5,d5));
//		printTab(maxFlowWithDemandsAndBound(c5,d5,2));
//		System.out.println(Arrays.deepToString(maxFlow(c3,start,goal)));
//		System.out.println(value(maxFlow(c2,start,goal)));
//		System.out.println(value(maxFlow(b1.Graphe_adjacence,b1.s,b1.t)));
//		System.out.println(b2.cheminsDisjoints().toString());
//		question4();
//		printTab(b1.KrouteFlow());
//		printArrayList(decomposition(b1.KrouteFlow(),b1.K));
//		System.out.println(b1.scheduling());
		b1.question11();
		
	}
}


