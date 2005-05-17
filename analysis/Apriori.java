package analysis;

import java.util.*;

import database.InfoWarehouse;
import database.SQLServerDatabase;

/**
 * Apriori performs an Apriori Association Rules generation on the 
 * given collection.
 * 
 * @author andersbe
 */
public class Apriori extends InfoGenerator 
{
	private float minSupport;
	private int minSupportInt;
	private float minConfidence;
	private int size;
	
	private boolean debug = true;
	
	private ArrayList<ArrayList<Integer>> largeItemsets = 
		new ArrayList<ArrayList<Integer>>();
	private HashSet<Rule> rules =  new HashSet<Rule>();
	
	private static class LengthComparator implements Comparator<Rule>{

		public int compare(Rule arg0, Rule arg1) {
			return arg1.right.size()-arg0.right.size();
		}
		
	}
	
	private static class Rule
	{
		public ArrayList<Integer> left;
		public ArrayList<Integer> right;
		public float confidence;
		public float support;
		
		public Rule(ArrayList<Integer> left, 
				ArrayList<Integer> right,
				float confidence,
				float support)
		{
			this.left = left;
			this.right = right;
			this.confidence = confidence;
			this.support = support;
		}
		
		public boolean equals(Object obj)
		{
			PQComparator comp = new PQComparator();
			Rule rule = (Rule) obj;
			if (comp.compare(rule.left, left) == 0 &&
				comp.compare(rule.right, right) == 0)
			{
				return true;
			}
			else
				return false;				
		}
		
		public String toString()
		{
			String returnThis = new String();
			
			for (int i = 0; i < left.size(); i++)
			{
				returnThis = returnThis + " " + left.get(i);
			}
			
			returnThis = returnThis + " -->";
			for (int i = 0; i < right.size(); i++)
			{
				returnThis = returnThis + " " + right.get(i);
			}
			returnThis = returnThis + " confidence = " + confidence + 
				" support = " + support;
			return returnThis;
		}
	}
	
	Apriori(int collectionID, InfoWarehouse db,
			float minSupport, float minConfidence)
	{
		super(collectionID, db,"AR:minSupport=" + minSupport + 
				"minConfidence=" + minConfidence,"Apriori Results");
		assert(minSupport <= 1.0f && minSupport >= 0.0f) :
			"Error: minSupport must be between 0 and 1";
		assert(minConfidence <= 1.0f && minConfidence >= 0.0f) :
			"Error: minConfidence must be between 0 and 1";
		this.minSupport = minSupport;
		this.minConfidence = minConfidence;
		minSupportInt = (int)
			(minSupport * db.getCollectionSize(collectionID));
		if (debug)
			System.out.println("minSupportInt = " + minSupportInt);
		size = db.getCollectionSize(collectionID);
	}

	@Override
	protected String generateNewDescription() 
	{
		if(debug)
			System.out.println("Starting generateNewDescription()");
		String description = new String();
		
		ArrayList<ArrayList<Integer>> goodItemsets = 
			new ArrayList<ArrayList<Integer>>();
		
		curs.reset();
		// key = location.  value = count
		HashMap<Integer, Integer> largeSingles = 
			new HashMap<Integer, Integer>();
		if (debug)
		{
			System.out.println("Starting curs loop");
		}
		while(curs.next())
		{
			BinnedPeakList bpl = curs.getCurrent().getBinnedList();
			bpl.resetPosition();
			for (int i = 0; i < bpl.length(); i++)
			{
				BinnedPeak bp = bpl.getNextLocationAndArea();
				Integer count = largeSingles.get(new Integer(bp.location));
				if (count == null)
				{
					largeSingles.put(bp.location,1);
				}
				else
				{
					largeSingles.put(bp.location, count + 1);
				}
					
			}
			bpl.resetPosition();
		}
		
		Set<Map.Entry<Integer,Integer>> peakCounts = 
			largeSingles.entrySet();
		Iterator<Map.Entry<Integer,Integer>> iter;
		ArrayList<ArrayList<Integer>> large = 
			new ArrayList<ArrayList<Integer>>(
					db.getCollectionSize(collectionID));
		for (iter = peakCounts.iterator(); iter.hasNext();  )
		{
			Map.Entry<Integer,Integer> entry = iter.next();
			if (entry.getValue() >= minSupportInt)
			{
				ArrayList<Integer> pq = 
					new ArrayList<Integer>();
				pq.add(entry.getKey());
				large.add(pq);
			}
		}
		Collections.sort(large, new PQComparator());
		if (debug)
		{
			printAL(large);
		}
		generateNextLargestItemsets(large);
		
		for (int i = 0; i < largeItemsets.size(); i++)
		{
			ArrayList<Integer> is = largeItemsets.get(i);
			float support = (float) getCount(is) / (float) size;
			
			findRules(
					new ArrayList<Integer>(is), 
					new ArrayList<Integer>(), 
					is, 
					support);
		}
		ArrayList<Rule> ruleList = new ArrayList<Rule>(rules);
		
		Collections.sort(ruleList, new LengthComparator());
		
		for (int i = 0; i < ruleList.size(); i++)
			description = description + ruleList.get(i).toString() + "\n";
		return description;
	}
	
