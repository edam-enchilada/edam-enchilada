package externalswing;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Useful {
	/**
	 * Searches an awt Container for all instances of the given Class.  This
	 * could be useful for the case when you want to add a mouse listener to
	 * every ChartArea.
	 * <p>
	 * Bad things might happen if this is run while the component hierarchy 
	 * is being modified, so you probably want to run this from the EDT.
	 * 
	 * @param type the class that you are looking for
	 * @param here the container to search for it.
	 * @return every instance of type in the container.
	 * @author smitht
	 */
	public static List<Component> findAll(Class type, Container here) {
		Queue<Component> components = new LinkedList<Component>();
		components.addAll(Arrays.asList(here.getComponents()));
		Component curr;
		LinkedList<Component> found = new LinkedList<Component>();
		
		// breadth-first search
		while (components.peek() != null) {
			curr = components.poll();
			
			if (type.isInstance(curr))
				found.add(curr);
			
			if (curr instanceof Container)
				components.addAll(Arrays.asList(
						((Container) curr).getComponents()));
		}
		return found;
	}
	
}
