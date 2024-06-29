package andrew.cmu.edu.abhineec;

import java.util.ArrayList;
import java.util.List;

/********************************************************************************************************
 * Heap class represents an array implementation of the min-Heap data structure capable of storing
 * Nodes as elements of the heap. The minimum_cost of travelling to the node is used to maintain the
 * min-heap property
 ********************************************************************************************************/
public class Heap {
    private List<Node> heap;

    /**
     * Initializes a new arraylist of nodes representing heap
     **/
    public Heap(){
        this.heap = new ArrayList<Node>();
    }

    /**
     * Method to insert a node into the heap
     * @precondition
     *  heap should have been initialized
     * @postcondition
     *  node is inserted into heap
     **/
    public void add(Node node){
        heap.add(node);
        minHeapify(heap.size()-1);
    }

    /**
     * Method to remove the minimal node into the heap
     * @precondition
     *  heap should contain at least one node in heap
     * @postcondition
     *  minimal node in heap is removed and 0th position in heap
     *  points to node with least cost
     **/
    public Node remove(){
        //assign 0th index = most recently added element in heap
        Node temp = heap.get(0);
        heap.set(0, heap.get(heap.size()-1));
        //remove the most recent position's element
        heap.remove(heap.size()-1);
        if(heap.size()>1)
            minHeapify(0);
        return temp;
    }

    /**
     * Method to return the minimal node into the heap
     * without removing it
     * @precondition
     *  heap should contain at least one node in heap
     * @postcondition
     *  minimal node in heap is returned
     **/
    public Node peek(){
        if(heap.isEmpty())
            return null;
        return heap.get(0);
    }

    /**
     * Method to return true if heap is empty
     **/
    public boolean isEmpty(){
        return heap.isEmpty();
    }

    /**
     * Method to remove a specific node from heap
     * @precondition
     *  heap should contain the node in heap
     * @postcondition
     *  specified node in heap is returned
     **/
    public void remove(Node node){
        int index = heap.indexOf(node);
        heap.set(index, heap.get(heap.size()-1));
        heap.remove(heap.size()-1);
        if(heap.size()>1 && index < heap.size())
            minHeapify(index);
        return;
    }

    /**
     * Method to ensure min-heap property is preserved
     * after any change in the heap array
     * @precondition
     *  heap shouldn't be empty
     * @postcondition
     *  heap array's 0th index points to least cost node
     **/
    private void minHeapify(int index){
        int parentIndex = (index-1)/2;
        int leftChildIndex = (2*index)+1;
        int rightChildIndex = (2*index)+2;
        if(parentIndex>=0 && Double.compare(heap.get(index).minimum_cost, heap.get(parentIndex).minimum_cost) < 0){
            swap(index, parentIndex);
            minHeapify(parentIndex);
        }
        else{
            int smallestValuedIndex = index;
            if(leftChildIndex < heap.size() && Double.compare(heap.get(index).minimum_cost, heap.get(leftChildIndex).minimum_cost) > 0)
                smallestValuedIndex = leftChildIndex;

            if(rightChildIndex < heap.size() && Double.compare(heap.get(smallestValuedIndex).minimum_cost, heap.get(rightChildIndex).minimum_cost) > 0)
                smallestValuedIndex = rightChildIndex;

            if(smallestValuedIndex != index){
                swap(index, smallestValuedIndex);
                minHeapify(smallestValuedIndex);
            }
        }
    }

    /**
     * Helper function to swap nodes in two positions
     */
    private void swap(int i, int j){
        Node temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

}
