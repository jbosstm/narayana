import java.io.PrintStream;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.examples.ejb.Customer;
import org.jboss.narayana.examples.ejb.SimpleEjb;
import org.jboss.narayana.examples.ejb.SimpleEjbLocal;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SimpleEJBTest {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class, "test.jar")
				.addClasses(SimpleEjbLocal.class, SimpleEjb.class)
				.addClasses(Customer.class)
				.addAsManifestResource("META-INF/persistence.xml",
						"persistence.xml");
	}

	// @EJB
	// private SimpleEjbLocal simpleEjbLocal;

	@Test
	public void testServlet() {
		PrintStream out = System.out;
		out.println("<html>");
		out.println("<head><title>Rocking out with Openshift</title></head>");
		out.println("<body>");
		out.println("<h1>Openshifting to 11</h1>");

		try {
			UserTransaction tx = (UserTransaction) new InitialContext()
					.lookup("java:comp/UserTransaction");
			tx.begin();
			InitialContext ic = new InitialContext();
			SimpleEjbLocal sel = (SimpleEjbLocal) ic
					.lookup("java:module/SimpleEjb");
			out.println("<p>" + sel.getStatus() + "</p>");
			out.println("<p>" + sel.getStatus2() + "</p>");
			out.println("<p>" + sel.createCustomerAndListIds() + "</p>");
			tx.commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		out.println("</body>");
	}

	@Test
	public void testServlet2() {
		PrintStream out = System.out;
		out.println("<html>");
		out.println("<head><title>Rocking out with Openshift</title></head>");
		out.println("<body>");
		out.println("<h1>Openshifting to 11</h1>");

		try {
			UserTransaction tx = (UserTransaction) new InitialContext()
					.lookup("java:comp/UserTransaction");
			tx.begin();
			InitialContext ic = new InitialContext();
			SimpleEjbLocal sel = (SimpleEjbLocal) ic
					.lookup("java:module/SimpleEjb");
			out.println("<p>" + sel.getStatus() + "</p>");
			out.println("<p>" + sel.getStatus2() + "</p>");
			out.println("<p>" + sel.createCustomerAndListIds() + "</p>");
			tx.commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		out.println("</body>");
	}
}
