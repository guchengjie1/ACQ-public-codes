package hku.algo.query2.fpMine;

/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/


import java.util.HashMap;
import java.util.Map;



/**
 * This is an implementation of a CFITree as used by the FPClose algorithm.
 *
 * @see CFINode
 * @see Itemset
 * @see AlgoFPClose
 * @author Philippe Fournier-Viger
 */
public class CFITree {
	
	// List of pairs (item, frequency) of the header table
	Map<Integer, CFINode> mapItemNodes = new HashMap<Integer, CFINode>();
	
	// Map that indicates the last node for each item using the node links
	// key: item   value: an fp tree node
	Map<Integer, CFINode> mapItemLastNode = new HashMap<Integer, CFINode>();
	
	// root of the tree
	CFINode root = new CFINode(); // null node

	// last added itemset
	CFINode lastAddedItemsetNode = null;
	
	/**
	 * Constructor
	 */
	public CFITree(){	
		
	}

	/**
	 * Method to fix the node link for an item after inserting a new node.
	 * @param item  the item of the new node
	 * @param newNode the new node thas has been inserted.
	 */
	private void fixNodeLinks(Integer item, CFINode newNode) {
		// get the latest node in the tree with this item
		CFINode lastNode = mapItemLastNode.get(item);
		if(lastNode != null) {
			// if not null, then we add the new node to the node link of the last node
			lastNode.nodeLink = newNode;
		}
		// Finally, we set the new node as the last node 
		mapItemLastNode.put(item, newNode); 
		
		CFINode headernode = mapItemNodes.get(item);
		if(headernode == null){  // there is not
			mapItemNodes.put(item, newNode);
		}
	}


	/**
	 * Add an itemset to the CFI-Tree
	 * @param itemset the itemset
	 * @param itemsetLength the length of the itemset
	 * @param support the support of the itemset
	 */
	public void addCFI(int[] itemset, int itemsetLength, int support) {
		
		CFINode currentNode = root;
		// For each item in the itemset
		for(int i=0; i < itemsetLength; i++){
			int item = itemset[i];
			
			// look if there is a node already in the FP-Tree
			CFINode child = currentNode.getChildWithID(item);
			if(child == null){ 
				// there is no node, we create a new one
				CFINode newNode = new CFINode();
				newNode.itemID = item;
				newNode.parent = currentNode;
				// remember at which level in the tree that node appears
				newNode.level = i+1;  
				newNode.counter = support; // NEW BY PHILIPPE 2015
				// we link the new node to its parrent
				currentNode.childs.add(newNode);
				
				// we take this node as the current node for the next for loop iteration 
				currentNode = newNode;
				
				// We update the header table.
				// We check if there is already a node with this id in the header table
				fixNodeLinks(item, newNode);	
			}else{ 
				// FPCLOSE:
				// If there is a node already, we update it
				// with the maximum of the support already in the path
				// and the support of the current itemset
				child.counter = Math.max(support, child.counter);
				currentNode = child;
			}
		}
		
//		 SET THE SUPPORT OF THE CFI (the last item)
//		currentNode.counter = support;
		
		// remember that this is the last added itemset
		lastAddedItemsetNode = currentNode;
	}

	/**
	 * Perform the subset test to see if an itemset is a subset of an already
	 * found CFI with the same support
	 * @param headWithP the itemset to be tested
	 * @param headWithPLength the last position to be considered in headWithP
	 * @param the support of the itemset headwithP
	 * @return true if the itemset is not a subset of an already found CFI.
	 */
	public boolean passSubsetChecking(int[] headWithP, int headWithPLength, int headWithPSupport) { 
		// OPTIMIZATION:
		// We first check against the last added itemset
		if(lastAddedItemsetNode != null) {
			boolean isSubset = issASubsetOfPrefixPath(headWithP, headWithPLength, lastAddedItemsetNode, headWithPSupport);
			// if the itemset is a subset of the last added itemset, we do not need to check further
			if(isSubset) {
				return false;
			}
		}
		

		// Find the node list for the first item of the itemset
		Integer firstITem = headWithP[headWithP.length-1];
		
		// OTHERWISE, WE NEED TO COMPARE "headwithP" with all the patterns in the CFI-tree.
		CFINode node = mapItemNodes.get(firstITem);
		// if that last item is not yet in the CFI-tree, it means that "itemset" is not a subset 
		// of some itemset already in the tree
		if(node == null) {
			return true;
		}
		// we will loop over each node by following node links
		do {
			// for a node, we will check if "headwithP" is a subset of the path ending at node
			boolean isSubset = issASubsetOfPrefixPath(headWithP, headWithPLength, node, headWithPSupport);
			// if it is a subset, then "headWithP" is in the CFI-tree, we return false
			if(isSubset) {   
				return false;
			}
			// go to the next itemset to test
			node = node.nodeLink;
		}while(node != null);

		// the itemset is not in the CFI-TREE.  Itemset passed the test!
		return true;
	}

	/**
	 * Check if the itemset headwithP is contained in the path ending at "node" in the CFI-tree
	 * and have the same support
	 * @param headWithP the itemset headwithP
	 * @param headWithPLength the last position to be considered in headWithP
	 * @param node  the node
	 * @param the support of the itemset headwithP
	 * @return true if "headwithP" is contained in the path ending at "node" in the CFI-Tree and has the same support.
	 * Otherwise, false.
	 */
	private boolean issASubsetOfPrefixPath(int[] headWithP, int headWithPLength,CFINode node, int support) {
		// optimization proposed in the fpmax* paper: if there is less than itemset node in that branch,
		// we don't need to check it
		if(node.counter == support && node.level >= headWithPLength) {
			// check if "itemset" is contained in the prefix path ending at "node"
			// We will start comparing from the parent of "node" in the prefix path since
			// the last item of itemset is "node".
			CFINode nodeToCheck = node;
			int positionInItemset = headWithP.length-1;
			int itemToLookFor = headWithP[positionInItemset];
			// for each item in itemset
			do {
				if(nodeToCheck.itemID == itemToLookFor) {
					positionInItemset--;
					// we found the itemset completely, so the subset check test is failed
					if(positionInItemset < 0) {
						return true;
					}
					itemToLookFor = headWithP[positionInItemset];
				}
				nodeToCheck = nodeToCheck.parent;
			}while(nodeToCheck != null);
		}
		return false;
	}
	
	
	@Override
	/**
	 * Method for getting a string representation of the CP-tree 
	 * (to be used for debugging purposes).
	 * @return a string
	 */
	public String toString() {
		return "M"+root.toString("");
	}


}
