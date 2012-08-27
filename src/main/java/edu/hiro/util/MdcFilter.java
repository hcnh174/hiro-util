package edu.hiro.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import ch.qos.logback.classic.ClassicConstants;
import edu.hiro.util.LoginHelper;

/**
 * based on MDCInsertingServletFilter 
 * A servlet filter that inserts various values retrieved from the incoming http
 * request into the MDC.
 * <p/>
 * <p/>
 * The values are removed after the request is processed.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MdcFilter implements Filter
{
	private final static String USERNAME_KEY="username";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		insertIntoMDC(request);
		try
		{
			chain.doFilter(request, response);
		}
		finally
		{
			clearMDC();
		}
	}

	void insertIntoMDC(ServletRequest request)
	{
		MDC.put(USERNAME_KEY, LoginHelper.getUsername());
		MDC.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());
		if (request instanceof HttpServletRequest)
		{
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			MDC.put(ClassicConstants.REQUEST_REQUEST_URI, httpServletRequest.getRequestURI());
			MDC.put(ClassicConstants.REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
			MDC.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY, httpServletRequest.getHeader("User-Agent"));
			MDC.put(ClassicConstants.REQUEST_X_FORWARDED_FOR, httpServletRequest.getHeader("X-Forwarded-For"));
			
			StringBuffer requestURL = httpServletRequest.getRequestURL();
			if (requestURL != null)
				MDC.put(ClassicConstants.REQUEST_REQUEST_URL, requestURL.toString());
		}
	}

	void clearMDC()
	{
		MDC.remove(USERNAME_KEY);
		MDC.remove(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY);
		MDC.remove(ClassicConstants.REQUEST_REQUEST_URI);
		MDC.remove(ClassicConstants.REQUEST_QUERY_STRING);
		MDC.remove(ClassicConstants.REQUEST_REQUEST_URL);
		MDC.remove(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY);
		MDC.remove(ClassicConstants.REQUEST_X_FORWARDED_FOR);
	}

	public void init(FilterConfig arg0) throws ServletException
	{
		// do nothing
	}
	
	public void destroy()
	{
		// do nothing
	}
}
