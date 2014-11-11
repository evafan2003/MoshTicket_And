package com.mosh.ticket.view;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * Wrap of org.apache.http.impl.client.DefaultHttpClient
 * 
 * @author lds
 * 
 */
public class HttpClient {

	private static final String TAG = "HttpClient";
	private final static boolean DEBUG = false;

	/** OK: Success! */
	public static final int OK = 200;
	/** Not Modified: There was no new data to return. */
	public static final int NOT_MODIFIED = 304;
	/**
	 * Bad Request: The request was invalid. An accompanying error message will
	 * explain why. This is the status code will be returned during rate
	 * limiting.
	 */
	public static final int BAD_REQUEST = 400;
	/** Not Authorized: Authentication credentials were missing or incorrect. */
	public static final int NOT_AUTHORIZED = 401;
	/**
	 * Forbidden: The request is understood, but it has been refused. An
	 * accompanying error message will explain why.
	 */
	public static final int FORBIDDEN = 403;
	/**
	 * Not Found: The URI requested is invalid or the resource requested, such
	 * as a user, does not exists.
	 */
	public static final int NOT_FOUND = 404;
	/**
	 * Not Acceptable: Returned by the Search API when an invalid format is
	 * specified in the request.
	 */
	public static final int NOT_ACCEPTABLE = 406;
	/**
	 * Internal Server Error: Something is broken. Please post to the group so
	 * the Weibo team can investigate.
	 */
	public static final int INTERNAL_SERVER_ERROR = 500;
	/** Bad Gateway: Weibo is down or being upgraded. */
	public static final int BAD_GATEWAY = 502;
	/**
	 * Service Unavailable: The Weibo servers are up, but overloaded with
	 * requests. Try again later. The search and trend methods use this to
	 * indicate when you are being rate limited.
	 */
	public static final int SERVICE_UNAVAILABLE = 503;

	private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;
	private static final int SOCKET_TIMEOUT_MS = 30 * 1000;

	public static final int RETRIEVE_LIMIT = 20;
	public static final int RETRIED_TIME = 3;

	private DefaultHttpClient mClient;
	private BasicHttpContext localcontext;

	private String mUserId;
	private String mPassword;

	private static boolean isAuthenticationEnabled = false;

	public HttpClient() {
		prepareHttpClient();
	}

	/**
	 * @param user_id
	 *            auth user
	 * @param password
	 *            auth password
	 */
	public HttpClient(String user_id, String password) {
		prepareHttpClient();
		// setCredentials(user_id, password);
	}

	/**
	 * Empty the credentials
	 */
	public void reset() {
		// setCredentials("", "");
	}

	/**
	 * @return authed user id
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @return authed user password
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * @param hostname
	 *            the hostname (IP or DNS name)
	 * @param port
	 *            the port number. -1 indicates the scheme default port.
	 * @param scheme
	 *            the name of the scheme. null indicates the default scheme
	 */
	public void setProxy(String host, int port, String scheme) {
		HttpHost proxy = new HttpHost(host, port, scheme);
		mClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	}

	public void removeProxy() {
		mClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
	}

	private void enableDebug() {
		Log.i(TAG, "enable apache.http debug");

		java.util.logging.Logger.getLogger("org.apache.http").setLevel(
				java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(
				java.util.logging.Level.FINER);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(
				java.util.logging.Level.OFF);
	}

	/**
	 * Setup DefaultHttpClient
	 * 
	 * Use ThreadSafeClientConnManager.
	 * 
	 */
	private void prepareHttpClient() {
		if (DEBUG) {
			enableDebug();
		}

		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 10);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		mClient = new DefaultHttpClient(cm, params);

	}

	public void setUserNamePas(String uname, String pwd) {
		this.mUserId = uname;
		this.mPassword = pwd;
	}

	public void setUserCookie(String key, String value) {
		BasicClientCookie cookie = new BasicClientCookie(key, value);
		cookie.setVersion(0);
		cookie.setDomain(".mosh.cn");
		cookie.setPath("/");
		mClient.getCookieStore().addCookie(cookie);
	}

