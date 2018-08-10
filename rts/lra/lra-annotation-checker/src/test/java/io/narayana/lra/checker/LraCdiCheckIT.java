/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.checker;

import org.eclipse.microprofile.lra.annotation.Forget;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.narayana.lra.checker.bean.AllAnnotationsNoPathBean;
import io.narayana.lra.checker.bean.AsyncSuspendWithoutForgetBean;
import io.narayana.lra.checker.bean.CorrectBean;
import io.narayana.lra.checker.bean.CorrectMethodLRABean;
import io.narayana.lra.checker.bean.ForgetWithoutDeleteBean;
import io.narayana.lra.checker.bean.LeaveWithoutPutBean;
import io.narayana.lra.checker.bean.LraJoinFalseBean;
import io.narayana.lra.checker.bean.LraJoinFalseMethodLRABean;
import io.narayana.lra.checker.bean.MultiForgetBean;
import io.narayana.lra.checker.bean.NoPostOrGetBean;
import io.narayana.lra.checker.cdi.LraAnnotationProcessingExtension;

/**
 * Test case which checks functionality of CDI extension by deploying wrongly
 * composed LRA components and expect an deployment exception to be thrown.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class LraCdiCheckIT {
    private static final String BEAN_CHECK_MSG1 = "No failue on checking bean ";
    private static final String BEAN_CHECK_MSG2 = " expected but the failure list is ";

    @Before
    public void cleanUp() throws Exception {
        FailureCatalog.INSTANCE.clear();
    }

    @Test
    public void complementaryPathAnnotation() throws Exception {
        initWeld(AllAnnotationsNoPathBean.class);
        assertFailureCatalogContains(AllAnnotationsNoPathBean.class, "should use complementary annotation.*Path");
    }

    @Test
    public void methodTypeAnnotationMissing() throws Exception {
        initWeld(NoPostOrGetBean.class);
        assertFailureCatalogContains(NoPostOrGetBean.class, "should use complementary annotation.*(PUT|GET)");
    }

    @Test
    public void asyncInvocationWithoutForgetDefined() throws Exception {
        initWeld(AsyncSuspendWithoutForgetBean.class);
        assertFailureCatalogContains(AsyncSuspendWithoutForgetBean.class, "The LRA class has to contain @Status and @Forget annotations");
    }

    @Test
    public void forgetMissingDelete() throws Exception {
        initWeld(ForgetWithoutDeleteBean.class);
        assertFailureCatalogContains(ForgetWithoutDeleteBean.class, "should use complementary annotation.*(DELETE)");
    }

    @Test
    public void leaveMissingPut() throws Exception {
        initWeld(LeaveWithoutPutBean.class);
        assertFailureCatalogContains(LeaveWithoutPutBean.class, "should use complementary annotation.*(PUT)");
    }

    @Test
    public void multiForgetAnnotations() throws Exception {
        initWeld(MultiForgetBean.class);
        assertFailureCatalogContains(MultiForgetBean.class, "multiple annotations.*" + Forget.class.getName());
    }

    @Test
    public void lraJoinFalseCorrect() throws Exception {
        initWeld(LraJoinFalseBean.class);
        Assert.assertTrue(BEAN_CHECK_MSG1 + LraJoinFalseBean.class.getName() + BEAN_CHECK_MSG2
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void lraJoinFalseCorrectLRAOnMethod() throws Exception {
        initWeld(LraJoinFalseMethodLRABean.class);
        Assert.assertTrue(BEAN_CHECK_MSG1 + LraJoinFalseMethodLRABean.class.getName() + BEAN_CHECK_MSG2
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void allCorrect() throws Exception {
        initWeld(CorrectBean.class);
        Assert.assertTrue(BEAN_CHECK_MSG1 + CorrectBean.class.getName() + BEAN_CHECK_MSG2
            + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void allCorrectLRAOnMethod() throws Exception {
        initWeld(CorrectMethodLRABean.class);
        Assert.assertTrue(BEAN_CHECK_MSG1 + CorrectMethodLRABean.class.getName() + BEAN_CHECK_MSG2
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void noLraContext() throws Exception {
        initWeld(CorrectMethodLRABean.class);
        Assert.assertTrue(BEAN_CHECK_MSG1 + CorrectMethodLRABean.class.getName() + BEAN_CHECK_MSG2
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    private void initWeld(Class<?> beanClassToCheck) {
        @SuppressWarnings("unchecked")
        Weld weld = new Weld().disableDiscovery().addExtensions(LraAnnotationProcessingExtension.class)
                .addBeanClasses(beanClassToCheck);

        try (WeldContainer container = weld.initialize()) {
            // weld intializes and works with extensions here
        }
    }

    private void assertFailureCatalogContains(Class<?> beanClass, String failureStringToCheck) {
        Assert.assertFalse("Failure on checking bean " + beanClass.getName() + " should happen",
                FailureCatalog.INSTANCE.isEmpty());
        Assert.assertThat(FailureCatalog.INSTANCE.formatCatalogContent().split(System.lineSeparator()),
                HamcrestRegexpMatcher.matches(failureStringToCheck));
    }
}
