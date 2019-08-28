Classe Barrier : Unique classe utilisée qui est associée à un  K-barrier coverage problem

ATTENTION : dans quasiment toutes les fonctions, on utilise la convention que la source est l'avant dernier sommet des graphes, le puits étant le dernier sommet


Constructeurs :

Barrier(String filename) : construit une instance du K-coverage problem à partir d'un nom de fichier. Ce fichier doit utiliser le même format que les fichiers sensornetwork0 et 1 fournis.


Méthodes principales :

ArrayList<LinkedList<Integer>> cheminDisjoints() : retourne une solution du problème "Retourner un maximum de chemins disjoints entre la source et le puits"

int[][] KrouteFlow() : retourne un K-route flow 

HashMap<LinkedList<Integer>,LinkedList<Integer>> scheduling() : retourne les chemins disjoints et leurs instants d'allumage, solution du problème "Maximum lifetime of a robust K-barrier coverage problem"



Fonctions statiques principales (utilisées avec la convention sus-citée sauf BFS) :

LinkedList<Integer> BFS(int[][] G, int start, int goal) : retourne le plus court chemin de s à t dans le graphe G

int value(int[][]flow) : retourne la valeur du flot flow

int[][] maxFlow(int[][] c) : retourne le flot maximal via Edmund-Karps du graphe c (c étant en même temps les capacités)

int[][] maxFlowWithBound(int[][] c, int bound) : retourne un flot maximal inférieur ou égal à bound dans le graphe c

int[][] feasibleFlow(int[][]G, int[][] d) : retourne un flot faisable dans le graphe G avec les demandes d

int[][] maxFlowWithDemands(int[][] c, int[][] d) : retourne un flot maximal dans le graphe c avec les demandes d

int[][] maxFlowWithDemandsAndBound(int[][] c,int[][] d, int bound) : retourne un flot maximal mais inférieur ou égal à bound dans le graphe c avec les demandes d

ArrayList<int[][]> decomposition(int[][] Kflow, int K) : décompose un Kflow, à K donné, en elementaryKflows. Attention : on retourne une liste d'elementary Kflow en sortie, mais ces flots n'ont rien d'élémentaires (i.e. de valeur K) car on renvoie leur coefficient au sein de leur valeur i.e. leur valeur vaut K*coeff de l'elementary Kflow

ArrayList<LinkedList<Integer>> pathsKflow(int[][] elementaryKflow) : décompose un elementary Kflow en ses chemins disjoints

HashMap<LinkedList<Integer>,LinkedList<Integer>> scheduling() : retourne un dictionnaire qui a chaque chemin disjoint associe son scheduling, c'est-à-dire une liste qui contient les instants d'allumage de ce chemin