	private Cookie getCookieValue(DefaultHttpClient httpclient, String name) {
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		if (cookies.isEmpty()) {
			return null;
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				Cookie cookie = cookies.get(i);
				if (cookie.getName().equalsIgnoreCase(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	private void writeCookie(DefaultHttpClient httpclient, String username)
			throws Exception {
		List<Cookie> myCookie = httpclient.getCookieStore().getCookies();
		if (myCookie == null || myCookie.size() == 0) {
			BasicClientCookie cookie = new BasicClientCookie("ssusername",
					username);
			// 设置�??容�?�?????页�??��?以�????�?ookie
			cookie.setPath("/");
			// 设置cookie???????�为2�??
			CookieStore cookies = httpclient.getCookieStore();
			cookies.addCookie(cookie);
			httpclient.setCookieStore(cookies);
		} else {
			boolean flag = false;
			for (int i = 0; i < myCookie.size(); i++) {
				BasicClientCookie cookie = (BasicClientCookie) myCookie.get(i);
				String cookiename = cookie.getName();
				if ("ssusername".equals(cookiename)) {
					cookie.setValue(username);
					flag = true;
					break;
				}
			}
			if (!flag) {
				BasicClientCookie cookie = new BasicClientCookie("ssusername",
						username);
				CookieStore cookies = httpclient.getCookieStore();
				cookies.addCookie(cookie);
				httpclient.setCookieStore(cookies);
			}
		}
	}

	public Response post(String url, ArrayList<BasicNameValuePair> postParams,
			boolean authenticated) throws Exception {
		if (null == postParams) {
			postParams = new ArrayList<BasicNameValuePair>();
		}
		return httpRequest(url, postParams, authenticated, HttpPost.METHOD_NAME);
	}

	public Response post(String url, ArrayList<BasicNameValuePair> params)
			throws Exception {
		return httpRequest(url, params, false, HttpPost.METHOD_NAME);
	}

	public Response post(String url, boolean authenticated) throws Exception {
		return httpRequest(url, null, authenticated, HttpPost.METHOD_NAME);
	}

	public Response post(String url) throws Exception {
		return httpRequest(url, null, false, HttpPost.METHOD_NAME);
	}

	public Response post(String url, File file) throws Exception {
		return httpRequest(url, null, file, false, HttpPost.METHOD_NAME);
	}

	/**
	 * POST�?��??��
	 * 
	 * @param url
	 * @param file
	 * @param authenticate
	 * @return
	 * @throws Exception
	 */
	public Response post(String url, File file, boolean authenticate)
			throws Exception {
		return httpRequest(url, null, file, authenticate, HttpPost.METHOD_NAME);
	}

	public Response get(String url, ArrayList<BasicNameValuePair> params,
			boolean authenticated) throws Exception {
		return httpRequest(url, params, authenticated, HttpGet.METHOD_NAME);
	}

	public Response get(String url, ArrayList<BasicNameValuePair> params)
			throws Exception {
		return httpRequest(url, params, false, HttpGet.METHOD_NAME);
	}

	public Response get(String url) throws Exception {
		return httpRequest(url, null, false, HttpGet.METHOD_NAME);
	}

	public Response get(String url, boolean authenticated) throws Exception {
		return httpRequest(url, null, authenticated, HttpGet.METHOD_NAME);
	}

	public Response httpRequest(String url,
			ArrayList<BasicNameValuePair> postParams, boolean authenticated,
			String httpMethod) throws Exception {
		return httpRequest(url, postParams, null, authenticated, httpMethod);
	}

	/**
	 * Execute the DefaultHttpClient
	 * 
	 * @param url
	 *            target
	 * @param postParams
	 * @param file
	 *            can be NULL
	 * @param authenticated
	 *            need or not
	 * @param httpMethod
	 *            HttpPost.METHOD_NAME HttpGet.METHOD_NAME
	 *            HttpDelete.METHOD_NAME
	 * @return Response from server
	 * @throws Exception
	 */
	public Response httpRequest(String url,
			ArrayList<BasicNameValuePair> postParams, File file,
			boolean authenticated, String httpMethod) throws Exception {
		Log.i(TAG, "Sending " + httpMethod + " request to " + url);
		long startTime = System.currentTimeMillis();

		URI uri = createURI(url);

		HttpResponse response = null;
		Response res = null;
		HttpUriRequest method = null;

		// Create POST, GET or DELETE METHOD
		method = createMethod(httpMethod, uri, file, postParams);
		// Setup ConnectionParams
		SetupHTTPConnectionParams(method);

		// Execute Request
		try {
			response = mClient.execute(method, localcontext);
			res = new Response(response);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception(e.getMessage(), e);
		} catch (IOException ioe) {
			throw new Exception(ioe.getMessage(), ioe);
		}

		if (response != null) {
			int statusCode = response.getStatusLine().getStatusCode();
			// It will throw a weiboException while status code is not 200
			HandleResponseStatusCode(statusCode, res);
		} else {
			Log.e(TAG, "response is null");
		}

		long endTime = System.currentTimeMillis();
		Log.d(TAG, "Http request in " + (endTime - startTime));

		return res;

	}

	/**
	 * CreateURI from URL string
	 * 
	 * @param url
	 * @return request URI
	 * @throws Exception
	 *             Cause by URISyntaxException
	 */
	private URI createURI(String url) throws Exception {
		URI uri;

		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception("Invalid URL.");
		}

		return uri;
	}

	/**
	 * @throws UnsupportedEncodingException
	 */
	private MultipartEntity createMultipartEntity(String filename, File file,
			ArrayList<BasicNameValuePair> postParams)
			throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity();
		// Don't try this. Server does not appear to support chunking.
		// entity.addPart("media", new InputStreamBody(imageStream, "media"));

		entity.addPart(filename, new FileBody(file));
		for (BasicNameValuePair param : postParams) {
			entity.addPart(param.getName(), new StringBody(param.getValue()));
		}
		return entity;
	}

	/**
	 * Setup HTTPConncetionParams
	 * 
	 * @param method
	 */
	private void SetupHTTPConnectionParams(HttpUriRequest method) {
		HttpConnectionParams.setConnectionTimeout(method.getParams(),
				CONNECTION_TIMEOUT_MS);
		HttpConnectionParams
				.setSoTimeout(method.getParams(), SOCKET_TIMEOUT_MS);
		mClient.setHttpRequestRetryHandler(requestRetryHandler);
		method.addHeader("Accept-Encoding", "gzip, deflate");
	}

	/**
	 * Create request method, such as POST, GET, DELETE
	 * 
	 * @param httpMethod
	 * @return httpMethod Request implementations for the various HTTP methods
	 *         like GET and POST.
	 * @throws Exception
	 *             createMultipartEntity ??UrlEncodedFormEntity�????OException
	 */
	private HttpUriRequest createMethod(String httpMethod, URI uri, File file,
			ArrayList<BasicNameValuePair> postParams) throws Exception {

		HttpUriRequest method;

		if (httpMethod.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
			// POST METHOD

			HttpPost post = new HttpPost(uri);
			// See this:
			// http://groups.google.com/group/twitter-development-talk/browse_thread/thread/e178b1d3d63d8e3b
			post.getParams().setBooleanParameter(
					"http.protocol.expect-continue", false);

			try {
				HttpEntity entity = null;
				if (null != file) {
					entity = createMultipartEntity("photo", file, postParams);
					post.setEntity(entity);
				} else if (null != postParams) {
					entity = new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
				}
				post.setEntity(entity);
			} catch (IOException ioe) {
				throw new Exception(ioe.getMessage(), ioe);
			}

			method = post;
		} else if (httpMethod.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
			method = new HttpDelete(uri);
		} else {
			method = new HttpGet(uri);
		}

		return method;
	}

	/**
	 * @param statusCode
	 * @return
	 */
	private static String getCause(int statusCode) {
		String cause = null;
		switch (statusCode) {
		case NOT_MODIFIED:
			break;
		case BAD_REQUEST:
			cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
			break;
		case NOT_AUTHORIZED:
			cause = "Authentication credentials were missing or incorrect.";
			break;
		case FORBIDDEN:
			cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
			break;
		case NOT_FOUND:
			cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
			break;
		case NOT_ACCEPTABLE:
			cause = "Returned by the Search API when an invalid format is specified in the request.";
			break;
		case INTERNAL_SERVER_ERROR:
			cause = "Something is broken.  Please post to the group so the Weibo team can investigate.";
			break;
		case BAD_GATEWAY:
			cause = "Weibo is down or being upgraded.";
			break;
		case SERVICE_UNAVAILABLE:
			cause = "Service Unavailable: The Weibo servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
			break;
		default:
			cause = "";
		}
		return statusCode + ":" + cause;
	}

	public boolean isAuthenticationEnabled() {
		return isAuthenticationEnabled;
	}

	public static void log(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	private void HandleResponseStatusCode(int statusCode, Response res)
			throws Exception {

	}

	public static String encode(String value) throws Exception {
		try {
			return URLEncoder.encode(value, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e_e) {
			throw new Exception(e_e.getMessage(), e_e);
		}
	}

	public static String encodeParameters(ArrayList<BasicNameValuePair> params)
			throws Exception {
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < params.size(); j++) {
			if (j != 0) {
				buf.append("&");
			}
			try {
				buf.append(URLEncoder.encode(params.get(j).getName(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(params.get(j).getValue(),
								"UTF-8"));
			} catch (java.io.UnsupportedEncodingException neverHappen) {
				throw new Exception(neverHappen.getMessage(), neverHappen);
			}
		}
		return buf.toString();
	}

	/**
     *     */
	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {

		@Override
		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {

			if (executionCount >= RETRIED_TIME) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// Do not retry on SSL handshake exception
				return false;
			}
			HttpRequest request = (HttpRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}
	};

}
