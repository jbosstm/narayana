/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian.api;

import org.junit.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * This rule works with annotation {@link ValidTestVersions}.
 * It verifies that the test contains a field named 'version'
 * and verifies if the list of the valid test versions contains the found field value.
 * If the version field and annotation values match then the test is executed,
 * otherwise the test execution is skipped.
 */
public class ValidTestVersionsRule implements MethodRule {
    private static final String VERSION_FIELD_NAME = "version";

    private String methodName;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        // storing name of the currently running method
        methodName = method.getName();

        ValidTestVersions validTestVersions = method.getAnnotation(ValidTestVersions.class);
        String foundVersionValue = findVersionField(target);
        if (validTestVersions != null && foundVersionValue != null) {
            if (Arrays.stream(validTestVersions.value()).noneMatch(val -> val.equals(foundVersionValue))) {
                // ignoring to run the test when annotation does specify the version
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        throw new AssumptionViolatedException("Test " + method.getName() + " annotated with annotation " +
                                ValidTestVersions.class.getSimpleName() + " and the current '" + VERSION_FIELD_NAME + "' field is not in set " +
                                "of the versions to run with");
                    }
                };
            }
        }
        return base;
    }

    public String getMethodName() {
        return methodName;
    }

    private String findVersionField(Object objectToSearch) {
        for (Field field: objectToSearch.getClass().getDeclaredFields()) {
            if (field.getName().equals(VERSION_FIELD_NAME) && field.getType().isAssignableFrom(String.class)) {
                try {
                    field.setAccessible(true);
                    return (String) field.get(objectToSearch);
                } catch (IllegalAccessException iae) {
                    new IllegalStateException("Cannot get value of '" + VERSION_FIELD_NAME + "' field with reflection", iae);
                }
            }
        }
        return null;
    }
}