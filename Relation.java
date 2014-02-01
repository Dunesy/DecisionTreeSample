import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

class Attribute
{
	public String attributename;
	public ArrayList<String> values;
	
	//Constructor
	public Attribute(String name, ArrayList<String> values)
	{
		attributename = name;
		this.values = values;		
	}
	
	public void print(){
		System.out.println(attributename + "  " + values);
	}
	
}

public class Relation 
{
	public static String ATTRIBUTE = "@attribute";
	public static String DATA = "@data";
	public static String RELATION = "@relation";

	String relation;
	ArrayList<Attribute> attributes;
	Attribute class_attribute;
	ArrayList<ArrayList<String>> tuples;
	ArrayList<ArrayList<String>> sample;
	ArrayList<ArrayList<String>> testing;
	
	//Constructor
	public Relation(String s){
		attributes = new ArrayList<Attribute>();
		tuples = new ArrayList<ArrayList<String>>();
		s = relation;
		class_attribute = null;
		sample = new ArrayList<ArrayList<String>>();
		testing = new ArrayList<ArrayList<String>>();
	}

	public void setName(String s) { relation = s; } 
	
	public void addAttribute(String attribute){
		StringTokenizer tokens = new StringTokenizer(attribute);
		String attribute_name = tokens.nextToken();
		String temp;
		ArrayList<String> values = new ArrayList<String>();
		
		
		Pattern p = Pattern.compile("\\{.*\\}");
		Matcher m = p.matcher(attribute);
		if (m.find()){
			temp = m.group();
			temp = temp.substring(1, temp.length()-1);
			tokens = new StringTokenizer(temp , ",");
			while(tokens.hasMoreTokens()){
				String line = tokens.nextToken();
				line = line.trim();				
				if (line.startsWith("'"))
					line = line.substring(1, line.length() - 1);
				values.add(line);	
			}
		}else{
			values.add(tokens.nextToken());  
		}
		attributes.add(new Attribute(attribute_name, values));		
	}
	
	public void setClass(){
		class_attribute = attributes.remove(attributes.size()-1);
	}
	
	public void printAttributes(){
		for (Attribute a : attributes){
			a.print();
		}
	}
	
	public void printTuples(){
		for (ArrayList<String> t : sample){
			System.out.println(t);
		}
	}
		
	public void createSampleandTest(double fraction)
	{
		for (ArrayList<String> t : tuples){
			sample.add(t);
		}
		int counter = 0;
		int index = 0;
		Random rand = new Random();
		while (counter != Math.floor(fraction * tuples.size())){
			index = rand.nextInt(sample.size());
		//	testing.add(sample.remove(sample.size()-1));
			testing.add(sample.remove(index));
			counter++;
		}
	}

	
	public void addTuple(String s){
		ArrayList<String> values = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(s, ",");
		String line;
		while (tokens.hasMoreTokens())
		{
			line = tokens.nextToken();
			line = line.trim();			
			if (line.startsWith("'"))
				line = line.substring(1, line.length() - 1);
			values.add(line);
		}
		tuples.add(values);		
	}
	
	public void addUnknowns(){
		for (Attribute a : attributes)
		{
			a.values.add("?");
		}
	}

}
