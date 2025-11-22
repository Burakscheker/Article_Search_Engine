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

    private final double maxLoadFactor;
    private final int hashType;
    private final int collisionType;

    private int primeQ;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    public HashMap(double maxLoadFactor, int hashType, int collisionType) {
        this.capacity = findNextPrime(DEFAULT_INITIAL_CAPACITY);

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

    @Override
    public void put(K key, V value) {
        if ((size + 1.0) / capacity > maxLoadFactor) {
            resize();
        }

        insert(key, value);
    }

    @Override
    public V get(K key) {
        int hashCode = hash(key);
        int step = 1;

        if (collisionType == DH) {
            step = secondaryHash_DH(hashCode);
        }

        for (int i = 0; i < capacity; i++) {
            int index = compress(hashCode + i * step);

            if (keys[index] != null && keys[index].equals(key)) {
                return values[index];
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
            int index = compress(hashCode + i * step);

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

        throw new RuntimeException("Hash table is completely full. Cannot insert key: " + key);
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

        long oldCollisionCount = this.collisionCount;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null) {
                insert(oldKeys[i], oldValues[i]);
            }
        }

        this.collisionCount = oldCollisionCount;
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
        return hash;
    }

    private int hash_PAF(String key) {
        int hash = 0;
        int z = 33;

        String lowerKey = key.toLowerCase();

        for (int i = 0; i < lowerKey.length(); i++) {
            char ch = lowerKey.charAt(i);

            if (ch >= 'a' && ch <= 'z') {
                int charValue = (int) ch - (int) 'a' + 1;
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

    private int compress(int hashCode) {
        return Math.abs(hashCode) % capacity;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        this.capacity = findNextPrime(DEFAULT_INITIAL_CAPACITY);
        this.keys = (K[]) new Object[this.capacity];
        this.values = (V[]) new Object[this.capacity];
        this.size = 0;
        this.collisionCount = 0;
        if (collisionType == DH) {
            updatePrimeQ();
        }
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public long getCollisionCount() {
        return this.collisionCount;
    }

    @Override
    public List<K> getKeys() {
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

    private int findPreviousPrime(int n) {
        for (int i = n - 1; i > 1; i--) {
            if (isPrime(i)) {
                return i;
            }
        }
        return 2;
    }

    private int findNextPrime(int n) {
        int num = n;
        if (num <= 2) return 2;
        if (num % 2 == 0) num++;
        while (!isPrime(num)) {
            num += 2;
        }
        return num;
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i = i + 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}