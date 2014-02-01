import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import sun.security.provider.SystemSigner;

class Node{
	
	//Node Reference Information//
	public Relation relation;
	//Identifying Value of Traits of the Node at Current Level
	public ArrayList<Attribute> attributes;
	public ArrayList<ArrayList<String>> current_tuples;
	
	public ArrayList<Attribute> best_cut;
	public ArrayList<String> attribute_values;
	public ArrayList<ArrayList<Integer>> changedindices;
	//Pointers to Map Out The Decision Tree
	public Node predecessor;
	public ArrayList<Node> successors;
	//Splicing value (Used for Cuts with Continuous Values.
	public double splicevalue, splicevalue2;
	public int depth = 0;
	public double infogain = 1;
	public String[] common_element;
	public Node(Relation r, ArrayList<Attribute> attributes){
		
		relation = r;
		splicevalue = 0; splicevalue2 = 0;
		depth = 0 ; 
		// Values To Help Sort Out the Decision Tree
		attribute_values = new ArrayList<String>();
		changedindices = new ArrayList<ArrayList<Integer>>();
		common_element = new String[2];
		
		best_cut = null;
		this.attributes = new ArrayList<Attribute>();
		for (Attribute a : attributes)
			this.attributes.add(a);
		
		predecessor = null;
		successors = new ArrayList<Node>();
		attribute_values = new ArrayList<String>();
		this.current_tuples = new ArrayList<ArrayList<String>>();
	}
	
	//Methods
	public void setPredecessor(Node n){
		predecessor = n;
	}
	public void setBestCut(ArrayList<Attribute> cut) { best_cut = cut; }
	public void addSuccessor(Node node){successors.add(node);}
	public void setDepth(int depth) {this.depth = depth;}
	public void removeExploredAttributes(ArrayList<Attribute> att) { 
		for (Attribute a : att) { attributes.remove(attributes.indexOf(a)); }
	}
	
	public ArrayList<ArrayList<String>> Query(ArrayList<Attribute> atts, ArrayList<String> att_values, ArrayList<String> operations, ArrayList<ArrayList<String>> current_tuples)
	{
		ArrayList<ArrayList<String>> queryset = new ArrayList<ArrayList<String>>();
		boolean[] match = new boolean[atts.size()];
		
		for (ArrayList<String> t : current_tuples)
		{		
			match[0] = false;
			if (match.length == 2) match[1] = false; 
			
			for (int i = 0 ; i < atts.size() ; i++)
			{
				int index = relation.attributes.indexOf(atts.get(i));
				if (operations.get(i).equals("Match")){		
					if (att_values.get(i).equals(t.get(index)) || t.get(index).equals("?") && common_element[i].equals(att_values.get(i))) { match[i] = true;}		
				}
				else if (operations.get(i).equals("<=")) {
					if ( Double.parseDouble(t.get(index)) <= Double.parseDouble(att_values.get(i))) {match[i] = true; }
				} else {
					if ( Double.parseDouble(t.get(index)) > Double.parseDouble(att_values.get(i))) { match[i] = true; }
				}
			}
			if (match.length == 1 && match[0]){
				queryset.add(t);				
			}else if (match.length == 2 && match[0] && match[1]){
				queryset.add(t);	
			}
		}
		return queryset;
	}
	
	public void pruneTuples(ArrayList<Attribute> attri, String value, String operation, ArrayList<ArrayList<String>> tuples)
	{
		String[] values = new String[attri.size()];
		String[] operations = new String[attri.size()];
		boolean[] match = new boolean[attri.size()];
		values = value.split(" ");
		operations = operation.split(" ");
		for (ArrayList<String> t : tuples)
		{		
			match[0] = false;
			if (match.length == 2) match[1] = false;
			for ( int i = 0 ; i < operations.length;  i++){	
				int index = relation.attributes.indexOf(attri.get(i));
				if (operations[i].equals("Match")){
					if (values[i].equals(t.get(index))) { match[i] = true;}		
				}
				else if (operations[i].equals("<=")) {
					if ( Double.parseDouble(t.get(index)) <= Double.parseDouble(values[i])) {match[i] = true; }
				} else {
					if ( Double.parseDouble(t.get(index)) > Double.parseDouble(values[i])) { match[i] = true; }
				}
			}
			if (match.length == 1 && match[0]){
				current_tuples.add(t);				
			}else if (match.length == 2 && match[0] && match[1]){
				current_tuples.add(t);	
			}
		}	
	}
	
