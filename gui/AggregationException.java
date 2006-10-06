package gui;

import collection.Collection;

public class AggregationException extends Exception {
	Collection collection;
	AggregationException(Collection collection){
		this.collection = collection;
	}
}