	private void printAL(ArrayList<ArrayList<Integer>> alal)
	{
		System.out.println("alal.size() = " + alal.size());
		int i = 0;
		for (Iterator<ArrayList<Integer>> 
		pqIter = alal.iterator(); 
		pqIter.hasNext();  )
		{
			ArrayList<Integer> pq = pqIter.next();
			
			for (Iterator<Integer> intIter = pq.iterator();
			intIter.hasNext();  )
			{
				System.out.println("pq" + i + " = " + 
						intIter.next());
			}
			i++;
		}
	}
	
	private void generateNextLargestItemsets(
			ArrayList<ArrayList<Integer>> goodItemsets
			)
	{
		largeItemsets.addAll(goodItemsets);
		ArrayList<ArrayList<Integer>> nextGoodItemsets = 
			new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < goodItemsets.size(); i++)
		{
			for (int j = i+1; j < goodItemsets.size(); j++)
			{
				ArrayList<Integer> is1 = goodItemsets.get(i), 
				is2 = goodItemsets.get(j);
				if (compatible(is1,is2))
				{
					ArrayList<Integer> testThis = 
						new ArrayList<Integer>(is1);
					testThis.add(is2.get(is2.size()-1));
					Collections.sort(testThis);
					
					if(satisfiesSupport(testThis))
					{
						nextGoodItemsets.add(testThis);
					}
				}
			}
		}
		Collections.sort(nextGoodItemsets, new PQComparator());
		if (debug)
			printAL(nextGoodItemsets);
		if (nextGoodItemsets.size() > 0)
		{
			generateNextLargestItemsets(nextGoodItemsets);
		}
			
	}
	
	private boolean satisfiesSupport(ArrayList<Integer> is)
	{
		int count = getCount(is);
		
		if (count >= minSupportInt)
			return true;
		else
			return false;
	}
	
	private int getCount(ArrayList<Integer> is)
	{
		int count = 0;
		curs.reset();
		while(curs.next())
		{
			BinnedPeakList bpl = curs.getCurrent().getBinnedList();
			boolean matches = true;
			for (int i = 0; i < is.size(); i++)
			{
				if (bpl.getAreaAt(is.get(i)) == 0.0f)
					matches = false;
			}
			if (matches)
				count++;
		}
		curs.reset();
		
		return count;
	}
	
	private boolean compatible(
			ArrayList<Integer> l1, 
			ArrayList<Integer> l2)
	{
		assert (l1.size() == l2.size()) : 
			"Cannot compare lists of inequal length";
		
		for (int i = 0; i < l1.size()-1; i++)
		{
			if (l1.get(i) != l2.get(i))
			{
				return false;
			}
		}
		return true;
	}
	
	private void findRules(
			ArrayList<Integer> left, 
			ArrayList<Integer> right,
			ArrayList<Integer> is,
			float support)
	{
		if (left.size() == 0)
			return;
		
		for (int i = 0; i < left.size(); i ++)
		{
			ArrayList<Integer> leaveOneOut = 
				new ArrayList<Integer>(left.size()-1);
			ArrayList<Integer> newRight = 
				new ArrayList<Integer>(right);
			for (int j = 0; j < left.size(); j++)
			{
				if (j == i)
					newRight.add(left.get(j));
				else
				{
					leaveOneOut.add(left.get(j));
				}
			}
			float confidence = calculateConfidence(leaveOneOut, is);
			
			if (confidence >= minConfidence)
			{
				Collections.sort(newRight);
				Collections.sort(leaveOneOut);
				Rule r = new Rule(leaveOneOut, newRight, 
						confidence, support);
				if (debug)
				{
					System.out.println(r.toString());
				}
				rules.add(r);
				findRules(leaveOneOut, newRight, is, support);
			}
		}
	}
	
	private float calculateConfidence(
			ArrayList<Integer> left,
			ArrayList<Integer> full)
	{
		int leftCount = getCount(left);
		int fullCount = getCount(full);
		
		
		return (float) fullCount / (float) leftCount;
	}
	
	public static void main(String args[])
	{
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		
		System.out.println("collection size = " + db.getCollectionSize(1));
		Apriori test = new Apriori(1,db,0.4f,0.98f);
		test.generateInfo();
		db.closeConnection();
	}

}