	public boolean isLeaf(Attribute class_attribute){
		
		int counters[] = new int[class_attribute.values.size()];
		
		if (attributes.isEmpty() || this.infogain == 0)
			return true;
		for (ArrayList<String> t : current_tuples)
			for (int i = 0 ; i < class_attribute.values.size(); i++)
				if (class_attribute.values.get(i).equals(t.get(t.size()-1)))
					counters[i]++;
		boolean single_value = false;
				
		for (int i = 0 ; i < class_attribute.values.size();i++)
		{
			if (single_value == false && counters[i] > 0)
				single_value = true;
			else if (single_value == true && counters[i] > 0)
				return false;
		}
		
		
		
		return true;
		
	}
	
	public String leafType(Attribute class_attribute)
	{
		int counters[] = new int[class_attribute.values.size()];
		if (current_tuples.isEmpty())
			return "Uknown";
		
		for (ArrayList<String> t : current_tuples)
			for (int i = 0 ; i < class_attribute.values.size(); i++)
				if (class_attribute.values.get(i).equals(t.get(t.size()-1)))
					counters[i]++;
		
		boolean single_value = false;
		for (int i = 0 ; i < class_attribute.values.size();i++)
		{
			if (single_value == false && counters[i] > 0)
				single_value = true;
			else if (single_value == true && counters[i] > 0)
				return "Unknown";
		}		
		
		for (int i = 0 ; i < class_attribute.values.size() ; i++)
			if (counters[i] > 0)
				return class_attribute.values.get(i);
		return "Uknown";
	}
	
	public String[] polymorphUknowns(ArrayList<Attribute> atts)
	{
		String[] common_element = new String[atts.size()];
		int c = 0;
		//ArrayList<ArrayList<String>> tuples = (ArrayList<ArrayList<String>>)current_tuples.clone();
		for (Attribute a: atts)
		{	
			int[] counter = new int[a.values.size()];
			int index = relation.attributes.indexOf(a);
			int index2 = 0;
			for (ArrayList<String> t : current_tuples)
			{
				for (int i = 0 ; i < a.values.size(); i++)
					if (t.get(index).equals(a.values.get(i)))
					{
						counter[i]++;
					}	
			}
			int bestcount = 0 ;
			for (int i = 0 ; i < counter.length ; i++)
			{
				if (counter[i] > bestcount)
				{
					bestcount = counter[i];
					index2 = i;
				}
			}
			common_element[c] = a.values.get(index2);
			c++;
		}
		return common_element;
	}
	
