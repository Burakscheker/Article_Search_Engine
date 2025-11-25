import java.util.ArrayList;
import java.util.List;

public class HashMap<K, V> implements MyMap<K, V> {

	public static final int SSF = 0;
	public static final int PAF = 1;

	public static final int LP = 0;
	public static final int DH = 1;

	private K[] keys;
	private V[] values;

	private int capacity;
	private int size;
	private long collisionCount;

	private double maxLoadFactor;
	private int hashType;
	private int collisionType;

	private int primeQ;

	private static final int initial_capacity = 131; // asal sayı

	public HashMap(double maxLoadFactor, int hashType, int collisionType) {
		this.capacity = findNextPrime(initial_capacity);

		this.maxLoadFactor = maxLoadFactor;
		this.hashType = hashType;
		this.collisionType = collisionType;

		this.keys = (K[]) new Object[this.capacity];
		this.values = (V[]) new Object[this.capacity];

		this.size = 0;
		this.collisionCount = 0;

		if (collisionType == DH) {
			updatePrimeQ();
		}
	}

	public void put(K key, V value) {
		if ((size + 1.0) / capacity > maxLoadFactor) { // kapasite load factoru asıyorsa once resize yap{
			resize();
		}
		insert(key, value); 
	}

	public V get(K key) {
		int hashCode = hash(key);
		int step = 1;

		if (collisionType == DH) { // eğer double hashşing yapıyorsak step 1 değil 2. fonksiyonun değeri olur 
			step = secondaryHash_DH(hashCode);
		}

		for (int i = 0; i < capacity; i++) {
			int index = Math.abs(hashCode + i * step) % capacity;

			if (keys[index] != null) {
				if (keys[index].equals(key)) {
					return values[index];
				}
			}

			if (keys[index] == null) {
				return null;
			}
		}
		return null;
	}

	private void insert(K key, V value) {
		int hashCode = hash(key);
		int step = 1;

		if (collisionType == DH) {
			step = secondaryHash_DH(hashCode);
		}

		for (int i = 0; i < capacity; i++) {
			int index =Math.abs(hashCode + i * step)% capacity;

			if (i > 0) {
				this.collisionCount++;
			}

			if (keys[index] == null) {
				keys[index] = key;
				values[index] = value;
				size++;
				return;
			}

			if (keys[index].equals(key)) {
				values[index] = value;
				return;
			}
		}
	}

	private void resize() {
		K[] oldKeys = this.keys;
		V[] oldValues = this.values;

		this.capacity = findNextPrime(this.capacity * 2);

		this.keys = (K[]) new Object[this.capacity];
		this.values = (V[]) new Object[this.capacity];
		this.size = 0;

		if (collisionType == DH) {
			updatePrimeQ();
		}

		long oldCollision = this.collisionCount;  // resize yaparken olan carpısmaları gormezden gelmemiz için tutuyoruz 

		for (int i = 0; i < oldKeys.length; i++) {
			if (oldKeys[i] != null) {
				insert(oldKeys[i], oldValues[i]);
			}
		}
		this.collisionCount = oldCollision;   // kaldıgımız carpısma sayısından devam 

	}

	private int hash(K key) {
		String s = (String) key;

		if (hashType == PAF) {
			return hash_PAF(s);
		} else {
			return hash_SSF(s);
		}
	}

	private int hash_SSF(String key) {
		int hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash += (int) key.charAt(i);
		}
		hash = hash * 1000003;// Mailde sorduğumuzda kullanabiliceğimizi söylemiştiniz. collision azaltmak
								// için kullanıldı
		return hash;
	}

	private int hash_PAF(String key) {
		int hash = 0;
		int z = 41;

		for (int i = 0; i < key.length(); i++) {
			char ch = key.charAt(i);
			int charValue = 0;

			if (ch >= 'a' && ch <= 'z') {
				charValue = (int) ch - (int) 'a' + 1;
			} else if (ch >= 'A' && ch <= 'Z') {
				charValue = (int) ch - (int) 'A' + 1;
			}

			if (charValue > 0) {
				hash = (hash * z) + charValue;
			}
		}
		return hash;
	}

	private int secondaryHash_DH(int hashCode) {
		int h = Math.abs(hashCode);
		if (h % primeQ == 0) {
			return 1;
		}
		return primeQ - (h % primeQ);
	}

	public int getCapacity() {
		return this.capacity;
	}

	public long getCollisionCount() {
		return this.collisionCount;
	}

	public List<K> getKeys() {  // searchengine classında kullan
		List<K> keyList = new ArrayList<>();
		for (int i = 0; i < capacity; i++) {
			if (keys[i] != null) {
				keyList.add(keys[i]);
			}
		}
		return keyList;
	}

	private void updatePrimeQ() {
		this.primeQ = findPreviousPrime(this.capacity);
	}

	private int findPreviousPrime(int n) {  // q capacityden küçük en büyük asal sayı olmalı
		for (int i = n - 1; i > 1; i--) {
			if (isPrime(i))
				return i;
		}
		return 2;
	}

	private int findNextPrime(int n) {  // resize yaptıgımzda asal sayı yapmak için kullanılır 
		if (n % 2 == 0)
			n++;
		while (!isPrime(n)) {
			n += 2;
		}
		return n;
	}

	private boolean isPrime(int n) {
		if (n <= 1)
			return false;

		for (int i = 2; i * i <= n; i++) {
			if (n % i == 0)
				return false;
		}
		return true;
	}
}