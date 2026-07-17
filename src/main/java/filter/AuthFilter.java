package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Permission;
import java.util.List;

public class AuthFilter implements Filter {

	private static final boolean debug = true;
	private FilterConfig filterConfig = null;

	public AuthFilter() {
	}

	public static String getStackTrace(Throwable t) {
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

	private void doBeforeProcessing(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if (debug) {
			log("AuthFilter:DoBeforeProcessing");
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String contextPath = httpRequest.getContextPath();
		String servletPath = httpRequest.getServletPath();

		if (servletPath == null) {
			servletPath = "";
		}

		String lowerPath = servletPath.toLowerCase();

		if (isPublic(lowerPath)) {
			request.setAttribute("_authPass", Boolean.TRUE);
			return;
		}

		HttpSession session = httpRequest.getSession(false);
		if (session == null || session.getAttribute("authUser") == null) {
			request.setAttribute("_authPass", Boolean.FALSE);
			request.setAttribute("_authRedirect", contextPath + "/login");
			return;
		}

		if (lowerPath.startsWith("/role-")) {
			List<Permission> perms = (List<Permission>) session.getAttribute("permissions");
			boolean hasRoleView = false;
			if (perms != null) {
				for (Permission p : perms) {
					if ("ROLE_VIEW".equals(p.getCode())) {
						hasRoleView = true;
						break;
					}
				}
			}
			if (!hasRoleView) {
				request.setAttribute("_authPass", Boolean.FALSE);
				request.setAttribute("_authRedirect", contextPath + "/views/error/403.jsp");
				return;
			}
		}

		Set allowedUrlsRaw = (Set) session.getAttribute("ALLOWED_URLS");
		if (allowedUrlsRaw == null) {
			allowedUrlsRaw = new HashSet();
		}

		if (allowedUrlsRaw.contains(servletPath)) {
			request.setAttribute("_authPass", Boolean.TRUE);
		} else {
			request.setAttribute("_authPass", Boolean.FALSE);
			request.setAttribute("_authRedirect", contextPath + "/views/error/403.jsp");
		}
	}

	private void doAfterProcessing(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if (debug) {
			log("AuthFilter:DoAfterProcessing");
		}

		Boolean authPass = (Boolean) request.getAttribute("_authPass");
		String redirectPath = (String) request.getAttribute("_authRedirect");

		if (Boolean.FALSE.equals(authPass) && redirectPath != null) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect(redirectPath);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (debug) {
			log("AuthFilter:doFilter()");
		}

		doBeforeProcessing(request, response);
		Throwable problem = null;
		try {
			Boolean authPass = (Boolean) request.getAttribute("_authPass");
			if (Boolean.TRUE.equals(authPass)) {
				chain.doFilter(request, response);
			}
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

	public FilterConfig getFilterConfig() {
		return (this.filterConfig);
	}

	public void setFilterConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void init(FilterConfig config) {
		this.filterConfig = config;
		if (filterConfig != null) {
			if (debug) {
				log("AuthFilter:Initializing filter");
			}
		}
	}

	public void destroy() {
	}

	@Override
	public String toString() {
		if (filterConfig == null) {
			return "AuthFilter()";
		}
		StringBuilder sb = new StringBuilder("AuthFilter(");
		sb.append(filterConfig);
		sb.append(")");
		return sb.toString();
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

	public void log(String msg) {
		if (filterConfig != null) {
			filterConfig.getServletContext().log(msg);
		}
	}

	private boolean isPublic(String path) {
		if (path.equals("/") || path.equals(""))
			return true;
		if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg")
				|| path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico")
				|| path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf"))
			return true;
		if (path.startsWith("/assets/"))
			return true;
		if (path.startsWith("/views/error/"))
			return true;
		if (path.startsWith("/auth/"))
			return true;
		if (path.startsWith("/login"))
			return true;
		if (path.startsWith("/logout"))
			return true;
		if (path.startsWith("/forgot-password"))
			return true;
		if (path.startsWith("/reset-password"))
			return true;
		if (path.startsWith("/change-password"))
			return true;
		if (path.startsWith("/home"))
			return true;
		if (path.startsWith("/profile"))
			return true;
		return false;
	}
}
