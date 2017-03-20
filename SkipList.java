/*
	Madeleine Cassidy
	COP 3503 - Computer Science 2
	Dr. Sean Szumlanski
	SkipList Program
*/

import java.io.*;
import java.util.*;

class Node<AnyType> 
{
	// I know every node has a height and a data being stored, as well as 
	// an array of next's, and an index for every level of the tower.
	
	private int height;
	private AnyType data;
	// the head of my arraylist is always going to be the base of the node
	private ArrayList<Node<AnyType>> next = new ArrayList<Node<AnyType>>();

	// The height will always be greater than 0
	Node(int height)
	{
		this.height = height;
		
		for (int i = 0; i < height; i++)
			next.add(null);
	}
	
	Node(AnyType data, int height)
	{
		this.data = data;	
		this.height = height;
		
		for (int i = 0; i < height; i++)
			next.add(null);
	}
	
	// returns the node's data
	public AnyType value()
	{
		return this.data;
	}
	
	// returns the node's height
	public int height()
	{
		return this.height;
	}
	
	// returns the node stored at the level indicated
	public Node<AnyType> next(int level)
	{
		if((level < 0) || (level >= height())) 
			return null;
	
		return next.get(level);
	}
	
	// Set the next reference at the given level within this node to node.
	public void setNext(int level, Node<AnyType> node)
	{
		next.set(level, node);
	}
	
	// Grow this node by exactly one level.
	public void grow()
	{
		this.height += 1;
		next.add(null);
	}
	
	// Grow this node by exactly one level iff math.random gives an even number
	public void maybeGrow()
	{
		int number = (int) (Math.random() * Integer.MAX_VALUE);
		
		if (number % 2 == 0)
			grow();
	}

	// trim this node by however many levels are needed to get to target height
	public void trim(int height)
	{
		while (height < next.size())
		{
			next.remove(next.size() - 1);
			this.height = this.height - 1;
		}
	}
}

public class SkipList <AnyType extends Comparable<AnyType>>
{
	private Node<AnyType> head;
	private int size;
		
	// This constructor creates a new skip list. 
	// The height of the skip list is initialized to 1
	SkipList()
	{
		head = new Node<AnyType>(1);
	}
	
	// This constructor creates a new skip list of a specified height.
	// Any negative numbers are treated like a height of 1.
	SkipList(int height)
	{
		if (height < 1)
			head = new Node<AnyType>(1);
		
		head = new Node<AnyType>(height);
	}
	
	// return the number of nodes in the skip list
	public int size()
	{
		return this.size;
	}
	
	// returns the height of the head node
	// which is the maximum height of the skiplist
	public int height()
	{
		return head().height();
	}

	// returns head of skiplist
	public Node<AnyType> head()
	{
		return this.head;
	}
	
	public void insert(AnyType data)
	{
		// put the maximum size in ht
		int ht = getMaxHeight(size());
		
		// if ht is smaller then the current height of the skiplist, 
		// replace ht with the head.height()
		ht = (head.height() > ht) ? head.height() : ht;
		
		// use that maximum height to generate a random height
		ht = generateRandomHeight(ht);
		
		// use that height to insert into the skip list
		insert(data, ht);
	}
	
	// The function takes the data and height, creates a node, 
	// and then inserts that node into the skiplist
	public void insert(AnyType data, int height)
	{
		// node that we're inserting
		Node<AnyType> insertnode = new Node<AnyType>(data, height);
		Node<AnyType> temp = head();
		
		// keeps track of the nodes that were dropped from
		LinkedList<Node<AnyType>> dropped = new LinkedList<>();
		int j = temp.height();
		boolean inserted = false;
		
		// This for loop determines all the the points that *may* connect with 
		// and places them in a linked list 
		for (int i = 0; i <= size() && !inserted; )
		{
			// only do if we can drop levels without going below base of node
			while (j > 0)
			{							
				// temp connects to a null -> add temp to LL and decrease level
				if (temp.next(j-1) == null)
				{
					dropped.add(temp);
					j--;
				}
			
				// if value < data, we want to insert after the next tower. 
				else if (i <= size() && temp.next(j-1).value().compareTo(data) < 0)
				{
					temp=temp.next(j-1);
					i++;
				}
						
				// if value >= data, we want to insert before the next tower
				else if (temp.next(j-1).value().compareTo(data) >= 0)
				{
					dropped.add(temp);
					j--;
				}	
			}
				
			// at this point j = 0, and all of the points that need to be 
			// changed are in linked list
			for (int cnt = head().height(); cnt > 0; cnt--)  
			{
				Node<AnyType> pass = dropped.pollFirst();
		
				// If the node that we are inserting is at the level of the
				// connection stored in "dropped" or below, then reconnect 
				// skiplist here and set inserted to true. Inserted keeps us
				// from continuing through the skiplist after we have already 
				// added our node
				if (cnt <= height && pass != null)
				{
					insertnode.setNext(cnt-1, pass.next(cnt-1));
					pass.setNext(cnt-1, insertnode);
					inserted = true;
				}
			}
		}
		
		// increase size and possibly grow skiplist if needed.
		this.size += 1;
		growSkipList();
	}