	public double Gain(ArrayList<Attribute> atts)
	{	
		double gain = 0;
		ArrayList<ArrayList<String>> queryset = new ArrayList<ArrayList<String>>();
		ArrayList<String> att_values = new ArrayList<String>();
		ArrayList<String> operations = new ArrayList<String>();
		Attribute class_type = null;
		Attribute real_type = null;
		double[] midpoints, midpoints2;
		double best_midpoints[] = new double[2];
		
		//Convert Unknowns to Most Common
		this.common_element = polymorphUknowns(atts);
		//
		
		if (atts.size() == 1 && atts.get(0).values.get(0).compareToIgnoreCase("real") == 0)
		{
			midpoints = getMidpoints(atts.get(0));
			bestMidpoints(atts.get(0), null, midpoints, null);
			return this.infogain;	
		}
		else if (atts.size() == 2 && atts.get(0).values.get(0).compareToIgnoreCase("real") == 0 && atts.get(1).values.get(0).compareToIgnoreCase("real") == 0)
		{
			midpoints = getMidpoints(atts.get(0));
			midpoints2 = getMidpoints(atts.get(0));
			bestMidpoints(atts.get(0), atts.get(1), midpoints , midpoints2);
			return this.infogain;
		}
		else if (atts.size() == 2 && (atts.get(0).values.get(0).compareToIgnoreCase("real") == 0 || atts.get(1).values.get(0).compareToIgnoreCase("real") == 0))
		{
			if (atts.get(0).values.get(0).compareToIgnoreCase("real") == 0){
				midpoints = getMidpoints(atts.get(0));
				class_type = atts.get(1);
				real_type = atts.get(0);
				
			}else { 
				midpoints = getMidpoints(atts.get(1));
				class_type = atts.get(0);
				real_type = atts.get(1);
			}
			bestMidpoints(class_type, real_type, midpoints, null);
			return this.infogain;
		}
		
		if (atts.size() == 1)
		{
			operations.add("Match");
			for (int i = 0 ; i < atts.size(); i++)
			{	
				ArrayList<String> vals = atts.get(i).values;
				for (String value : vals)
				{									
						att_values.add(value);
						queryset = Query(atts, att_values, operations, current_tuples);
						gain += Information(queryset);
						att_values.clear();
				}
			} 	
		}
		else if (atts.size() == 2)
		{
			
			for (String value1 : atts.get(0).values)
				for (String value2 : atts.get(1).values)
				{
					operations.add("Match"); operations.add("Match");
					att_values.add(value1); att_values.add(value2);
					queryset = Query(atts, att_values, operations, current_tuples);
					gain += Information(queryset); operations.clear(); att_values.clear();
				}
		}
		return Information(current_tuples) - gain;		
	}
	
public double Information(ArrayList<ArrayList<String>> tuples){
		
		double set_size = (double) tuples.size();
		double big_set = (double) current_tuples.size();
		ArrayList<String> values = relation.class_attribute.values;
		double counter[] = new double[values.size()];
		for (ArrayList<String> t : tuples){
			for (String class_val : relation.class_attribute.values){
				if (class_val.equals(t.get(t.size() - 1)))
						counter[values.indexOf(t.get(t.size() - 1))]++;
				}
		}
		double entropy = 0;
		for (int i = 0 ; i < counter.length; i++){
			if (counter[i] == 0)
				entropy -= 0;
			else
			{
				entropy -= Entropy(counter[i], set_size) * (set_size/big_set);
				//System.out.println("Entropy " + Entropy(counter[i],set_size) + " counter " + counter[i] + " set_size " + set_size);
			}
		}
		
		//System.out.println(entropy);
		return entropy;
}

public double Entropy(double count, double size)
{	
	double entropy = 0;
	entropy = (count/size) * Math.log(count / size) / Math.log(2);
	return entropy;
}

public double[] getMidpoints(Attribute a)
{
	double[] midpoints = new double[current_tuples.size() -1];
	double[] numbers = new double[current_tuples.size()];
	int index = relation.attributes.indexOf(a);
		
	for (int i = 0 ; i < numbers.length; i++)
		numbers[i] = Double.parseDouble(current_tuples.get(i).get(index));		
	
	Arrays.sort(numbers);
	
	for (int i = 0 ; i < numbers.length - 1 ; i++){
		midpoints[i] = (numbers[i] + numbers[i+1])/2;
	}
	return midpoints;
}

public double[] bestMidpoints(Attribute a, Attribute b, double[] midpoints, double[] midpoints2)
{
	ArrayList<Attribute> atts = new ArrayList<Attribute>(); atts.add(a); 
	if (b != null)
		atts.add(b);
	
	ArrayList<String> att_vals = new ArrayList<String>();
	ArrayList<String> operands = new ArrayList<String>();
	double[] best_mids = new double[2];
	double Set_Entropy, best_info = 0;
	ArrayList<ArrayList<String>> tuples;
	
	Set_Entropy = Information(current_tuples);
	if (b == null && midpoints2 == null)
	{
		for (int i = 0 ; i < midpoints.length ; i++)
		{
			double info = 0;
			att_vals.add(midpoints[i]+"");
			operands.add("<=");
			tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
			info += Information(tuples);
			
			operands.add(">");
			tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
			info += Information(tuples);
			if (best_info < (Set_Entropy - info))
			{
				best_info = Set_Entropy - info; best_mids[0] = midpoints[i];
				splicevalue = midpoints[i];
			}
			att_vals.clear();		
		}
	}
	else if (a.values.get(0).compareToIgnoreCase("real") != 0 && b.values.get(0).compareToIgnoreCase("real") == 0)
	{	for ( int i = 0 ; i < midpoints.length ; i++)
		{
			for (String value : a.values)
			{	
				double info = 0;
				att_vals.add(value); att_vals.add(midpoints[i]+""); 
				
				operands.add("Match") ; operands.add("<="); 
				tuples = Query(atts, att_vals, operands, current_tuples); operands.clear(); 
				info += Information(tuples);
				
				operands.add("Match"); operands.add(">");
				tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
				info += Information(tuples);
				
				if (best_info < (Set_Entropy - info))
				{
					best_info = Set_Entropy - info; best_mids[0] = midpoints[i];
					splicevalue = midpoints[i];
				}
				att_vals.clear();
			}
		}
	}	
	else
	{
		for (int i = 0 ; i < midpoints.length ; i++)
		{
			for (int j = 0 ; j < midpoints2.length ;j++)
			{
				double info = 0;
				att_vals.add(midpoints[i]+""); att_vals.add(midpoints2[i]+"");
				
				operands.add("<=") ; operands.add(">");
				tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
				info += Information(tuples);
				
				operands.add("<="); operands.add("<=");
				tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
				info += Information(tuples);
				
				operands.add(">"); operands.add(">");
				tuples = Query(atts, att_vals, operands, current_tuples);operands.clear();
				info += Information(tuples);
				
				operands.add(">"); operands.add("<="); 
				tuples = Query(atts, att_vals, operands, current_tuples); operands.clear();
				info += Information(tuples);
				
				if (best_info < (Set_Entropy - info))
				{
					best_info = Set_Entropy - info; best_mids[0] = midpoints[i]; best_mids[1] = midpoints[j];
					splicevalue = midpoints[i]; splicevalue2 = midpoints2[j];
				}
				att_vals.clear();
			}
		}
	}
	
	this.infogain = best_info;
	return best_mids; 
}


