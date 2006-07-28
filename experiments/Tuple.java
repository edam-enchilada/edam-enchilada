package experiments;

/**
 * Just a cute thing for representing any pair of bits of data.
 * 
 * @author smitht
 */

public class Tuple<K,V> {
	private K key;
	private V value;
	
	
	public Tuple(K key, V value) {
		this.key = key;
		this.value = value;
	}


	public K getKey() {
		return key;
	}


	public void setKey(K key) {
		this.key = key;
	}


	public V getValue() {
		return value;
	}


	public void setValue(V value) {
		this.value = value;
	}
	
	public String toString() {
		return "Tuple["+key.toString()+","+value.toString()+"]";
	}
}
