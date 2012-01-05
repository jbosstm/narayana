/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.qa.junit;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A modified version of org.junit.rules.TestName to support naming of qa tests.
 * Original code is from junit 4.8.1. under IBM CPL 0.5 licence.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
public class QATestNameRule implements MethodRule
{
    private String groupName;
    private String methodName;
    private Integer parameterSetNumber;

    /**
     * @return the name of the currently running test group.
     * This is normally the (unqualified) name of the class being run, which
     * may be a subclass of the one actually containing hte test method.
     */
    public String getGroupName() {
        return groupName;
    }

	/**
	 * @return the name of the currently-running test method
	 */
	public String getMethodName() {
		return methodName;
	}

    public Integer getParameterSetNumber()
    {
        return parameterSetNumber;
    }

    public void setParameterSetNumber(Integer parameterSetNumber)
    {
        this.parameterSetNumber = parameterSetNumber;
    }

    /**
     * Modifies the method-running {@link org.junit.runners.model.Statement} to implement an additional
     * test-running rule.
     *
     * @param base   The {@link org.junit.runners.model.Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @return a new statement, which may be the same as {@code base},
     *         a wrapper around {@code base}, or a completely new Statement.
     */
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
    {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                methodName = method.getName();
                
                groupName = target.getClass().getSimpleName();
                if(groupName.startsWith("TestGroup_")) {
                    groupName = groupName.substring(10);
                }

                if(includeTest()) {
                    base.evaluate(); // calls setUp, test, testDown - see BlockJUnit4ClassRunner.methodBlock
                }
            }
        };
    }

    protected boolean includeTest() {
        String includePattern = System.getProperty("names");
        if(includePattern != null && methodName != null) {
            if(!methodName.matches(includePattern)) {
                System.out.println("QATestNameRule.checkIncludes: skipping test "+methodName+" as it does not match 'names' pattern "+includePattern);
                return false;
            }
        }
        return true;
    }
}
