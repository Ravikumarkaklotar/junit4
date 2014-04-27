package org.junit.runner;

import java.lang.annotation.Annotation;

/**
 * The {@code TestDescription} describes a test which is to be run or has been run.
 * <p>
 * Until version 4.11 {@code Description} instances were mutable objects. With 4.12 the DescriptionBuilder was
 * introduced that guarantees that all generated descriptions are immutable objects.
 *
 * @see org.junit.runner.Description
 * @see org.junit.runner.ImmutableDescription
 * @see org.junit.runner.DescriptionBuilder
 * @since 4.12
 */
class TestDescription extends ImmutableDescription {
    TestDescription(Class<?> testClass, String displayName, String uniqueId, Annotation[] annotations) {
        super(testClass, displayName, uniqueId, annotations);
    }

    @Override
    public boolean isSuite() {
        return false;
    }

    @Override
    public boolean isTest() {
        return true;
    }

    @Override
    public int testCount() {
        return 1;
    }
}