/*
 * SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.arquillian;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parameterized Arquillian JUnit runner adding the possibility to run parameterized JUnit test cases ,
 * while using the functionality of Arquillian of executing tests within the container.
 *
 * Adjusted from the HAL testsuite (https://github.com/hal/testsuite).
 */
public class ArquillianParametrized extends ParentRunner<Arquillian> {

    private List<Arquillian> arquillians = new ArrayList<>();
    private final List<FrameworkMethod> ignoredMethods;

    public ArquillianParametrized(Class<?> testClass) throws Throwable {
        super(testClass);
        Parameterized.Parameters parameters = getParametersMethod().getAnnotation(
                Parameterized.Parameters.class);
        ignoredMethods = new ArrayList<>(getTestClass().getAnnotatedMethods(Ignore.class));
        createRunnersForParameters(allParameters(), parameters.name());
    }

    @Override
    protected List<Arquillian> getChildren() {
        return arquillians;
    }

    @Override
    protected Description describeChild(Arquillian child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Arquillian child, RunNotifier notifier) {
        // This is a workaround for parameterized test cases which single test method is ignored
        if (ignoredMethods.size() == 1) {
            notifier.fireTestIgnored(getDescription());
            return;
        }
        child.run(notifier);
    }

    private class TestClassEnricher extends Arquillian {

        private final Object[] fParameters;

        private final String fName;

        TestClassEnricher(Class<?> testClass, Object[] fParameters, String fName) throws InitializationError {
            super(testClass);
            this.fParameters = fParameters;
            this.fName = fName;
        }

        @Override
        public Object createTest() throws Exception {
            if (fieldsAreAnnotated()) {
                return createTestUsingFieldInjection();
            } else {
                return createTestUsingConstructorInjection();
            }
        }

        private Object createTestUsingConstructorInjection() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(fParameters);
        }

        private Object createTestUsingFieldInjection() throws Exception {
            List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
            if (annotatedFieldsByParameter.size() != fParameters.length) {
                throw new Exception("Wrong number of parameters and @Parameter fields." +
                        " @Parameter fields counted: " + annotatedFieldsByParameter.size() + ", available parameters: " + fParameters.length + ".");
            }
            Object testClassInstance = getTestClass().getJavaClass().newInstance();
            for (FrameworkField each : annotatedFieldsByParameter) {
                Field field = each.getField();
                Parameterized.Parameter annotation = field.getAnnotation(Parameterized.Parameter.class);
                int index = annotation.value();
                try {
                    field.set(testClassInstance, fParameters[index]);
                } catch (IllegalArgumentException iare) {
                    throw new Exception(getTestClass().getName() + ": Trying to set " + field.getName() +
                            " with the value " + fParameters[index] +
                            " that is not the right type (" + fParameters[index].getClass().getSimpleName() + " instead of " +
                            field.getType().getSimpleName() + ").", iare);
                }
            }
            return testClassInstance;
        }

        @Override
        protected String getName() {
            return fName;
        }

        @Override
        protected String testName(FrameworkMethod method) {
            return method.getName() + getName();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
            if (fieldsAreAnnotated()) {
                validateZeroArgConstructor(errors);
            }
        }

        @Override
        protected void validateFields(List<Throwable> errors) {
            super.validateFields(errors);
            if (fieldsAreAnnotated()) {
                List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
                int[] usedIndices = new int[annotatedFieldsByParameter.size()];
                for (FrameworkField each : annotatedFieldsByParameter) {
                    int index = each.getField().getAnnotation(Parameterized.Parameter.class).value();
                    if (index < 0 || index > annotatedFieldsByParameter.size() - 1) {
                        errors.add(
                                new Exception("Invalid @Parameter value: " + index + ". @Parameter fields counted: " +
                                        annotatedFieldsByParameter.size() + ". Please use an index between 0 and " +
                                        (annotatedFieldsByParameter.size() - 1) + ".")
                        );
                    } else {
                        usedIndices[index]++;
                    }
                }
                for (int index = 0; index < usedIndices.length; index++) {
                    int numberOfUse = usedIndices[index];
                    if (numberOfUse == 0) {
                        errors.add(new Exception("@Parameter(" + index + ") is never used."));
                    } else if (numberOfUse > 1) {
                        errors.add(new Exception("@Parameter(" + index + ") is used more than once (" + numberOfUse + ")."));
                    }
                }
            }
        }
    }

    private Iterable<Object[]> allParameters() throws Throwable {
        Object parameters = getParametersMethod().invokeExplosively(null);
        if (parameters instanceof Iterable) {
            // verification if it's arrays of arrays or just single value array; if so need to wrap
            Iterator iter = ((Iterable) parameters).iterator();
            if (iter.hasNext()) {
                if (!(iter.next() instanceof Object[])) { // it's single value
                    List<Object[]> multiValue = new ArrayList<>();
                    for (Object parameter: (Iterable<? extends Object>) parameters) {
                        multiValue.add(new Object[]{parameter});
                    }
                    return multiValue;
                }
            }
            return (Iterable<Object[]>) parameters;
        } else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
                Parameterized.Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
                + getTestClass().getName());
    }

    private void createRunnersForParameters(Iterable<Object[]> allParameters,
                                            String namePattern) throws InitializationError, Exception {
        try {
            int i = 0;
            for (Object[] parametersOfSingleTest : allParameters) {
                String name = nameFor(namePattern, i, parametersOfSingleTest);
                TestClassEnricher enrichedArquillianRunner = new TestClassEnricher(
                        getTestClass().getJavaClass(), parametersOfSingleTest,
                        name);
                arquillians.add(enrichedArquillianRunner);
                ++i;
            }
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}",
                Integer.toString(index));
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format(
                "{0}.{1}() must return an Iterable of arrays.",
                className, methodName);
        return new Exception(message);
    }

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameterized.Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }
}