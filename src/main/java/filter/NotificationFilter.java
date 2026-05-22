package filter;

import dal.TicketDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Permission;
import model.User;

import java.io.*;
import java.util.List;

public class NotificationFilter implements Filter {

	private static final boolean debug = true;
	private FilterConfig filterConfig = null;
	private final TicketDAO ticketDAO = new TicketDAO();

	public NotificationFilter() {
	}

	private void doBeforeProcessing(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if (debug)
			log("NotificationFilter:DoBeforeProcessing");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession(false);

		if (session != null) {
			User user = (User) session.getAttribute("authUser");
			// Chỉ hiện notification ticket khi có TICKET_VIEW permission (dynamic RBAC)
			if (user != null && hasPermission(session, "TICKET_VIEW")) {
				int count = ticketDAO.countPendingTickets();
				session.setAttribute("pendingTicketCount", count);
			}
		}
	}

	private boolean hasPermission(HttpSession session, String code) {
		if (session == null || code == null)
			return false;
		List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
		if (perms == null)
			return false;
		for (Permission p : perms) {
			if (code.equals(p.getCode()))
				return true;
		}
		return false;
	}

	private void doAfterProcessing(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if (debug)
			log("NotificationFilter:DoAfterProcessing");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (debug)
			log("NotificationFilter:doFilter()");

		doBeforeProcessing(request, response);
		Throwable problem = null;
		try {
			chain.doFilter(request, response);
		} catch (Throwable t) {
			problem = t;
			t.printStackTrace();
		}
		doAfterProcessing(request, response);
		if (problem != null) {
			if (problem instanceof ServletException)
				throw (ServletException) problem;
			if (problem instanceof IOException)
				throw (IOException) problem;
			sendProcessingError(problem, response);
		}
	}

	public void init(FilterConfig config) {
		this.filterConfig = config;
		if (filterConfig != null && debug)
			log("NotificationFilter:Initializing filter");
	}

	public void destroy() {
	}

	public FilterConfig getFilterConfig() {
		return this.filterConfig;
	}

	public void setFilterConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void log(String msg) {
		if (filterConfig != null) {
			filterConfig.getServletContext().log(msg);
		}
	}

	private void sendProcessingError(Throwable t, ServletResponse response) {
		String stackTrace = getStackTrace(t);

		if (stackTrace != null && !stackTrace.equals("")) {
			try {
				response.setContentType("text/html");
				PrintStream ps = new PrintStream(response.getOutputStream());
				PrintWriter pw = new PrintWriter(ps);
				pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n");
				pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
				pw.print(stackTrace);
				pw.print("</pre></body>\n</html>");
				pw.close();
				ps.close();
				response.getOutputStream().close();
			} catch (Exception ex) {
			}
		} else {
			try {
				PrintStream ps = new PrintStream(response.getOutputStream());
				t.printStackTrace(ps);
				ps.close();
				response.getOutputStream().close();
			} catch (Exception ex) {
			}
		}
	}

	private String getStackTrace(Throwable t) {
		String stackTrace = null;
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			sw.close();
			stackTrace = sw.getBuffer().toString();
		} catch (Exception ex) {
		}
		return stackTrace;
	}
}
