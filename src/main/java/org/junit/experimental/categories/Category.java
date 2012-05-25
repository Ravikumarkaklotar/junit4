package org.junit.experimental.categories;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a test class or test method as belonging to one or more categories of tests.
 * The value is an array of arbitrary classes.
 * 
 * This annotation is only interpreted by the Categories runner (at present).
 * 
 * For example:
<pre>
	public interface FastTests {}
	public interface SlowTests {}

	public static class A {
		&#064;Test
		public void a() {
			fail();
		}

		&#064;Category(SlowTests.class)
		&#064;Test
		public void b() {
		}
	}

	&#064;Category({SlowTests.class, FastTests.class})
	public static class B {
		&#064;Test
		public void c() {

		}
	}
</pre>
 * 
 * For more usage, see code example on {@link Categories}.
 * @since 4.8
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	Class<?>[] value();
}