	public ArrayList<Attribute> decideOnCut()
	{
		int temp = 0; double splitvalue1 = 0 , splitvalue2 = 0;
		double value = 0, value2 = 0;
		ArrayList<Attribute> bestargs = new ArrayList<Attribute>(); 
		Attribute x = null , y = null;
		Random random = new Random();
		
		
		///Sets The Probability that more than one attribute is used
		if (attributes.size() > 1)
		{
			temp = random.nextInt(11);
			if (temp > 7)
				temp = 1;
			else
				temp = 0;
		}
		
		
		ArrayList<Attribute> args = new ArrayList<Attribute>();
		if (temp == 0)
		{
			//Single Variable Cut			
			for (Attribute a : attributes)
			{
				args.add(a);
				double info = 0;
				//polymorphUknowns(args);
				value = Gain(args);
				if (value > value2)
				{	
					value2 = value; 
					x = a;
					if (x.values.get(0).compareToIgnoreCase("real") == 0)
						splitvalue1 = splicevalue;
					else 
						splitvalue1 = 0;
				} 
				args.clear();
			}
			splicevalue = splitvalue1;
			this.infogain = value2;
		}else{
			for (Attribute a : attributes){
				for (Attribute b : attributes){
					if (!a.attributename.equals(b.attributename)){
						args.add(a); args.add(b);
						
						value = Gain(args);
						if (value > value2)
						{
							value2 = value;
							x = a; 
							y = b;
							if (x.values.get(0).compareToIgnoreCase("real") == 0 && y.values.get(0).compareToIgnoreCase("real") == 0)
								{splitvalue1 = splicevalue; splitvalue2 = splicevalue2;}
							else if (x.values.get(0).compareToIgnoreCase("real") == 0)
								{splitvalue1 = splicevalue; splitvalue2 = 0;}
							else if (y.values.get(0).compareToIgnoreCase("real") == 0)
								{splitvalue2 = splicevalue; splitvalue1 = 0;}
							else 
								{splitvalue2 = 0; splitvalue1 = 0;}
						}
						args.clear();							
					}
				}
			}
			infogain = value2;
			if (x.values.get(0).compareToIgnoreCase("real") == 0)
				splicevalue = splitvalue1;
			if (x.values.get(0).compareToIgnoreCase("real") == 0)
				splicevalue2 = splitvalue2;
		}		
		if (x != null)
			bestargs.add(x);
		if (y != null)
			bestargs.add(y);
		/*
		bestargs.clear();
		int choice = random.nextInt(attributes.size());
		Attribute choice_att = attributes.get(choice);
		bestargs.add(choice_att);
		*/
		return bestargs;	
	}
	
