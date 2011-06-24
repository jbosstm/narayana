package org.jboss.narayana.examples.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.jboss.narayana.examples.ejb.SimpleEJB;

public class SimpleServlet extends HttpServlet {

	@EJB
	private SimpleEJB simpleEJB;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Rocking out with Openshift</title></head>");
		out.println("<body>");
		String operation = request.getParameter("operation");
		if (operation != null) {
			if (operation.equals("list")) {
				out.println(listCustomers());
			} else if (operation.equals("create")) {
				String name = request.getParameter("name");
				if (name != null) {
					out.println(createCustomer(name));
				} else {
					out.println(error("Requires name parameter"));
				}
			} else {
				out.println(error("Unknown operation: " + operation));
			}
		} else {
			out.println(error("Requires operation"));
		}
		out.println("</body>");
		out.println("</html>");
	}

	private String error(String error) {
		StringBuffer toWrite = new StringBuffer();
		toWrite.append("<h1>Invalid parameters</h1>\n");
		toWrite.append("<p>" + error + "</p>");
		return toWrite.toString();
	}

	public String listCustomers() {
		StringBuffer toWrite = new StringBuffer();
		toWrite.append("<h1>List of customer Ids</h1>\n");

		try {
			UserTransaction tx = (UserTransaction) new InitialContext()
					.lookup("java:comp/UserTransaction");
			tx.begin();
			toWrite.append("<p>" + simpleEJB.listIds() + "</p>");
			tx.commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return toWrite.toString();
	}

	public String createCustomer(String name) {
		StringBuffer toWrite = new StringBuffer();
		toWrite.append("<h1>Update summary</h1>\n");

		try {
			UserTransaction tx = (UserTransaction) new InitialContext()
					.lookup("java:comp/UserTransaction");
			tx.begin();
			simpleEJB.createCustomer(name);
			toWrite.append("<p>Created: " + name + "</p>");
			tx.commit();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return toWrite.toString();
	}
}
