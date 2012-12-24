package org.junit.runner.manipulation;

import java.util.Comparator;

import org.junit.runner.Description;

/**
 * A <code>Sorter</code> orders tests. In general you will not need
 * to use a <code>Sorter</code> directly. Instead, use {@link org.junit.runner.Request#sortWith(Comparator)}.
 *
 * @since 4.0
 */
public class Sorter implements Comparator<Description> {
    /**
     * NULL is a <code>Sorter</code> that leaves elements in an undefined order
     */
    public static Sorter NULL = new Sorter(new Comparator<Description>() {
        public int compare(Description o1, Description o2) {
            return 0;
        }
    });
    /**
     * RANDOM is a <code>Sorter</code> that will randomly shuffle elements using
     * <code>Collections.shuffle()</code>.
     * Note that <code>Collections.shuffle()</code> performs an unbiased
     * shuffling of the elements, which cannot be done using a
     * <code>Comparator</code> and <code>Collections.sort()</code>. Therefore,
     * the comparison method in <code>Sorter.RANDOM</code> is not used. 
     */
    public static Sorter RANDOM = new Sorter(new Comparator<Description>() {
        public int compare(Description o1, Description o2) {
            return 0;
        }
    });
    private final Comparator<Description> fComparator;

    /**
     * Creates a <code>Sorter</code> that uses <code>comparator</code>
     * to sort tests
     *
     * @param comparator the {@link Comparator} to use when sorting tests
     */
    public Sorter(Comparator<Description> comparator) {
        fComparator = comparator;
    }

    /**
     * Sorts the test in <code>runner</code> using <code>comparator</code>
     */
    public void apply(Object object) {
        if (object instanceof Sortable) {
            Sortable sortable = (Sortable) object;
            sortable.sort(this);
        }
    }

    public int compare(Description o1, Description o2) {
        return fComparator.compare(o1, o2);
    }
    
    @Override
    public boolean equals(Object o) {
    	return o == null ? fComparator == null : o.equals(fComparator);
    }
    
    @Override
    public int hashCode() {
    	return fComparator == null ? 0 : fComparator.hashCode();
    }
}
