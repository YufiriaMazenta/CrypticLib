package crypticlib.particle.utils;

public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K key() {
        return key;
    }

    public Pair<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    public V value() {
        return value;
    }

    public Pair<K, V> setValue(V value) {
        this.value = value;
        return this;
    }
}
