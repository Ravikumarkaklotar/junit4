package org.junit.internal.runners.rules;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.fixtures.ClassFixture;
import org.junit.fixtures.Fixture;
import org.junit.fixtures.TestFixture;
import org.junit.runners.model.FrameworkMember;

/**
 * Validates the fixture fields/methods of a {@link org.junit.runners.model.TestClass}.
 * All reasons for rejecting the {@code TestClass} are written to a list of errors.
 *
 * <p>There are four slightly different validators. The {@link #CLASS_FIXTURE_FIELD_VALIDATOR}
 * validates fields with a {@link ClassFixture} annotation and the
 * {@link #FIXTURE_FIELD_VALIDATOR} validates fields with a {@link Fixture} annotation.</p>
 *
 * <p>The {@link #CLASS_FIXTURE_METHOD_VALIDATOR}
 * validates methods with a {@link ClassFixture} annotation and the
 * {@link #FIXTURE_METHOD_VALIDATOR} validates methods with a {@link Fixture} annotation.</p>
 
 */
public class FixtureMemberValidator extends AnnotatedMemberValidator {
    /**
     * Validates fields with a {@link ClassFixture} annotation.
     */
    public static final FixtureMemberValidator CLASS_FIXTURE_FIELD_VALIDATOR = classMemberValidatorBuilder()
            .withValidator(new FieldMustBeAFixture())
            .build();

    /**
     * Validates fields with a {@link Fixture} annotation.
     */
    public static final FixtureMemberValidator FIXTURE_FIELD_VALIDATOR = testMemberValidatorBuilder()
            .withValidator(new MemberMustBeNonStaticOrAlsoClassFixture())
            .withValidator(new FieldMustBeAFixture())
            .build();
  
    /**
     * Validates methods with a {@link ClassFixture} annotation.
     */
    public static final FixtureMemberValidator CLASS_FIXTURE_METHOD_VALIDATOR = classMemberValidatorBuilder()
            .forMethods()
            .withValidator(new MethodMustBeAFixture())
            .build();

    /**
     * Validates methods with a {@link Fixture} annotation.
     */
    public static final FixtureMemberValidator FIXTURE_METHOD_VALIDATOR = classMemberValidatorBuilder()
            .forMethods()
            .withValidator(new MemberMustBeNonStaticOrAlsoClassFixture())
            .withValidator(new MethodMustBeAFixture())
            .build();


    FixtureMemberValidator(Builder builder) {
        super(builder);
    }

    private static class Builder extends AnnotatedMemberValidator.Builder<Builder> {

        FixtureMemberValidator build() {
            return new FixtureMemberValidator(this);
        }
    }
    
    private static Builder classMemberValidatorBuilder() {
        return new Builder().forClassAnnotation(ClassFixture.class);
    }

    private static Builder testMemberValidatorBuilder() {
        return new Builder().forTestAnnotation(Fixture.class);
    }

    private static boolean isTestFixture(FrameworkMember<?> member) {
        return TestFixture.class.isAssignableFrom(member.getType());
    }

    /**
     * Require the member to return an implementation of {@link org.junit.rules.TestFixture}
     */
    private static final class MethodMustBeAFixture implements MemberValidator {
        public void validate(FrameworkMember<?> member,
                Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!isTestFixture(member)) {
                errors.add(new ValidationError(member, annotation, 
                        "must return an implementation of TestFixture."));
            }
        }
    }
    
    /**
     * Requires the member is a field implementing {@link org.junit.rules.TestFixture}
     */
    private static final class FieldMustBeAFixture implements MemberValidator {

        public void validate(FrameworkMember<?> member,
                Class<? extends Annotation> annotation, List<Throwable> errors) {
            if (!isTestFixture(member)) {
                errors.add(new ValidationError(member, annotation,
                        "must implement TestFixture."));
            }
        }
    }

    /**
     * Requires the validated member to be non-static, unless they are annotated with {@code @ClassFixture}.
     */
    private static final class MemberMustBeNonStaticOrAlsoClassFixture implements MemberValidator {
        public void validate(FrameworkMember<?> member, Class<? extends Annotation> annotation, List<Throwable> errors) {
            boolean isClassFixtureAnnotated = (member.getAnnotation(ClassFixture.class) != null);

            if (member.isStatic() && !isClassFixtureAnnotated) {
                String message = "must not be static or it must be annotated with @ClassFixture.";
                errors.add(new ValidationError(member, annotation, message));
            }
        }
    }
}
