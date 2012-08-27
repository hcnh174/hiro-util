package edu.hiro.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service("loginService")
public class LoginHelper
{
	public static final String REMEMBER_ME=TokenBasedRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
	public static final String LAST_EXCEPTION=WebAttributes.AUTHENTICATION_EXCEPTION;
	public static final String ACCESS_DENIED=WebAttributes.ACCESS_DENIED_403;
	public static final String LAST_USERNAME=UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY;

	private static final String EMPTY_PASSWORD="";
	
	public static void setUser(UserDetails user)
	{
		Collection<GrantedAuthority> authorities=copyAuthorities(user);
		UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(user,EMPTY_PASSWORD,authorities);
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	private static Collection<GrantedAuthority> copyAuthorities(UserDetails user)
	{
		Collection<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : user.getAuthorities())
		{
			authorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
		}
		return authorities;
	}
	
	public static boolean isAnonymous()
	{
		Authentication auth=SecurityContextHolder.getContext().getAuthentication();
		//System.out.println("auth class="+auth.getClass().getName());
		return (auth instanceof AnonymousAuthenticationToken);
	}
	
	private static String getDebugInfo()
	{
		StringBuilder buffer=new StringBuilder();
		Authentication authentication=getAuthentication();
		if (authentication!=null)
		{
			buffer.append("Authentication class="+getAuthentication().getClass().getName()+"\n");
			Object principal=authentication.getPrincipal();
			if (principal!=null)
				buffer.append("principal class="+principal.getClass().getName()+"\n");
		}
		return buffer.toString(); 
	}
	
	public static UserDetails getUserDetails()
	{
		//System.out.println("getUserDetails");
		Authentication authentication=getAuthentication();
		if (authentication==null)
		{
			//System.out.println("authentication is null");
			return null;
		}
		//System.out.println("Authentication class="+authentication.getClass().getName());
		Object principal=authentication.getPrincipal();
		//System.out.println("principal class="+principal.getClass().getName());
		if (principal instanceof UserDetails)
		{
			//System.out.println("found instance of CUserDetails");
			return (UserDetails)principal;
		}
		else
		{
			//System.out.println("principal is not an instance of CUserDetails. returning null");
			return null;
		}
	}
	
	public static UserDetails getUserDetails(boolean nullokay)
	{
		UserDetails details=getUserDetails();
		if (details==null)
		{
			if (nullokay)
				return null;
			throw new CException("user ID is null\n"+getDebugInfo());
		}
		return details;
	}
	
	public static String getUsername()
	{
		UserDetails user=getUserDetails();
		if (user==null)
			return null;
		return user.getUsername();
	}
	
	public static Collection<GrantedAuthority> getAuthorities(final Collection<String> roles)
	{
		Collection<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();
		for (String role : roles)
		{
			authorities.add(new SimpleGrantedAuthority(role));
		}
		return authorities;
	}
	
	public static Collection<GrantedAuthority> getAuthorities(final String... roles)
	{
		return getAuthorities(Arrays.asList(roles));
	}
	
	public static String getSavedRequest(HttpServletRequest request, HttpServletResponse response, String dflt)
	{
		String redirect=getSavedRequest(request,response);
		if (redirect==null)
			return dflt;
		else return redirect;
	}
	
	public static String getSavedRequest(HttpServletRequest request, HttpServletResponse response)
	{
		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		if (savedRequest==null)
			return null;
		return savedRequest.getRedirectUrl();//.getFullRequestUrl();
	}
	
	public static AuthenticationException getLastException(HttpServletRequest request)
	{
		HttpSession session=request.getSession(false);
		if (session==null)
			return null;
		return (AuthenticationException)session.getAttribute(LAST_EXCEPTION);
	}
	
	public static String getReason(HttpServletRequest request)
	{
		AuthenticationException e=getLastException(request);
		String reason=e.getMessage();
		return reason.substring(reason.lastIndexOf(':')+1);
	}
	
