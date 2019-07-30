package de.comparus.opensource.longmap;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.Objects;

public class HashLongMap<V> implements LongMap<V> {
    /**
     * A count of hash bins by default if not specified on creation.
     */
    private static final int DEFAULT_HASH_SIZE = 64;

    private int hashSize;
    private int size;
    private Entry<V>[] entries;

    @SuppressWarnings("unchecked")
    public HashLongMap(int hashSize) {
        if (hashSize < 1) {
            hashSize = DEFAULT_HASH_SIZE;
        }

        this.hashSize = hashSize;
        this.entries = (Entry<V>[]) new Entry[hashSize];
    }

    @SuppressWarnings("unchecked")
    public HashLongMap() {
        this.entries = (Entry<V>[]) new Entry[DEFAULT_HASH_SIZE];
    }

    public V put(long key, V value) {
        int hash = getHash(key);

        Entry<V> head = getHead(hash);

        if (head == null) {
            entries[hash] = new Entry<>(key, value);
            size++;
            return null;
        } else {
            Entry<V> previous = head;
            while (head.key != key) {
                if (head.next == null) {
                    break;
                }

                previous = head;
                head = head.next;
            }

            if (previous == head) {
                entries[hash] = new Entry<>(key, value);
                return null;
            }

            Entry<V> created = new Entry<>(key, value);
            created.next = previous.next.next;
            previous.next = created;

            return head.value;
        }
    }

    public V get(long key) {
        Entry<V> entry = getEntry(key);
        return entry == null ? null : entry.value;
    }

    public V remove(long key) {
        int hash = getHash(key);

        Entry<V> head = getHead(hash);

        if (head == null) {
            return null;
        }

        if (head.key == key) {
            entries[hash] = head.next;
            size--;
            return head.value;
        }

        Entry<V> previous;
        do {
            previous = head;
            head = head.next;
        } while (head != null && head.key != key);

        if (head == null) {
            return null;
        }

        previous.next = head.next;
        size--;
        return head.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return getEntry(key) != null;
    }

    public boolean containsValue(V value) {
        for (Entry<V> entry : entries) {
            if (entry != null) {
                do {
                    if (Objects.deepEquals(entry.value, value)) {
                        return true;
                    }

                    entry = entry.next;
                } while (entry != null);
            }
        }

        return false;
    }

    public long[] keys() {
        long[] keys = new long[size];
        int index = 0;

        for (Entry<V> entry : entries) {
            if (entry != null) {
                do {
                    keys[index++] = entry.key;
                    entry = entry.next;
                } while (entry != null);
            }
        }

        return keys;
    }

    @SuppressWarnings("unchecked")
    public V[] values() {
        V[] values = (V[]) new Object[size];
        int index = 0;

        for (Entry<V> entry : entries) {
            if (entry != null) {
                do {
                    values[index++] = entry.value;
                    entry = entry.next;
                } while (entry != null);
            }
        }

        V[] result = (V[]) Array.newInstance(values[0].getClass(), size);
        System.arraycopy(values, 0, result, 0, size);
        return result;
    }

    public long size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = null;
        }

        size = 0;
    }

    /**
     * Gets entry by key.
     *
     * @param key long key
     * @return found entry, otherwise null
     */
    private Entry<V> getEntry(long key) {
        Entry<V> head = getHead(getHash(key));

        while (head != null && head.key != key) {
            head = head.next;
        }

        return head;
    }

    /**
     * Returns a head of hash bin.
     *
     * @param hash hash of bin
     * @return tail entry or null if not exists
     */
    private Entry<V> getHead(int hash) {
        return entries[hash];
    }

    /**
     * Calculates hash code from long key.
     *
     * @param key long key
     * @return hash bin number
     */
    private int getHash(long key) {
        int keyHash = Objects.hashCode(key);

        if (hashSize == 0) {
            return keyHash % DEFAULT_HASH_SIZE;
        }

        return keyHash % hashSize;
    }

    @RequiredArgsConstructor
    private static final class Entry<V> {
        private final long key;
        private final V value;
        private Entry<V> next;
    }
}
