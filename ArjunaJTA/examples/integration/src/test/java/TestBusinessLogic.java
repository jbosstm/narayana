import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.examples.ejb.Customer;
import org.jboss.narayana.examples.ejb.SimpleEJB;
import org.jboss.narayana.examples.ejb.SimpleEJBImpl;
import org.jboss.narayana.examples.mdb.SimpleMDB;
import org.jboss.narayana.examples.servlet.SimpleServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBusinessLogic {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class, "test.jar")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addClasses(SimpleMDB.class)
				// .addAsManifestResource("WEB-INF/web.xml", "web.xml")
				.addAsManifestResource("META-INF/persistence.xml",
						"persistence.xml");
	}

	@Test
	public void checkThatDoubleCallIncreasesListSize() {
		SimpleServlet simpleServlet = new SimpleServlet();
		simpleServlet.createCustomer("tom");
		simpleServlet.createCustomer("tom");
	}

}
