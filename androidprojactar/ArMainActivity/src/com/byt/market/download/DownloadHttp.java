package com.byt.market.download;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class DownloadHttp {

	private static final String CLIENT_VERSION_HEADER = "User-Agent";
	private static final int TIMEOUT = 25 * 1000;// 25 Seconds
	private static final int CONNECTIONTIMEOUT = 12 * 1000;// 12 Seconds
	private DefaultHttpClient mHttpClient;

	public DownloadHttp() {
		mHttpClient = createHttpClient();
	}

	public HttpResponse doHttpRequest(HttpRequestBase httpRequest)
			throws ClientProtocolException, IOException {
		return executeHttpRequest(httpRequest);
	}

	public HttpResponse doHttpPost(String url) throws ClientProtocolException,
			IOException {
		HttpPost sHttpPost = createHttpPost(url);
		return executeHttpRequest(sHttpPost);
	}

	public HttpResponse doHttpPost(String url, String params)
			throws ClientProtocolException, IOException {
		HttpPost sHttpPost = createHttpPost(url, params);
		return executeHttpRequest(sHttpPost);
	}

	public HttpResponse doHttpPost(String url, NameValuePair... params)
			throws ClientProtocolException, IOException {
		HttpPost sHttpPost = createHttpPost(url, params);
		return executeHttpRequest(sHttpPost);
	}

	public HttpResponse doHttpGet(String url) throws ClientProtocolException,
			IOException {
		HttpGet sHttpGet = createHttpGet(url);
		return executeHttpRequest(sHttpGet);
	}

	public HttpResponse doHttpGet(String url, NameValuePair... params)
			throws ClientProtocolException, IOException {
		HttpGet sHttpGet = createHttpGet(url, params);
		return executeHttpRequest(sHttpGet);
	}

	public HttpResponse executeHttpRequest(HttpRequestBase httpRequest)
			throws ClientProtocolException, IOException {
		HttpResponse sHttpResponse = mHttpClient.execute(httpRequest);
		DownloadLog.info(DownloadHttp.class, "yoyo http response code-->"
				+ sHttpResponse.getStatusLine().getStatusCode());
		return sHttpResponse;
	}

	private HttpPost createHttpPost(String url) {
		HttpPost sHttpPost = new HttpPost(url);
		return sHttpPost;
	}

	private HttpPost createHttpPost(String url, String params)
			throws UnsupportedEncodingException {
		HttpPost sHttpPost = createHttpPost(url);
		sHttpPost.setEntity(new StringEntity(params, HTTP.UTF_8));
		return sHttpPost;
	}

	private HttpPost createHttpPost(String url, NameValuePair... pairs)
			throws UnsupportedEncodingException {
		HttpPost sHttpPost = createHttpPost(url);
		sHttpPost.setEntity(new UrlEncodedFormEntity(formatParams(pairs),
				HTTP.UTF_8));
		return sHttpPost;
	}

	public HttpGet createHttpGet(String url) {
		HttpGet sHttpGet = new HttpGet(url);
		return sHttpGet;
	}

	private HttpGet createHttpGet(String url, NameValuePair... pairs) {
		String params = URLEncodedUtils.format(formatParams(pairs), HTTP.UTF_8);
		return createHttpGet(url + "?" + params);
	}

	private ArrayList<NameValuePair> formatParams(NameValuePair... pairs) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		if (pairs != null) {
			for (NameValuePair nameValuePair : pairs) {
				if (nameValuePair != null) {
					params.add(nameValuePair);
				}
			}
		}
		return params;
	}

	public static final DefaultHttpClient createHttpClient() {
		final SchemeRegistry supportedSchemes = new SchemeRegistry();

		supportedSchemes.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		supportedSchemes.register(new Scheme("https",
				new EasySSLSocketFactory(), 443));

		final HttpParams httpParams = createHttpParams();
		HttpClientParams.setRedirecting(httpParams, true);

		final ClientConnectionManager ccm = new ThreadSafeClientConnManager(
				httpParams, supportedSchemes);
		return new DefaultHttpClient(ccm, httpParams);
	}

	/**
	 * Create the default HTTP protocol parameters.
	 */
	private static final HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		HttpConnectionParams.setConnectionTimeout(params, CONNECTIONTIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		return params;
	}

	/**
	 * This socket factory will create ssl socket that accepts everything
	 */
	public static class EasySSLSocketFactory implements SocketFactory,
			LayeredSocketFactory {

		private static SocketFactory instance = new EasySSLSocketFactory();

		private SSLContext sslcontext = null;

		private static SSLContext createEasySSLContext() throws IOException {
			try {
				SSLContext context = SSLContext.getInstance("TLS");

				// Create a trust manager that does not validate certificate
				// chains
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// do nothing
					}

					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// do nothing
					
					}

				} };

				context.init(null, trustAllCerts, new SecureRandom());

				return context;
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}

		private SSLContext getSSLContext() throws IOException {
			if (this.sslcontext == null) {
				this.sslcontext = createEasySSLContext();
			}
			return this.sslcontext;
		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket,
		 *      java.lang.String, int, java.net.InetAddress, int,
		 *      org.apache.http.params.HttpParams)
		 */
		public Socket connectSocket(Socket sock, String host, int port,
				InetAddress localAddress, int localPort, HttpParams params)
				throws IOException, UnknownHostException,
				ConnectTimeoutException {
			int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
			int soTimeout = HttpConnectionParams.getSoTimeout(params);

			InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
			SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock
					: createSocket());

			if ((localAddress != null) || (localPort > 0)) {
				// we need to bind explicitly
				if (localPort < 0) {
					localPort = 0; // indicates "any"
				}
				InetSocketAddress isa = new InetSocketAddress(localAddress,
						localPort);
				sslsock.bind(isa);
			}

			sslsock.connect(remoteAddress, connTimeout);
			sslsock.setSoTimeout(soTimeout);
			return sslsock;

		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
		 */
		public Socket createSocket() throws IOException {
			return getSSLContext().getSocketFactory().createSocket();
		}

		/**
		 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
		 */
		public boolean isSecure(Socket socket) throws IllegalArgumentException {
			return true;
		}

		/**
		 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
		 *      java.lang.String, int, boolean)
		 */
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return getSSLContext().getSocketFactory().createSocket(socket,
					host, port, autoClose);
		}

		public boolean equals(Object obj) {
			return ((obj != null) && obj.getClass().equals(
					EasySSLSocketFactory.class));
		}

		public int hashCode() {
			return EasySSLSocketFactory.class.hashCode();
		}

		public static SocketFactory getSocketFactory() {
			return instance;
		}

	}

}
