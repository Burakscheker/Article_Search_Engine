import java.util.List;

public interface MyMap<K, V> {

    void put(K key, V value);

    V get(K key);

    int size();

    boolean isEmpty();

    void clear();

    int getCapacity();

    List<K> getKeys();

    long getCollisionCount();
}