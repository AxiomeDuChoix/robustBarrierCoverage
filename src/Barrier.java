import java.util.*;
import java.io.*;

public class Barrier {
	static int max=Integer.MAX_VALUE;
	static int inf=max/2; //on crée l'infini (si on prend inf=max, max+1 passe en négatif ce qui est parfois gênant)
	float[][] sensors; //(sensors[5][0],sensors[5][1]) contient les coordonnées du 5e capteur
	int nbSensors; //nombre de capteurs
	float R; //portée des capteurs
	int[] ltSensors; //temps de vie des capteurs
	float xmax; 
	float ymax;
	int s; //sommet source
	int t; //sommet puits
	int[][] Graphe_adjacence; //Matrice représentant le graphe du problème (avec les capacités)
	int K;
	int cMax;	//capacité maximum rencontrée dans Graphe_adjacence
	int lifetimeMax;
	
	//CONVENTION IMPORTANTE : pour simplifier les codes, dans toute la suite
	//on suppose que le puits est le DERNIER sommet dans les matrices d'adjacence
	//et la source l'AVANT-DERNIER
	
	//CONSTRUCTEURS
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
				this.sensors[i][0]=Float.parseFloat(line[i+1]);
			}
			line=bufferedReader.readLine().split("   ");
			for(int i=0;i<nbSensors;i++){
				this.sensors[i][1]=Float.parseFloat(line[i+1]);
			}
			this.ltSensors=new int[nbSensors];
			line=bufferedReader.readLine().split("   ");
			for(int i=0;i<nbSensors;i++){
				this.ltSensors[i]=Math.round(Float.parseFloat(line[i+1]));
			}
			bufferedReader.close();
			this.cMax=0;
			this.s=nbSensors; //On respecte la convention fixée en créant source et puits
			this.t=nbSensors+1;
			Graphe_adjacence=new int[nbSensors+2][nbSensors+2];
			int poids_arete; //IMPORTANT : la capacité d'une arête est définie en général 
			//comme le minimum des temps de vie des deux sommets
			//s et t ont un temps de vie infini
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
	public static double distance(float[] pt1,float[] pt2){
		return Math.sqrt(((pt1[0]-pt2[0])*(pt1[0]-pt2[0])+(pt1[1]-pt2[1])*(pt1[1]-pt2[1])));
	}
	
	Barrier(int[][] c,int K){ //Utile pour faire des tests manuels
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
	
	//ALGORITHMES DE BASE UTILES
	
	public static LinkedList<Integer> BFS(int[][] G, int start, int goal){
		//Parcours en largeur : recherche d'un plus court chemin de start à goal
		//Implémenté de manière standard avec des matrices d'adjacence
		//Complexité en O(n²) avec n le nombre de sommets
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
		//On récupère le plus court chemin
		LinkedList<Integer> res=new LinkedList<Integer>();
		if(!vus[goal])return res;
		while(pred[goal]!=goal){
			res.addFirst(goal);
			goal=pred[goal];
		}
		res.addFirst(goal);
		return res;
	}
	
	public static int value(int[][]flow){
		//Permet de calculer la valeur d'un flot
		//ATTENTION : avec la convention sus-citée sur s et t seulement
		int l=flow.length;
		int sum=0;
		for(int i=0;i<l;i++){
			sum+=flow[i][l-1];
		}
		return sum;
	}
	
	public static int[][] maxFlow(int[][] c){
		//Trouve un flot maximal dans le graphe c
		int l1=c.length;
		int s=l1-2,t=l1-1; //Convention sus-citée
		//Dédoublement du graphe pour éviter d'avoir au départ une arête dans un sens et
		//dans l'autre sens avec des capacités positives pour les deux sens
		//L'entrée du sommet i est i, la sortie i+l1
		int[][] G2=new int[2*l1-2][2*l1-2];
		int l=G2.length;
		int[][] f=new int[l][l];
		//Initialisation du graphe dédoublé
		for(int i=0;i<l1-2;i++){
			G2[s][i]=c[s][i];
			G2[i+l1][t]=c[i][t];
			for(int j=0;j<l1-2;j++){
				G2[i+l1][j]=c[i][j];	//Arête sortante de i vers j  
			}
			G2[i][i+l1]=inf;	//On ne doit pas être limité au sein d'un seul sommet
			G2[i+l1][i]=inf;	//Ni dans un sens ni dans l'autre
		}
		
		//Application d'Edmund-Karps comme d'habitude
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
		
		//On recondense f vers le graphe original
		int[][]f0=new int[l1][l1];
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
	
	public static int[][] maxFlow2(int[][] gEnvoi, int s, int t){
		//AUTRE VERSION PLUS EFFICACE MAIS DIFFICILE A DEMONTRER 
		//On aura privilégié l'autre version par simplicité de démonstration
		//Algorithme d'Edmund-Karps avec passage en valeurs positives
		//(fonctionne avec des graphes "doublement orientés grâce à la positivation voir fin de l'algo)
		//Complexité en O(n³m) avec n sommets et m arêtes
		int l=gEnvoi.length;
		int[][] Gf=new int[l][l]; //Graphe qu'on modifie au fur et à mesure
		int[][] f=new int[l][l];  //Flot qu'on augmente
		
		//Initialisation
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				Gf[i][j]=gEnvoi[i][j];
			}
		}
		LinkedList<Integer> chemin=BFS(Gf,s,t); //contient le plus court chemin de s à t
		ListIterator<Integer> parcours;
		int sommet,sommet2,capaMax=max;
		
		//Augmentation
		while(!chemin.isEmpty()){	//Tant qu'il existe un chemin
			//Recherche de la capacité du chemin
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				capaMax=Integer.min(capaMax,Gf[sommet][sommet2]);	//minimum des capacités
				sommet=sommet2;
			}
			//Modification de Gf et de f
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				f[sommet][sommet2]+=capaMax;	//On incrémente le flot de capaMax
				f[sommet2][sommet]-=capaMax;	//On décrémente le flot opposé de capaMax
				Gf[sommet][sommet2]-=capaMax;	//On peut d'autant moins passer par l'arête directe
				Gf[sommet2][sommet]+=capaMax;	//Et d'autant plus passer par l'arête opposée pour "annuler" le flot direct
				sommet=sommet2;
			}
			chemin=BFS(Gf,s,t);
			capaMax=max;
		}
		//Positivation du flot : on ne veut plus de valeurs négatives dans le flot
		//Ces valeurs négatives apparaissent :
		//- soit sur des arêtes inexistantes dans le graphe originelle, on s'en débarasse donc.
		//- soit s'il existe une arête directe (u,v) et son opposé dans le graphe de départ,
		//comme f(u,v)=-f(v,u),on peut montrer que supprimer la valeur négative conserve les propriétés de flot
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				f[i][j]=Integer.max(0,f[i][j]);
			}
		}
		return f;
	}
	
	public static int[][] maxFlowWithBound(int[][] c, int bound){
		//Utile pour trouver un flot maximum de value borné supérieurement par bound
		//Voir la partie ou l'on cherche des flots multiples de K notamment
		//PRINCIPE : on ajoute une nouvelle source, qu'on relie à l'ancienne avec une capacité bound
		//Ainsi le flot trouvé ne dépasse pas bound
		int l=c.length;
		int oldSource=l-2,newSource=l-1,oldTarget=l-1,newTarget=l;	//On décale les indices et on crée une nouvelle source
		int[][] c2=new int[l+1][l+1];	//Graphe avec la nouvelle source
		int[][] f2;						//Flot associé
		
		//Initialisation
		for(int i=0;i<l-1;i++){
			for(int j=0;j<l-1;j++){
				c2[i][j]=c[i][j];
			}
			c2[i][newTarget]=c[i][oldTarget];
			c2[newTarget][i]=c[oldTarget][i];
		}
		c2[newSource][oldSource]=bound;
		
		//Calcul du flot maximum
		f2=maxFlow(c2);
		
		//Calcul du flot dans le graphe d'origine
		int[][] f=new int[l][l];
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
	
	
	//HOMOGENEOUS LIFETIME : Chemins aux noeuds disjoints
	
	public ArrayList<LinkedList<Integer>> cheminsDisjoints(){
		//On adapte maxFlow à notre problème
		ArrayList<LinkedList<Integer>> chemins=new ArrayList<LinkedList<Integer>>(); //Stocke les chemins disjoints
		
		//Dédoublement des sommets
		int[][] G2=new int[2*nbSensors+2][2*nbSensors+2];
		for(int i=0;i<nbSensors;i++){
			G2[i][i+nbSensors+2]=1;
			G2[s][i]=Graphe_adjacence[s][i];
			G2[i+nbSensors+2][t]=Graphe_adjacence[i][t];
			for(int j=0;j<nbSensors;j++){
				G2[i+nbSensors+2][j]=Graphe_adjacence[i][j];
			}
		}
		
		//Application d'Edmund-Karps dans le graphe modifié
		int l=G2.length;
		int[] pred=new int[l];	//On va sauvegarder les prédecesseurs de chaque sommet
		//REMARQUE : un sommet peut changer de prédécesseur au cours de l'algorithme,
		//néanmoins, le dernier prédécesseur vu sera le prédécesseur final dans les chemins disjoints
		//il n'y a donc que lui qui nous intéresse
		pred[s]=s;
		LinkedList<Integer> lastPred=new LinkedList<Integer>();	//Permet de sauvegarder les prédécesseurs du puits t,
		//qui seront nécessairement les extrémités des chemins disjoints
		int[][] f=new int[l][l];
		LinkedList<Integer> chemin=BFS(G2,s,t);
		ListIterator<Integer> parcours;
		int sommet,sommet2,capaMax=1; //La capacité maximum des chemins est fixé à 1 par définition 
		while(!chemin.isEmpty()){
			parcours=chemin.listIterator();
			sommet=parcours.next();
			while(parcours.hasNext()){
				sommet2=parcours.next();
				f[sommet][sommet2]+=capaMax;
				f[sommet2][sommet]-=capaMax;
				G2[sommet][sommet2]-=capaMax;
				G2[sommet2][sommet]+=capaMax;
				if(f[sommet][sommet2]>0){   //Si l'arête (sommet,sommet2) est parcouru par un flot positif
					pred[sommet2]=sommet;	//Ce flot risque d'exister dans le flot final d'où le prédecesseur
					if(sommet2==t){
						lastPred.add(sommet);  //Si l'on voit t, on ajoute aux prédecesseurs de t
					}
				}
				sommet=sommet2;
			}
			chemin=BFS(G2,s,t);
		}
		
		//On crée les chemins disjoints en remontant les prédecesseurs de t
		ListIterator<Integer> i=lastPred.listIterator();
		int compteur=0;
		while(i.hasNext()){
			chemins.add(new LinkedList<Integer>());
			chemins.get(compteur).addFirst(t);
			sommet=i.next();
			sommet=pred[sommet];
			while(pred[sommet]!=sommet){
				chemins.get(compteur).addFirst(sommet);
				sommet=pred[sommet]; //On remonte 2 fois les prédecesseurs pour éviter le sommet dédoublé,
				sommet=pred[sommet];
			}
			chemins.get(compteur).addFirst(sommet);
			compteur++;
		}
		return chemins;
	}
	
	public static void question4(){
		System.out.println("REPONSE QUESTION 4");
		long t=System.currentTimeMillis();
		Barrier b=new Barrier("sensornetwork0.doc");
		ArrayList<LinkedList<Integer>> res=b.cheminsDisjoints();
		System.out.println("Temps : "+(System.currentTimeMillis()-t)+" ms");
		System.out.println("Chemins : "+res.toString());
		System.out.println("Temps de vie total : "+(res.size())/b.K);
		System.out.println();
	}
	
	
	//HETEROGENEOUS LIFETIME
		//Trouver le K-route flow maximal
	public int[][] Gp(int p){
		//Création du graphe Gp du sujet partie 4.1
		int[][] g=new int[nbSensors+2][nbSensors+2];
		for(int i=0;i<nbSensors+2;i++){
			for(int j=0;j<nbSensors+2;j++){
				g[i][j]=Integer.min(p,Graphe_adjacence[i][j]);
			}
		}
		return g;
	}
	public int[][] KrouteFlow(){
		//Recherche du K-route flow maximal par dichotomie
		//On part de 0 à gauche et cMax à droite
		int gauche=0,centre=cMax/2,droite=cMax;
		int[][] gCentre=Gp(centre);
		int[][] f = null;
		int v=0;
		while(droite-gauche>1){ //Tant qu'on ne peut pas choisir
			f=maxFlow(gCentre);
			v=value(f)-K*centre;
			if(v>=0){	//La valeur de centre est trop grande
				gauche=centre; //La valeur recherchée se trouve donc plus à droite : on se décale
				centre+=(droite-gauche)/2;
			}
			else{	//La valeur de centre est trop faible
				droite=centre;	//La valeur recherchée se trouve donc plus à gauche : on se décale
				centre-=(droite-gauche)/2;
			}
			gCentre=Gp(centre);
		}
		int p;
		int vf=value(maxFlow(Gp(droite)));
		v=vf-K*droite;
		if(v>=0)p=droite;	//Si on est encore >= 0 à droite on prend le flot de droite car F(p) est croissant
		else p=gauche;	//Sinon on doit prendre le flot de gauche
		return maxFlowWithBound(Gp(p),K*(vf/K));	//On retourne le flot maximal, 
		//mais il faut un flot multiple de K, d'où le bound K*(vf/K)
		
	}
	
		//Trouver un flot faisable et le maximiser
	
	public static int[][] feasibleFlow(int[][]G, int[][] d){
		//Trouve un flot faisable 
		//Création de G'
		int l=G.length;
		int s=l-2,t=l-1;
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
		
		//Calcul du flot maximal
		int[][] f=maxFlow(G2);
		
		//Vérification du caractère saturant
		boolean saturatingFlow=true;
		for(int i=0;i<l;i++){
			saturatingFlow&=f[s2][i]==G2[s2][i];
			saturatingFlow&=f[i][t2]==G2[i][t2];
		}
		if(!saturatingFlow){ //S'il n'est pas saturant pas de solution
			return null;
		}
		
		//Calcul du flot faisable
		int[][] f2= new int[l][l];
		for(int i=0;i<l;i++){
			for(int j=0;j<l;j++){
				if(G[i][j]>0)f2[i][j]=f[i][j]+d[i][j]; //On ne conserve que les arêtes originelles
			}
		}
		return f2;
	}
	
	public static int[][] maxFlowWithDemands(int[][] c, int[][] d){
		//Trouve un flot maximal sous contraintes des demandes
		int l1=c.length;
		int s=l1-2,t=l1-1; //Convention sus-citée
		
		//Calcul d'un flot faisable
		int[][] f0=feasibleFlow(c,d);
		if(f0==null)return null; //Si pas de flot faisable : pas de solutions
		
		//Dédoublement du graphe pour éviter d'avoir au départ une arête dans un sens et
		//dans l'autre sens avec des capacités positives pour les deux sens
		//L'entrée du sommet i est i, la sortie i+l1
		int[][] G2=new int[2*l1-2][2*l1-2];
		int l=G2.length;
		int[][] f=new int[l][l];
		//Initialisation du graphe dédoublé et du flot faisable
		for(int i=0;i<l1-2;i++){
			G2[s][i]=c[s][i]-f0[s][i];
			f[s][i]=f0[s][i];
			//Pas besoin de considérer les arêtes dans l'autre 
			//sens car on ne revient pas sur ses pas quand on sort de s
			
			G2[i+l1][t]=c[i][t]-f0[i][t];
			f[i+l1][t]=f0[i][t];
			//Pas besoin de considérer les arêtes dans l'autre 
			//sens car on ne revient pas sur ses pas quand on entre dans t
			
			for(int j=0;j<l1-2;j++){
				//Arêtes sortantes de i : sens direct où f>=0
				G2[i+l1][j]=c[i][j]-f0[i][j];	//Capacité restante
				f[i+l1][j]=f0[i][j];	//Définition du flot faisable
				
				//Arêtes de retour vers i : sens indirect où f<=0
				G2[j][i+l1]=f[i+l1][j]-d[i][j];	//Lorsqu'on annule le flot direct, 
				//il ne faut pas tomber en dessous de la demande 
				f[j][i+l1]=d[i][j]-f[i+l1][j];	//Le flot disponible pour annuler vaut donc flot-demande 
			}
			
			G2[i][i+l1]=inf;	//On ne doit pas être limité au sein d'un seul sommet
			G2[i+l1][i]=inf;	//Ni dans un sens ni dans l'autre
		}
		
		//Application d'Edmund-Karps comme d'habitude
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
		
		//On recondense f vers le graphe original
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
		//On cherche un flot maximal qui satisfait la demande et dont la valeur est plus faible que bound
		//Algorithme en tout point similaire à maxFlowWithBound
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
	
	//Decomposition d'un K-route Flow
	
	public static ArrayList<int[][]> decomposition(int[][] Kflow,int K){
		//On décompose le K-route flow donné en K-flows élémentaires
		
		
		ArrayList<int[][]> res=new ArrayList<int[][]>();	//Contiendra les elementary K-flows
		int l=Kflow.length;
		
		//on crée les variables utiles
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
		while(v!=0){	//Tant qu'on n'a pas enlevé tout ce qu'on pouvait enlever de f (ie f=0 si f est un multiple de K au départ)
			//Création du graphe intermédiaire et des demandes
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					Gf[i][j]=(int)Math.ceil(((double)f[i][j])/((double)v));
					Df[i][j]=(int)Math.floor(((double)f[i][j])/((double)v));
				}
			}
			gFlow=maxFlowWithDemandsAndBound(Gf,Df,K);	//On cherche un elementary K-flow g dans Gf (donc de valeur K)
			//Calcul de delta
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					if(gFlow[i][j]==0)delta1=Integer.min(delta1,v-f[i][j]);
					else delta2=Integer.min(delta2,f[i][j]);
				}
			}
			delta=Integer.min(delta1,delta2);
			
			//Modification de f et v
			v-=delta;
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					f[i][j]-=delta*gFlow[i][j];
				}
			}
			delta1=inf; //remise à "zéro"
			delta2=inf;	//remise à "zéro"
			
			//Ajout de l'elementary K-flow g dans le résultat
			res.add(new int[l][l]);
			for(int i=0;i<l;i++){
				for(int j=0;j<l;j++){
					res.get(res.size()-1)[i][j]=delta*gFlow[i][j];	//On multiplie l'elementary K-flow par sa valeur associée
				}
			}
		}
		return res;
	}
	
		//Scheduling
	public static ArrayList<LinkedList<Integer>> pathsKflow(int[][] elementaryKflow){
		//Décompose un elementary-Kflow en liste de ses chemins aux noeuds disjoints
		ArrayList<LinkedList<Integer>>res=new ArrayList<LinkedList<Integer>>();
		int l=elementaryKflow.length;
		int pere,rang,indice;
		for(int i=0;i<l;i++){
			if(elementaryKflow[i][l-1]>0){	//Si on a trouvé un prédecesseur du puits
				//On va remonter les prédecesseurs (en les cherchant) et ajouter le chemin trouvé à res
				pere=i;
				res.add(new LinkedList<Integer>());
				rang=res.size()-1;
				res.get(rang).addFirst(l-1);
				while(pere!=l-2){
					res.get(rang).addFirst(pere);
					indice=0;
					while(elementaryKflow[indice][pere]==0){	//On cherche le prédecesseur de père
						indice++;
					}
					pere=indice;
				}
				res.get(rang).addFirst(pere);
			}
		}
		return res;
	}
	
	public HashMap<LinkedList<Integer>,LinkedList<Integer>> scheduling(){
		//Renvoie le scheduling des chemins aux noeuds disjoints qui vont être utilisés
		HashMap<LinkedList<Integer>,LinkedList<Integer>> map=new HashMap<LinkedList<Integer>, LinkedList<Integer>>();
		ArrayList<int[][]> decomposition=decomposition(KrouteFlow(),K);	//On décompose le K-route flot maximal du graphe courant
		ArrayList<LinkedList<Integer>> paths;
		Iterator<LinkedList<Integer>> it;
		LinkedList<Integer> path;
		int[][] elementaryKflow;
		int value;
		int time=0;
		
		//On parcourt tous les elementary K-flow de la décomposition
		for(int i=0;i<decomposition.size();i++){
			elementaryKflow=decomposition.get(i); //Ce n'est pas un véritable elementary K-flow mais un multiple 
			value=value(elementaryKflow)/K; //On récupère le coefficient de l'elementary K-flow 
			paths=pathsKflow(elementaryKflow);	//On sépare l'elementary K-flow en ses chemins disjoints
			it=paths.iterator();
			while(it.hasNext()){
				path=it.next();
				for(int t1=time;t1<time+value;t1++){
					if(!map.containsKey(path))map.put(path,new LinkedList<Integer>());
					map.get(path).add(t1);	//Pour tous les chemins, on ajoute les instants 
					//ou on va activer l'elementary K-flow considéré au scheduling
				}
			}
			time+=value; //elementaryKflow aura tenu value secondes
		}
		lifetimeMax=time;
		return map;
	}
	public static void question12(){
		System.out.println("REPONSE QUESTION 12");
		long t=System.currentTimeMillis();
		Barrier b=new Barrier("sensornetwork1.doc");
		System.out.println("Chemins et temps d'allumage : ");
		System.out.println(b.scheduling());
		System.out.println("Temps de vie total : "+b.lifetimeMax);
		System.out.println("Temps : "+(System.currentTimeMillis()-t)+" ms");
	}
	
	
	
	public static void main(String[] args) {
		question4();
		question12();
	}
}