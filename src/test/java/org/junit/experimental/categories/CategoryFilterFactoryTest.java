package org.junit.experimental.categories;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class CategoryFilterFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TestName testName = new TestName();

    private CategoryFilterFactory categoryFilterFactory = new CategoryFilterFactoryStub();

    @Test
    public void shouldCreateFilter() throws Exception {
        FilterFactoryParams params = new FilterFactoryParams(
                CategoryFilterFactoryStub.class.getName());
        Filter filter = categoryFilterFactory.createFilter(params);

        assertThat(filter, instanceOf(DummyFilter.class));
    }

    @Test
    public void shouldThrowException() throws Exception {
        FilterFactoryParams params = new FilterFactoryParams(
                "NonExistentFilter");

        expectedException.expect(FilterFactory.FilterNotCreatedException.class);

        categoryFilterFactory.createFilter(params);
    }

    private static class CategoryFilterFactoryStub extends CategoryFilterFactory {
        @Override
        protected Filter createFilter(Class<?>[] categories) {
            return new DummyFilter();
        }
    }

    private static class DummyFilter extends Filter {
        @Override
        public boolean shouldRun(Description description) {
            return false;
        }

        @Override
        public String describe() {
            return null;
        }
    }
}
