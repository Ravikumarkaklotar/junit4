package org.junit.rules;

import java.util.concurrent.TimeUnit;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * The Timeout Rule applies the same timeout to all test methods in a class:
 * <pre>
 * public static class HasGlobalLongTimeout {
 *
 *  &#064;Rule
 *  public Timeout globalTimeout= new Timeout(20);
 *
 *  &#064;Test
 *  public void run1() throws InterruptedException {
 *      Thread.sleep(100);
 *  }
 *
 *  &#064;Test
 *  public void infiniteLoop() {
 *      while (true) {}
 *  }
 * }
 * </pre>
 * <p>
 * Each test is run in a new thread. If the specified timeout elapses before
 * the test completes, its execution is interrupted via {@link Thread#interrupt()}.
 * This happens in interruptable I/O and locks, and methods in {@link Object}
 * and {@link Thread} throwing {@link InterruptedException}.
 *
 * @since 4.7
 */
public class Timeout implements TestRule {

    /**
     * Optional system property with global timeout in milliseconds.
     */
    public static final String GLOBAL_TIMEOUT_PROPERTY_NAME = "org.junit.rules.timeout.global";

    private final long fTimeout;
    private final TimeUnit fTimeUnit;
    private boolean fLookForStuckThread;

    /**
     * Create a {@code Timeout} instance with the timeout specified
     * in milliseconds.
     * <p>
     * This constructor is deprecated.
     * <p>
     * Instead use {@link #Timeout(long, java.util.concurrent.TimeUnit)},
     * {@link Timeout#millis(long)}, or {@link Timeout#seconds(long)}.
     *
     * @param millis the maximum time in milliseconds to allow the
     * test to run before it should timeout
     */
    @Deprecated
    public Timeout(int millis) {
        this(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Create a {@code Timeout} instance with the timeout specified
     * at the unit of granularity of the provided {@code TimeUnit}.
     *
     * @param timeout the maximum time to allow the test to run
     * before it should timeout
     * @param unit the time unit for the {@code timeout}
     * @since 4.12
     */
    public Timeout(long timeout, TimeUnit unit) {
        fTimeout = timeout;
        fTimeUnit = unit;
        fLookForStuckThread = false;
    }

    /**
     * @param millis the timeout in milliseconds
     * @since 4.12
     */
    public static Timeout millis(long millis) {
        return new Timeout(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * @param seconds the timeout in seconds
     * @since 4.12
     */
    public static Timeout seconds(long seconds) {
        return new Timeout(seconds, TimeUnit.SECONDS);
    }

    /**
     * Specifies whether to look for a stuck thread.  If a timeout occurs and this
     * feature is enabled, the test will look for a thread that appears to be stuck
     * and dump its backtrace.  This feature is experimental.  Behavior may change
     * after the 4.12 release in response to feedback.
     * @param enable {@code true} to enable the feature
     * @return This object
     * @since 4.12
     */
    public Timeout lookForStuckThread(boolean enable) {
        fLookForStuckThread = enable;
        return this;
    }

    public Statement apply(Statement base, Description description) {
        return new FailOnTimeout(base, fTimeout, fTimeUnit, fLookForStuckThread);
    }

    /**
     * Returns global timeout set via system property, or 0 if none is set.
     */
    public static long getGlobalTimeoutInMillis() {
        return Long.getLong(GLOBAL_TIMEOUT_PROPERTY_NAME, 0);
    }

    public static Timeout newTimeoutFromGlobalTimeout() {
        return getGlobalTimeoutInMillis() > 0 ? new Timeout(getGlobalTimeoutInMillis(), TimeUnit.MILLISECONDS) : null;
    }

    public Timeout newTimeoutFromGlobalTimeoutIfLessThanThisTimeout() {
        Timeout timeout = this;
        if (getGlobalTimeoutInMillis() > 0) {
            if (getGlobalTimeoutInMillis() < fTimeUnit.toMillis(fTimeout)) {
                timeout = new Timeout(getGlobalTimeoutInMillis(), TimeUnit.MILLISECONDS)
                        .lookForStuckThread(fLookForStuckThread);
            }
        }
        return timeout;
    }
}