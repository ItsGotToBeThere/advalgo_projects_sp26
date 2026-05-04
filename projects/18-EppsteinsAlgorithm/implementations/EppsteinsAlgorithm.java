import java.util.*;
/*

I think at the end of the day i had to use the cf blog. Yes, it's convoluted, but i guess this algorithm overall is.

FOr logic, I used  https://www.w3tutorials.net/blog/eppstein-s-algorithm-and-yen-s-algorithm-for-k-shortest-paths/
to get the logic down.

This was a bit simpler and a lot "to the point" on what i needed to do. 
ok but on the actual enumeration part that was sort of hard so i referneced the cf blog a bit.

For a bit more detail on what I should do, the CF blog was def helpful.


Also i forgot how to use java so:
https://www.geeksforgeeks.org/java/priority-queue-in-java/
https://www.geeksforgeeks.org/java/treeset-in-java-with-examples/
https://www.geeksforgeeks.org/java/treemap-in-java/
https://ayushgupta2959.medium.com/java-8-custom-comparable-and-comparator-fbe3da2ed664


Graph example is from
https://www.tutorialspoint.com/article/weighted-graph-representation-in-data-structure
Delete the edge from 3-> 4.

Because leftist heap is used a lot:
https://www.geeksforgeeks.org/dsa/leftist-tree-leftist-heap/

*/
public class EppsteinsAlgorithm {

    // This is just a temporary class needed to sort. all we really
//. need to do wit this is assign a heap to a priority in a PQ and let sort, hence needing comparable
    static class Heappair implements Comparable<Heappair>{
        int value;
        Heap heap;
        public  Heappair(int value, Heap heap){
            this.value = value;
            this.heap = heap;
        }
         // standard comparision about the priority for PQ sorting
        @Override
        public int compareTo(Heappair other){
            return Integer.compare(this.value, other.value);
        }
    }
    static class Heap{
        public Heap left = null; 
        public Heap right = null; 
        public int cost;
        public int targetnode;
        public int dist;
        public Heap(int cost, int targetnode){
            this.cost = cost;
            this.targetnode = targetnode;
            this.dist = 1;

        }
        public void insert(int cost, int targetnode){
            Heap out = merge(new Heap(cost,targetnode), this);
            //full overwrite
            this.left = out.left;
            this.right = out.right;
            this.cost = out.cost;
            this.targetnode = out.targetnode;
            this.dist = out.dist;
        }
        // this is a leftist heap. quite tricky.
        public static Heap merge(Heap left, Heap right){
            // tree should not be null and must be left weighted. This means that
            // there are more nodes of lower cost on the left. 
            if(left == null){return right;}
            if(right == null){return left;}
            if(left.cost > right.cost){
                Heap temp = left; left = right; right = temp;
            }
            // We can just ensure that the less number (more priority) branch ends up on top by choosing
            // the better of the two branches at each level then going through the chosen one's (right) child and the nonchosen.
            left.right = merge(left.right, right);
            if(left.left == null){
                left.left = left.right;
                left.right = null;
            }
            else{
                 // logic here is to try to ensure that heap has a proper left of possible and the left is deeper than the right.
                if(left.right == null || (left.left != null && left.left.dist < left.right.dist)){
                    Heap temp = left.left;
                    left.left = left.right;
                    left.right = temp;
                }
                 //label distance of node after transformation
                left.dist = (left.right == null ? 1 : left.right.dist + 1);
            }
            return left;

        }   

    }    

    public static List<TreeMap<Integer,Integer>> djikstra(TreeMap<Integer,ArrayList<int[]>> graph, Integer src){

        //basic djikstra. assign tenporary values that the nodes have huge cost and no way to get to it.
        TreeMap<Integer, Integer> costs = new TreeMap<Integer,Integer>();
        TreeMap<Integer, Integer> parents = new TreeMap<Integer,Integer>();
        for(int node : graph.keySet()){
            parents.put(node, null);
            costs.put(node, Integer.MAX_VALUE);
        }
        //starting with the source node, put its info in the PQ and get its neighbors to explore next
        PriorityQueue<int[]> pq = new PriorityQueue<>((x,y)->Integer.compare(x[0],y[0])); // (totcost, node, parent)
        TreeSet<Integer> covered = new TreeSet<Integer>();
        costs.put(src,0);
        parents.put(src,null);
        covered.add(src);
        for(int[] nbr : graph.get(src)){
            pq.offer(new int[]{nbr[1],nbr[0],src});
        }
        //go through the least cumulative cost node, say we got to it, and add itd neighbors cost to the  PQ to explore those.
        while(pq.peek()!=null){
            int[] top = pq.poll();
            if(!covered.contains(top[1])){
                int curcost = top[0];
                int node = top[1];
                int parent = top[2];
                covered.add(node);
                costs.put(node,curcost);
                parents.put(node, parent);
                for(int[] nbrdata : graph.get(node)){
                    int nbr = nbrdata[0];
                    int edgecost = nbrdata[1];
                    pq.add(new int[]{curcost+edgecost,nbr, node});
                }
            }
        }
        return List.of(costs,parents); //just to pack the dist from node to rest and the parents of the visiting tree. 
    }