	public void print()
	{
		System.out.print("Best Cut At Current Node: ");
		if (!isLeaf(relation.class_attribute) && best_cut != null){			
			for (int i = 0; i < best_cut.size() ; i++)
			{
				System.out.print(best_cut.get(i).attributename + " ");
			}
			System.out.print ("\n");
		//	System.out.println (current_tuples);
		}else {
			System.out.println("Leaf Node");
		}	
	}
	
}

public class DecisionTree{

	Relation r;
	ArrayList<ArrayList<String>> tuples;
	ArrayList<Node> nodes;
	ArrayList<Node> queue;
	
	public DecisionTree(Relation r, ArrayList<ArrayList<String>> tuples)
	{
		queue = new ArrayList<Node>();
		nodes = new ArrayList<Node>();
		Node n = new Node(r, r.attributes);
		n.current_tuples = tuples;
		
		nodes.add(n);
		queue.add(n);
		this.r = r;
		this.tuples = tuples;
	}
	
	public void generateNodes(Node n)
	{
		ArrayList<Attribute> best_attributes;
		String operation;
		double[] midpoints;
		double[] temp_mid, temp_mid2;
		String vals; 
		if (!n.isLeaf(r.class_attribute)){  //Check to see if Node is a Leaf
			best_attributes = n.decideOnCut();	
			if (best_attributes.size() == 0)
				return;
			
			n.setBestCut(best_attributes);			
			//public void pruneTuples(ArrayList<Attribute> attri, String value, String operation, ArrayList<ArrayList<String>> tuples)
			//Remove Attributes From Next Level
			if (best_attributes.size() == 2) {   //Two Values Conjucated
				for ( int i = 0 ; i < best_attributes.get(0).values.size() ; i++)
				{
					String value1 = best_attributes.get(0).values.get(i);
					for ( int j = 0 ; j < best_attributes.get(1).values.size() ; j++)
					{	
						String value2 = best_attributes.get(1).values.get(j);												
						//Prepare The New Node						
						Node m;
						if (value1.compareToIgnoreCase("real") == 0 && value2.compareToIgnoreCase("real") == 0){
							vals = n.splicevalue + " " + n.splicevalue2; 
							
							m = new Node(r, n.attributes); operation = "<= <="; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("> " + n.splicevalue); m.attribute_values.add("<= " + n.splicevalue2);
							m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
							
							m = new Node(r, n.attributes); operation = "> >"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("> " + n.splicevalue); m.attribute_values.add("> " + n.splicevalue2); 
							m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
							
							m = new Node(r, n.attributes); operation = "<= >"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("<= " + n.splicevalue); m.attribute_values.add("> " + n.splicevalue2);
							m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
							
							m = new Node(r, n.attributes); operation = "> <="; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("> " + n.splicevalue); m.attribute_values.add("<= " + n.splicevalue2);
							
						} else if (value1.compareToIgnoreCase("real") == 0) {
							vals = n.splicevalue + " " + value2;
							
							m = new Node(r, n.attributes); operation = "<= Match"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("<= " + n.splicevalue); m.attribute_values.add(value2);
							m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
							
							
							m = new Node(r, n.attributes); operation = "> Match"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add("> " + n.splicevalue); m.attribute_values.add(value2);
							
						}	else if (value2.compareToIgnoreCase("real") == 0) {
							vals = value1 + " " + n.splicevalue;
							
							m = new Node(r, n.attributes); operation = "Match <="; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add(value1); m.attribute_values.add("<= " + n.splicevalue);
							m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
							
							m = new Node(r, n.attributes); operation = "Match >"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add(value1); m.attribute_values.add("> " + n.splicevalue);
							
						}
						else {
							vals = value1 + " " + value2;
							m = new Node(r, n.attributes); operation = "Match Match"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
							m.attribute_values.add(value1); m.attribute_values.add(value2); 
							//System.out.println(value1 + " " + value2 + " " + m.current_tuples);
						}
						m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
					}
				}
			}else{
				for ( int i = 0 ; i < best_attributes.get(0).values.size(); i++){
					String value1 = best_attributes.get(0).values.get(i);
					//Prepare the New Node
					Node m;
					if (value1.compareToIgnoreCase("real") == 0){
						vals = n.splicevalue + "";
						m = new Node(r, n.attributes); operation = "<="; m.pruneTuples(best_attributes, vals, operation, n.current_tuples);
						m.attribute_values.add("<= " + vals); m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
						//System.out.println("<= REAL VALUE MIDPOINT "+  n.splicevalue);
					
						m = new Node(r, n.attributes); operation = ">"; m.pruneTuples(best_attributes, vals, operation, n.current_tuples); 
						m.attribute_values.add("> " + vals); m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
						//System.out.println("> REAL VALUE MIDPOINT "+  n.splicevalue);
					} else {						
						m = new Node(r, n.attributes); operation = "Match"; m.pruneTuples(best_attributes, value1, operation, n.current_tuples); 
						m.attribute_values.add(value1); m.setPredecessor(n); m.removeExploredAttributes(best_attributes); n.addSuccessor(m); queue.add(m); nodes.add(m); m.setDepth(n.depth + 1);
					}
				}
			}
		}
	}
	