	public static String getLastUsername(HttpServletRequest request)
	{
		HttpSession session=request.getSession(false);
		if (session==null)
			return null;
		return (String)session.getAttribute(LAST_USERNAME);
	}
	
	public static AuthenticationException getUnauthorizedException(HttpServletRequest request)
	{	
		return (AuthenticationException)request.getAttribute(ACCESS_DENIED);
	}
	
	public static Authentication getAuthentication()
	{	
		final SecurityContext context=SecurityContextHolder.getContext();
		if (context==null)
			return null;
		final Authentication authentication=context.getAuthentication();
		if (authentication==null)
			return null;
		return authentication;
	}
	
	public static void logout(HttpServletRequest request, HttpServletResponse response)
	{
		//System.out.println("logging out user: "+request.getRemoteUser());
		forgetMe(response,"/");
		endSession(request);
		clearContext();		
	}
	
	// invalidate session if there is one
	private static void endSession(HttpServletRequest request)
	{
		HttpSession session=request.getSession(false);
		if (session!=null)
			session.invalidate();
	}
	
	// erase the remember me cookie
	private static void forgetMe(HttpServletResponse response, String webapp)
	{
		Cookie cookie = new Cookie(REMEMBER_ME, null);
		cookie.setMaxAge(0);
		cookie.setPath(webapp); //You need to add this!!!!!
		response.addCookie(cookie);
	}
	
	private static void clearContext()
	{
		SecurityContextHolder.clearContext(); //invalidate authentication
	}

	////////////////////////////////////////////////////////////////
	
	/*
	public static String encodePassword(String password)
	{
		return encodePassword(password,null);
	}
	
	public static String encodePassword(String password, String salt)
	{
		int strength=256;
		PasswordEncoder encoder=new ShaPasswordEncoder(strength);
		return encoder.encodePassword(password,salt);
	}
	*/
	
	/////////////////////////////////////////////////////////////////////////////////

	private static LoginStatus getLoginStatus(AbstractAuthenticationEvent event)
	{
		LoginStatus status=LoginStatus.LOGIN;
		if (isFailure(event))
			status=LoginStatus.CREDENTIALS;
		return status;
	}
	
	private static boolean isAuthenticationEvent(ApplicationEvent evt)
	{
		if (!(evt instanceof AbstractAuthenticationEvent))
			return false;
		if (evt instanceof InteractiveAuthenticationSuccessEvent) // the login is already logged as AuthenticationSuccessEvent
			return false;
		return true;
	}
	
	private static String getAuthenticationMessage(AbstractAuthenticationEvent event)
	{
		String username=getUsername(event);
		String cls=ClassUtils.getShortName(event.getClass());
		WebAuthenticationDetails details=getDetails(event);
		String ipaddress=details.getRemoteAddress();
		String sessionid=details.getSessionId();
		
		StringBuilder buffer=new StringBuilder();
		buffer.append("User "+username+" login result: "+cls+"; ipaddress="+ipaddress+"; sessionid="+sessionid+";");
		if (isFailure(event))
			buffer.append(" exception="+getException(event).getMessage());
        return buffer.toString();
	}
	
	private static AuthenticationException getException(AbstractAuthenticationEvent evt)
	{
		AbstractAuthenticationFailureEvent failure=(AbstractAuthenticationFailureEvent)evt;
		return failure.getException();
	}
	
	private static boolean isFailure(AbstractAuthenticationEvent event)
	{
		return (event instanceof AbstractAuthenticationFailureEvent);
	}
	
	private static String getUsername(AbstractAuthenticationEvent event)
	{
        return event.getAuthentication().getName();
	}
	
	private static WebAuthenticationDetails getDetails(AbstractAuthenticationEvent event)
	{
		return (WebAuthenticationDetails)event.getAuthentication().getDetails();
	}
	
	public enum LoginStatus
	{
		LOGIN,
		LOGOUT,
		CREDENTIALS,
		EXPIRED,
		DISABLED,
		CONCURRENT,
		LOCKED
	};
}
