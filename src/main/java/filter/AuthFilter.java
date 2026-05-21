package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
		String path = httpRequest.getRequestURI().substring(contextPath.length()).toLowerCase();

		if (isPublic(path)) {
			return;
		}

		HttpSession session = httpRequest.getSession(false);
		if (session == null || session.getAttribute("authUser") == null) {
			httpResponse.sendRedirect(contextPath + "/login");
		}
	}

	private void doAfterProcessing(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if (debug) {
			log("AuthFilter:DoAfterProcessing");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (debug) {
			log("AuthFilter:doFilter()");
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Xu ly auth, lay response ngay de kiem tra co redirect khong
		boolean isPublic = false;
		String contextPath = httpRequest.getContextPath();
		String path = httpRequest.getRequestURI().substring(contextPath.length()).toLowerCase();

		if (isPublic(path)) {
			isPublic = true;
		} else {
			HttpSession session = httpRequest.getSession(false);
			if (session != null && session.getAttribute("authUser") != null) {
				isPublic = true;
			}
		}

		if (!isPublic) {
			// Chua dang nhap -> redirect den /login
			httpResponse.sendRedirect(contextPath + "/login");
			return;
		}

		// Da dang nhap hoac la trang public -> cho di qua
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
			if (problem instanceof ServletException) {
				throw (ServletException) problem;
			}
			if (problem instanceof IOException) {
				throw (IOException) problem;
			}
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
				pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); // NOI18N

				pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
				pw.print(stackTrace);
				pw.print("</pre></body>\n</html>"); // NOI18N
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
		if (path.startsWith("/login"))
			return true;
		if (path.startsWith("/forgot-password"))
			return true;
		if (path.startsWith("/reset-password"))
			return true;
		return false;
	}
}