	public void createTree(){	
		long time = System.currentTimeMillis();
		while (!queue.isEmpty()){
			//Choose Best Attribute to Cut With
			Node n;
			n = queue.remove(0);
			//System.out.println(n.current_tuples);
			generateNodes(n);
		}
		time = System.currentTimeMillis() - time ;
		inOrder(nodes.get(0));
		System.out.println("Time took to build tree is " + time + " milliseconds.");
	}
	
	public void writeToFile()
	{
		
		File f = new File("Output.dot");
		BufferedWriter out = null;
		String s = "digraph{\n";
		try {
			out = new BufferedWriter(new FileWriter(f));
		for (Node n : nodes)
		{	
			if (!n.isLeaf(r.class_attribute))
			{
				s += "Node" + nodes.indexOf(n) + " [label=\"";
				for (Attribute a : n.best_cut)
					s += a.attributename + " ";
				s += "\"];\n";
				for (Node m : n.successors)
				{
					s += "Node" + nodes.indexOf(m) + " [label=\"";
					if (!m.isLeaf(r.class_attribute)){ 
						for (Attribute a : m.best_cut)
							s += a.attributename + " ";
						s += "\"];\n";
					} else {
							s += m.leafType(r.class_attribute) + "\"];\n";
					}
				}
				for (Node m : n.successors)
				{
					s += "Node" + nodes.indexOf(n) + " -> ";
					s += "Node" + nodes.indexOf(m);
					s += "[ label=\"" ; 
					s += m.attribute_values + " ";
					s += "\"];\n";
				}
			}
		}
		s += "}";
		out.write(s);
	
		out.newLine();
		
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
            try { 
			if (out != null) {
                    out.flush();
                    out.close();
                }
		 } catch (IOException ex) {
             ex.printStackTrace();
         }
		}
		
	}
	
	
	public void inOrder(Node n){
		String value1 = "" , value2 = "";
		if (n.predecessor != null){
			value1 = n.predecessor.best_cut.get(0).attributename;
			if(n.predecessor.best_cut.size() == 2)
				value2 = n.predecessor.best_cut.get(1).attributename;				
		}
		
		for (int i = 0 ; i < n.depth; i++){ System.out.print("\t");}
		System.out.println("Attributes Used: " + value1 + " " + value2 + "------> " + n.attribute_values);
		
		if (n.isLeaf(r.class_attribute)){
			for (int i = 0 ; i < n.depth; i++){ System.out.print("\t");}
			System.out.println(" Leaf -> " + n.leafType(r.class_attribute) + "\n");
			return;
		}
			
		if (n.best_cut.size() > 1){	
			for (int i = 0 ; i < n.depth; i++){ System.out.print("\t");}
			System.out.println(" Best Cut is " + n.best_cut.get(0).attributename + " " + n.best_cut.get(1).attributename + " With Info Gain of " + n.infogain);
		}else{
			for (int i = 0 ; i < n.depth; i++){ System.out.print("\t");}
			System.out.println(" Best Cut is " + n.best_cut.get(0).attributename + " With Info Gain of " + n.infogain);
		}
		System.out.println("");
		
		for (Node m : n.successors){
			inOrder(m);
		}
	}
		
