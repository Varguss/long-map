package de.comparus.opensource.longmap;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class HashLongMap<V> implements LongMap<V> {
    /**
     * A count of hash bins by default if not specified on creation.
     */
    private static final int DEFAULT_HASH_SIZE = 16;

    /**
     * Load factor when we need to scale hash bins.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

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
        this.hashSize = DEFAULT_HASH_SIZE;
        this.entries = (Entry<V>[]) new Entry[DEFAULT_HASH_SIZE];
    }

    /**
     * Puts a value to entry by key. If entry by key already exists, replaces this entry with new one. Returns previous value or null if absent.
     * @param key key
     * @param value value
     * @return previous value or null if none
     */
    public V put(long key, V value) {
        int hash = getHash(key);

        Entry<V> head = getHead(hash);

        if (head == null) {
            entries[hash] = new Entry<>(key, value);

            size++;
            resizeIfNeeded();

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

            size++;
            resizeIfNeeded();

            return head.value;
        }
    }

    /**
     * @param key Gets value by key.
     * @return value
     */
    public V get(long key) {
        Entry<V> entry = getEntry(key);
        return entry == null ? null : entry.value;
    }

    /**
     * Removes an entry from map if exists, otherwise null. If exists, returns removed value.
     * @param key
     * @return
     */
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

    /**
     * Checks if map is empty.
     * @return true - empty, false - not empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks that map contains key.
     * @param key key to search for
     * @return true - contains, false - does not contain
     */
    public boolean containsKey(long key) {
        return getEntry(key) != null;
    }

    /**
     * Checks that map contains value.
     * @param value value to search for
     * @return true - contains, false - does not contain
     */
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

    /**
     * Returns an array of keys.
     * @return keys
     */
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

    /**
     * Returns an array of values.
     * @return values
     */
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

    /**
     * Gets current size of map.
     * @return map size
     */
    public long size() {
        return size;
    }

    /**
     * Clears map from entries.
     */
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

    private void resizeIfNeeded() {
        if (size > hashSize * DEFAULT_LOAD_FACTOR) {
            resize();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        hashSize *= 2;

        Entry<V>[] buffer = (Entry<V>[]) new Entry[entries.length];
        System.arraycopy(entries, 0, buffer, 0, entries.length);

        entries = (Entry<V>[]) new Entry[hashSize];
        Arrays.stream(buffer)
                .filter(Objects::nonNull)
                .forEach(entry -> this.put(entry.key, entry.value));
    }

    @RequiredArgsConstructor
    private static final class Entry<V> {
        private final long key;
        private final V value;
        private Entry<V> next;
    }
}