    public static void main(String[] args){

        TreeMap<Integer,ArrayList<int[]>> graph = new TreeMap<Integer,ArrayList<int[]>>(); // node: (nbr, cost);
        for(int i=1;i<=5;i++){graph.put(i, new ArrayList<int[]>());}
        //load graph

        //your numbers go here and so does your graph.
        int src=5, dest=3, k=5;
        //graph.get(node).add(new int[]{nbr, cost});
        graph.get(1).add(new int[]{4,3});
        graph.get(1).add(new int[]{3,6});
        graph.get(2).add(new int[]{1,3});
        graph.get(4).add(new int[]{2,1});
        graph.get(4).add(new int[]{3,1});
        graph.get(5).add(new int[]{2,4});
        graph.get(5).add(new int[]{4,2});

        TreeMap<Integer,ArrayList<int[]>> invgraph = new TreeMap<Integer,ArrayList<int[]>>(); // node: (nbr, cost);
        for(int i=1;i<=5;i++){invgraph.put(i, new ArrayList<int[]>());}
        // we need to run djikstra on the inverse graph since we need to determine cheapest way to get to t from here. construct it by
        // saying an edge (a,b) is now edge (b,a) in thr inverse graph.
        for(int node : graph.keySet()){
            for(int[] nbrdata : graph.get(node)){
                invgraph.get(nbrdata[0]).add(new int[]{node, nbrdata[1]});
            }
        }

        //do djikstra and all.
        List<TreeMap<Integer,Integer>> djout = djikstra(invgraph, dest);
        TreeMap<Integer,Integer> dists = djout.get(0);
        TreeMap<Integer,Integer> parents = djout.get(1);
        // we need to take note of which children are ublocked by which parent node. helpful for calculating detours.
        TreeMap<Integer, ArrayList<Integer>> childlist = new TreeMap<Integer, ArrayList<Integer>>(); 
        for(int node : graph.keySet()){
            childlist.put(node, new ArrayList<Integer>());
        }
        for(int node : parents.keySet()){
            if(parents.get(node)!=null){
                childlist.get(parents.get(node)).add(node);
            }
        }

        //every vertex gets its heap of the best routes to take from there.
        TreeMap<Integer, Heap> heaps = new TreeMap<Integer, Heap>();
        LinkedList<Integer> vertexqueue = new LinkedList<Integer>();
        vertexqueue.addLast(dest);
        while(!vertexqueue.isEmpty()){
            int node = vertexqueue.pollFirst();
            // for each child of the node, the detour cost is thr difference beteeen its min-djikstra distance and that of the parent. this is added to current cost for total detour cost
            for(int[] data : graph.get(node)){  
                int child = data[0];
                int cost = data[1];
                if(dists.get(child) == Integer.MAX_VALUE) continue;
                int detourcost = cost + dists.get(child) - dists.get(node);
                // Skip tree edge only when detour cost is 0
                if(parents.get(node) != null && parents.get(node).equals(child) && detourcost == 0) continue;
                if(!heaps.containsKey(node)){
                    heaps.put(node, new Heap(detourcost, child));
                } else {
                    heaps.put(node, Heap.merge(new Heap(detourcost, child), heaps.get(node)));
                }
            }
            // the reason we calculate children is so we know childs csn inherit parents' possibke detours. 
            for(int child : childlist.get(node)){
                heaps.put(child, Heap.merge(heaps.get(child), heaps.get(node)));
                vertexqueue.addLast(child);
            }
        }
     
        ArrayList<Integer> answers = new ArrayList<Integer>();
        answers.add(dists.get(src)); //answer 1: djikstra;

        // PQ so we can explore heaps (having less total detouring cost) first 
        PriorityQueue<Heappair> explorequeue = new PriorityQueue<Heappair>();

        //store as current distnace, the heap we use.

        //why is this null bruh. i had to do some null check here since java complains.
        if(heaps.get(src)!=null){
            explorequeue.add(new Heappair(dists.get(src) + heaps.get(src).cost, heaps.get(src)));
            while(answers.size() < k && !explorequeue.isEmpty()){
                Heappair data = explorequeue.poll();
                int curcost = data.value;
                Heap curheap = data.heap;
                answers.add(curcost);
                //options: 
                /*
                - left child with its detour -> cost incorporates difference of best travel cost of both nodes since that is effectivr codt of switching (from current to left node)
                - right child with its detour -> see above
                - current detour -> add the current heaps detour cost to the current cost
                */
                if(curheap.left!=null){
                    explorequeue.add(new Heappair(curcost + curheap.left.cost - curheap.cost, curheap.left));
                }
                if(curheap.right!=null){
                    explorequeue.add(new Heappair(curcost + curheap.right.cost - curheap.cost, curheap.right));
                }
                if(heaps.get(curheap.targetnode)!=null){
                    explorequeue.add(new Heappair(curcost + heaps.get(curheap.targetnode).cost, heaps.get(curheap.targetnode)));
            
                }
            }
        } // print out array of cost
        System.out.println("Answers");
        System.out.println(answers);

    }
}
