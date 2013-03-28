package org.junit.experimental.parallel;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Collection;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.experimental.parallel.ParallelComputerBuilder.Type;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.experimental.parallel.ParallelComputerBuilderTest.RangeMatcher.between;

/**
 * @author Tibor Digana (tibor17)
 * @since 4.12
 */
public class ParallelComputerBuilderTest {
    @Rule
    public final Stopwatch runtime = new Stopwatch();

    @Before
    public void beforeTest() {
        Class1.maxConcurrentMethods = 0;
        Class1.concurrentMethods = 0;
        shutdownTask = null;
        afterShutdown = false;
    }

    @Test
    public void parallelMethodsReuseOneOrTwoThreads() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.useOnePool(4);

        // One thread because one suite: TestSuite, however the capacity is 5.
        parallelComputerBuilder.parallel(5, Type.SUITES);

        // Two threads because TestSuite has two classes, however the capacity is 5.
        parallelComputerBuilder.parallel(5, Type.CLASSES);

        // One or two threads because one threads comes from '#useOnePool(4)'
        // and next thread may be reused from finished class, however the capacity is 3.
        parallelComputerBuilder.parallel(3, Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(0));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertFalse(computer.splitPool);
        assertThat(computer.poolCapacity, is(4));
        assertTrue(result.wasSuccessful());
        if (Class1.maxConcurrentMethods == 1) {
            assertThat(timeSpent, between(1950, 2250));
        } else if (Class1.maxConcurrentMethods == 2) {
            assertThat(timeSpent, between(1450, 1750));
        } else {
            fail();
        }
    }

    @Test
    public void suiteAndClassInOnePool() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.useOnePool(5);
        parallelComputerBuilder.parallel(5, Type.SUITES);
        parallelComputerBuilder.parallel(5, Type.CLASSES);
        parallelComputerBuilder.parallel(3, Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class, Class1.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(1));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertFalse(computer.splitPool);
        assertThat(computer.poolCapacity, is(5));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(2));
        assertThat(timeSpent, anyOf(between(1450, 1750), between(1950, 2250)));
    }

    @Test
    public void onePoolWithUnlimitedParallelMethods() {
        // see ParallelComputerBuilder Javadoc
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.useOnePool(8);
        parallelComputerBuilder.parallel(2, Type.SUITES);
        parallelComputerBuilder.parallel(4, Type.CLASSES);
        parallelComputerBuilder.parallel(Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class, Class1.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(1));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertFalse(computer.splitPool);
        assertThat(computer.poolCapacity, is(8));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(4));
        assertThat(timeSpent, between(950, 1250));
    }

    @Test
    public void underflowParallelism() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.useOnePool(3);

        // One thread because one suite: TestSuite.
        parallelComputerBuilder.parallel(5, Type.SUITES);

        // One thread because of the limitation which is bottleneck.
        parallelComputerBuilder.parallel(1, Type.CLASSES);

        // One thread remains from '#useOnePool(3)'.
        parallelComputerBuilder.parallel(3, Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(0));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertFalse(computer.splitPool);
        assertThat(computer.poolCapacity, is(3));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(1));
        assertThat(timeSpent, between(1950, 2250));
    }

    @Test
    public void separatePoolsWithSuite() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.parallel(5, Type.SUITES);
        parallelComputerBuilder.parallel(5, Type.CLASSES);
        parallelComputerBuilder.parallel(3, Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(0));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertTrue(computer.splitPool);
        assertThat(computer.poolCapacity, is(ParallelComputerBuilder.TOTAL_POOL_SIZE_UNDEFINED));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(3));
        assertThat(timeSpent, between(950, 1250));
    }

    @Test
    public void separatePoolsWithSuiteAndClass() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.parallel(5, Type.SUITES);
        parallelComputerBuilder.parallel(5, Type.CLASSES);
        parallelComputerBuilder.parallel(3, Type.METHODS);

        // 6 methods altogether.
        // 2 groups with 3 threads.
        // Each group takes 0.5s.
        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class, Class1.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(1));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertTrue(computer.splitPool);
        assertThat(computer.poolCapacity, is(ParallelComputerBuilder.TOTAL_POOL_SIZE_UNDEFINED));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(3));
        assertThat(timeSpent, between(950, 1250));
    }

    @Test
    public void separatePoolsWithSuiteAndSequentialClasses() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder();
        parallelComputerBuilder.parallel(5, Type.SUITES);
        parallelComputerBuilder.parallel(1, Type.CLASSES);
        parallelComputerBuilder.parallel(3, Type.METHODS);

        ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        Result result = new JUnitCore().run(computer, TestSuite.class, Class1.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertThat(computer.suites.size(), is(1));
        assertThat(computer.classes.size(), is(1));
        assertThat(computer.nestedClasses.size(), is(2));
        assertThat(computer.nestedSuites.size(), is(0));
        assertTrue(computer.splitPool);
        assertThat(computer.poolCapacity, is(ParallelComputerBuilder.TOTAL_POOL_SIZE_UNDEFINED));
        assertTrue(result.wasSuccessful());
        assertThat(Class1.maxConcurrentMethods, is(2));
        assertThat(timeSpent, between(1450, 1750));
    }

    @Test(timeout = 2000)
    public void shutdownWithInterrupt() {
        ParallelComputerBuilder parallelComputerBuilder = new ParallelComputerBuilder().useOnePool(8);
        parallelComputerBuilder.parallel(2, Type.SUITES);
        parallelComputerBuilder.parallel(3, Type.CLASSES);
        parallelComputerBuilder.parallel(3, Type.METHODS);

        final ParallelComputerBuilder.ParallelComputer computer = parallelComputerBuilder.buildComputer();
        shutdownTask = new Runnable() {
            public void run() {
                Collection<org.junit.runner.Description> startedTests = computer.shutdown(true);
                assertThat(startedTests.size(), is(not(0)));
            }
        };
        Result result = new JUnitCore().run(computer, TestSuite.class, Class2.class, Class3.class);
        long timeSpent = runtime.runtime(MILLISECONDS);

        assertTrue(result.wasSuccessful());
        assertTrue(beforeShutdown);
        assertTrue(afterShutdown);
        assertThat(timeSpent, between(450, 1750));
    }

    private static volatile boolean beforeShutdown;
    private static volatile boolean afterShutdown;
    private static volatile Runnable shutdownTask;

    public static class Class1 {
        static volatile int concurrentMethods = 0;
        static volatile int maxConcurrentMethods = 0;

        @Test
        public void test1() throws InterruptedException {
            synchronized (Class1.class) {
                ++concurrentMethods;
                Class1.class.wait(500);
                maxConcurrentMethods = Math.max(maxConcurrentMethods, concurrentMethods--);
            }
        }

        @Test
        public void test2() throws InterruptedException {
            test1();
            Runnable shutdownTask = ParallelComputerBuilderTest.shutdownTask;
            if (shutdownTask != null) {
                beforeShutdown = true;
                shutdownTask.run();
                afterShutdown = true;
            }
        }
    }

    public static class Class2 extends Class1 {
    }

    public static class Class3 extends Class1 {
    }

    @RunWith(Suite.class)
    @Suite.SuiteClasses({Class2.class, Class1.class})
    public class TestSuite {
    }

    static class RangeMatcher extends TypeSafeMatcher<Long> {
        private final long from;
        private final long to;

        private RangeMatcher(long from, long to) {
            super(Long.class);
            this.from = from;
            this.to = to;
        }

        @Override
        protected boolean matchesSafely(Long actual) {
            return actual >= from && actual <= to;
        }

        public void describeTo(Description description) {
            description.appendValueList("between ", " and ", "", from, to);
        }

        public static Matcher<Long> between(long from, long to) {
            return new RangeMatcher(from, to);
        }
    }
}
