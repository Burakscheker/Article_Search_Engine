
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

    private static final int BASLANGIC_KAPASITE = 131;

    public HashMap(double maxLoadFactor, int hashType, int collisionType) {
        this.capacity = findNextPrime(BASLANGIC_KAPASITE);

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

        long eskiCollision = this.collisionCount;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null) {
                insert(oldKeys[i], oldValues[i]);
            }
        }
        this.collisionCount = eskiCollision;
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
        hash = hash * 100;
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

    private int compress(int hashCode) {
        long positiveHash = hashCode;
        if (positiveHash < 0) positiveHash = -positiveHash;
        return (int) (positiveHash % capacity);
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
        this.capacity = findNextPrime(BASLANGIC_KAPASITE);
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
            if (isPrime(i)) return i;
        }
        return 2;
    }

    private int findNextPrime(int n) {
        if (n % 2 == 0) n++;
        while (!isPrime(n)) {
            n += 2;
        }
        return n;
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;

        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}