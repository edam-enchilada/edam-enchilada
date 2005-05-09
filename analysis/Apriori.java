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
	private float minSupportInt;
	private float minConfidence;
	
	private boolean debug = true;
	
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
		minSupportInt = minSupport * db.getCollectionSize(collectionID);
	}

	@Override
	protected String generateNewDescription() {
		String description = new String();
		
		ArrayList<ArrayList<Integer>> goodItemsets = 
			new ArrayList<ArrayList<Integer>>();
		
		curs.reset();
		// key = location.  value = count
		HashMap<Integer, Integer> largeSingles = 
			new HashMap<Integer, Integer>();
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
		PriorityQueue<PriorityQueue<Integer>> large = 
			new PriorityQueue<PriorityQueue<Integer>>(
					db.getCollectionSize(collectionID),
					new PQComparator());
		for (iter = peakCounts.iterator(); iter.hasNext();  )
		{
			Map.Entry<Integer,Integer> entry = iter.next();
			if (entry.getValue() >= minSupportInt)
			{
				PriorityQueue<Integer> pq = 
					new PriorityQueue<Integer>();
				pq.add(entry.getKey());
				large.add(pq);
			}
		}
		if (debug)
		{
			printPQ(large);
		}
		return description;
	}
	
	private void printPQ(PriorityQueue<PriorityQueue<Integer>> pqpq)
	{
		int i = 0;
		for (Iterator<PriorityQueue<Integer>> 
		pqIter = pqpq.iterator(); 
		pqIter.hasNext();  )
		{
			PriorityQueue<Integer> pq = pqIter.next();
			
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
			PriorityQueue<PriorityQueue<Integer>> goodItemsets
			)
	{
		PriorityQueue<PriorityQueue<Integer>> potentialItemsets = 
			new PriorityQueue<PriorityQueue<Integer>>();
		for (int i = 0; i < goodItemsets.size(); i++)
		{
		
		}
	}
	
	public static void main(String args[])
	{
		InfoWarehouse db = new SQLServerDatabase();
		db.openConnection();
		
		System.out.println("collection size = " + db.getCollectionSize(1));
		Apriori test = new Apriori(1,db,0.5f,0.9f);
		db.closeConnection();
	}

}
