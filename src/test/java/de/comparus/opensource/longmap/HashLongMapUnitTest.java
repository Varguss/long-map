package de.comparus.opensource.longmap;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class HashLongMapUnitTest {
    @InjectMocks
    private HashLongMap<String> subject;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void test_common_scenario() {
        assertEquals(0, subject.size());

        int puts = 3;
        for (int i = 1; i <= puts; i++) {
            subject.put(i, "Test #" + i);
            assertEquals("Test #" + i, subject.get(i));
        }

        assertEquals(puts, subject.size());

        long[] expectedKeys = { 1, 2, 3 };
        long[] keys = subject.keys();
        assertArrayEquals(expectedKeys, keys);

        String[] expectedValues = { "Test #1", "Test #2", "Test #3" };
        assertArrayEquals(expectedValues, subject.values());

        assertTrue(subject.containsKey(1));
        assertTrue(subject.containsValue("Test #1"));

        assertFalse(subject.containsKey(666));
        assertFalse(subject.containsValue("Test #666"));

        assertFalse(subject.isEmpty());

        subject.clear();
        assertEquals(0, subject.size());

        for (int i = 0; i < puts; i++) {
            assertNull(subject.get(i));
        }

        assertFalse(subject.containsKey(1));
        assertFalse(subject.containsValue("Test #1"));

        assertTrue(subject.isEmpty());
    }

    @Test
    public void test_put_the_same_key() {
        assertEquals(0, subject.size());

        int puts = 3;
        for (int i = 1; i <= puts; i++) {
            subject.put(1, "Test #" + i);
        }

        assertEquals(1, subject.size());
        assertEquals("Test #3", subject.get(1));
        assertNull(subject.get(2));
        assertNull(subject.get(3));
    }

    @Test
    public void test_remove() {
        assertEquals(0, subject.size());

        int puts = 3;
        for (int i = 1; i <= puts; i++) {
            subject.put(i, "Test #" + i);
        }

        assertEquals(puts, subject.size());

        String removed = subject.remove(1);
        assertEquals("Test #1", removed);
        assertEquals(puts-1, subject.size());
        assertNull(subject.get(1));
    }
}