	// Return a reference to a node in the skip list that contains data. 
	// If no node exists, return null. If multiple such nodes exist, return 
	// the first such node that would be found.
	public Node<AnyType> get(AnyType data)
	{
		Node<AnyType> traveler = head();
		int level = height()-1;

		while (traveler != null)
		{
			if (traveler.next(level) == null)
			{
				if (level == 0)
					return null;

				level--;
			}
			
			// if the parameter data is smaller than the value stored in the node
			else if (traveler.next(level).value().compareTo(data) > 0)
			{
				if (level == 0)
					return null;
				
				level--;
			}
			
			// if the parameter data is bigger than the value stored in the node
			else if (traveler.next(level).value().compareTo(data) < 0)
			{
				traveler = traveler.next(level);
			}
			
			// if the parameter is equal to the value stored in the node
			else
			{
				return traveler.next(level);
			}
		}
		
		// this method failed to find the node containing data
		return null;
	}
	
	// determines in log n time if data is stored within the skiplist
	public boolean contains(AnyType data)
	{
		return (get(data) == null) ? false : true;
	}
	
	// returns the max height of a skip list with n nodes.
	private static int getMaxHeight(int n)
	{
		int maxheight = (int)(Math.ceil(Math.log(n) / Math.log(2)));
		
		if (n == 0 || maxheight == 0) 
			return 1;
		
		return maxheight;
	}

	// Returns 1 with 50% probability, 2 with 25% probability, 3 with 12.5% 
	// probability, and so on, without exceeding maxHeight.
	private static int generateRandomHeight(int maxHeight)
	{
		int numb;
		
		for (int i = 1; i <= maxHeight; i++)
		{
			numb = (int) (Math.random() * Integer.MAX_VALUE);
			if (numb % 2 == 1)
				return i;
		}
		
		return maxHeight;
	}

	// Grow the skip list for the insert() method.
	private void growSkipList()
	{
		int currentSkipListSize = size();
		int possibleNewMaxHeight = getMaxHeight(currentSkipListSize);
		int oldMaxHt = head().height();

		if (oldMaxHt < possibleNewMaxHeight)
		{
			// if we are here, then new possible max height is greater than 
			// old max height. Now must alter every node with max height
			
			head().grow();
			Node<AnyType> nod = head();
			Node<AnyType> AttachTopLevel = head();
			
			// oldMaxHt - 1 is the topmost level of any node in my SkipList
			// go through the top levels of skip list and grow if it is needed
			for (int n = oldMaxHt - 1; nod.next(n) != null; nod = nod.next(n))
			{
				nod.next(n).maybeGrow();

				// adjust the new top level linkage if I need to 
				if (AttachTopLevel.height() == nod.next(n).height())
				{
					AttachTopLevel.setNext(n+1, nod.next(n));
					AttachTopLevel = nod.next(n);
				}
			}
		}
	}
	 
	public void delete(AnyType data)
	{
		boolean deleted = false;
		Node<AnyType> traveler = head();
		Node<AnyType> temp = null;
		int level = height() - 1;
		int deletedNodesHeight = 0;
		LinkedList<Node<AnyType>> points = new LinkedList<>();
		
		// finds all the points that connect to the node I want to delete
		while (level >= 0)
		{							
			if (traveler.next(level) == null)
			{
				points.add(traveler);
				level--;
			}
			
			// if parameter data is bigger than the value stored in the node
			else if (traveler.next(level).value().compareTo(data) < 0)
			{
				traveler=traveler.next(level);
			}
			
			// if parameter data is smaller than the value stored in the node			
			else if (traveler.next(level).value().compareTo(data) > 0)
			{
				points.add(traveler);
				level--;
			}	
			
			// if the parameter is equal to the value stored in the node
			else 
			{
				points.add(traveler);
				temp = traveler.next(level);
				deletedNodesHeight = traveler.next(level).height();
				level--;
			}
		}
					
		// delete the node
		for (int cnt = head().height(); cnt > 0 && temp != null; cnt--)  
		{
			Node<AnyType> pass = points.pollFirst();
	
			if (cnt <= deletedNodesHeight && pass != null)
			{
				pass.setNext(cnt-1, temp.next(cnt-1));
				deleted = true;
			}
		}
		
		if (deleted)
		{	
			this.size = this.size - 1;
			trimSkipList();
		}
	}
	
	// Trim the skip list for the delete() method
	private void trimSkipList()
	{
		int NewMaxHeight = getMaxHeight(size());
		int OldMaxHeight = head().height();
		Node<AnyType> traveler = head();
		Node<AnyType> temp;

		if (OldMaxHeight > NewMaxHeight)
		{
			// if we have reached here, then the new possible max height is 
			// less than the old max height and must alter every node that 
			// has the maxed out height.
			
			// go through skip list and decrease height to target height
			while(traveler != null)
			{
				temp = traveler.next(NewMaxHeight);
				traveler.trim(NewMaxHeight);
				traveler = temp;
			}
		}
	}
	 
	 // This function prints out the SkipList
	public void printSkipList(SkipList<AnyType> s)
	{
		Node<AnyType> nodey;
		
		for (int n = s.height() - 1; n >= 0; n--)
		{
			nodey = s.head();
			
			while (nodey.next(n) != null)
			{
				System.out.print(" " + nodey.value());
				nodey = nodey.next(n);
			}
			System.out.print(" " + nodey.value());
			System.out.println("");
		}
		
		System.out.println("");
	}
	 
	public static double difficultyRating()
	{
		return 3.0;
	}
	
	public static double hoursSpent()
	{
		return 32.5;
	}
	
}