	public double TestHypothesis(ArrayList<ArrayList<String>> testset, Node n)
	{
		double accuracy = 0;
		double counter = 0;
		
		accuracy = CorrectHypothesis(n, testset);  			
		return accuracy/((double)testset.size());
	}
	
	public double CorrectHypothesis(Node n, ArrayList<ArrayList<String>> set)
	{
		int c = 0 ;
		double counter = 0;
		if (n.isLeaf(r.class_attribute))
		{
				for (ArrayList<String> t : set)
				{
					if(n.leafType(r.class_attribute).equals(t.get(t.size()-1)))
						counter++;
				}
				
				return counter;
		}
		ArrayList<Attribute> atts = n.best_cut;
		for (Node m : n.successors)
		{
			ArrayList<String> attvals = m.attribute_values;
			ArrayList<String> operands = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			
			
			for ( int i = 0 ; i < attvals.size(); i++)
			{
				if (atts.get(0).values.get(0).compareToIgnoreCase("real") == 0)
				{
					StringTokenizer tokens = new StringTokenizer(attvals.get(i));
					operands.add(tokens.nextToken());
					values.add(tokens.nextToken());
				}
				else{
					operands.add("Match");
					values.add(attvals.get(i));
				}	
			}
				counter += CorrectHypothesis( m, n.Query(atts, values, operands, set)); 
		}
		
		return counter;
	}
	
	
	public static void main( String args[]) throws IOException
	{
		FileReader fstream = new FileReader(new File("mushroom_data.txt"));
		BufferedReader in = new BufferedReader(fstream);
		String line = null;
		Relation r = new Relation("");
		boolean data = false;
		
		if (!data)
			while (in.ready())
			{
				line = in.readLine();
				line.trim();
				
				if (line.length() > 4 || data)
					if (line.charAt(0) != '%')
					if (data){				
							r.addTuple(line);
					}	
					else if (line.substring(0, Relation.DATA.length()).compareToIgnoreCase(Relation.DATA) == 0){
							data = true;
					}				
					else if (line.substring(0, Relation.ATTRIBUTE.length()).compareToIgnoreCase(Relation.ATTRIBUTE) == 0){
						r.addAttribute(line.substring(Relation.ATTRIBUTE.length()));
					}
					else if (line.substring(0, Relation.RELATION.length()).compareToIgnoreCase(Relation.RELATION) == 0){
						r.setName(line.substring(Relation.RELATION.length(), Relation.RELATION.length()));
					}
			}
		r.setClass();
		//r.addUnknowns();
		r.createSampleandTest(.5);
		DecisionTree dt = new DecisionTree(r, r.sample);
		dt.createTree();
		System.out.println(dt.TestHypothesis(r.testing, dt.nodes.get(0)));
		dt.writeToFile();
		System.out.println("Number of Nodes. " + dt.nodes.size());
	}
		
}
