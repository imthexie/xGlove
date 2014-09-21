package dongle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

public class WebHelpers 
{

	private static OkHttpClient okHttpClient;
	private static Gson gson;
	private static boolean initialized = false;
	private final String UTF_8 = "UTF-8";

	// should be called during Application.onCreate() to ensure availability
	public void initialize() 
	{
		if(!initialized) 
		{
			okHttpClient = new OkHttpClient();
			gson = new GsonBuilder()
					.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
					.create();
			initialized = true;
		}
	}

	public static Gson getGson() 
	{
		return gson;
	}

	public static OkHttpClient getOkClient() 
	{
		return okHttpClient;
	}
	
	Response post(URL url, String stringData) 
	{
		return performRequestWithInputData(url, "POST", stringData);
	}

	Response put(URL url, String stringData) 
	{
		return performRequestWithInputData(url, "PUT", stringData);
	}

	Response performRequestWithInputData(URL url, String httpMethod, String stringData) 
	{
		HttpURLConnection connection = okHttpClient.open(url);
		OutputStream out = null;
		InputStream in = null;
		int responseCode = -1;
		String responseData = "";

		try 
		{
			try 
			{
				connection.setRequestMethod(httpMethod);
				connection.setDoOutput(true);

				out = connection.getOutputStream();
				out.write(stringData.getBytes(UTF_8));
				out.close();

				responseCode = connection.getResponseCode();

				in = connection.getInputStream();
				responseData = readAsUtf8String(in);

			} 
			finally 
			{
				// Clean up.
				if (out != null) 
				{
					out.close();
				}
				if (in != null) 
				{
					in.close();
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Error trying to make " + connection.getRequestMethod() + " request");
		}

		return new Response(responseCode, responseData);
	}

	Response get(URL url) 
	{
		HttpURLConnection connection = okHttpClient.open(url);
		InputStream in = null;
		int responseCode = -1;
		String responseData = "";
		// Java I/O throws *exception*al parties!
		try 
		{
			try 
			{
				responseCode = connection.getResponseCode();
				in = connection.getInputStream();
				responseData = readAsUtf8String(in);
			}
			finally 
			{
				if (in != null) 
				{
					in.close();
				}
			}
		} 
		catch (IOException e) 
		{
			System.out.println("Error trying to make GET request");
		}

		return new Response(responseCode, responseData);
	}
	
	ByteArrayOutputStream reusableResponseStream = new ByteArrayOutputStream(8192);

	String readAsUtf8String(InputStream in) throws IOException 
	{
		reusableResponseStream.reset();
		byte[] buffer = new byte[1024];
		for (int count; (count = in.read(buffer)) != -1;) 
		{
			reusableResponseStream.write(buffer, 0, count);
		}
		return reusableResponseStream.toString(UTF_8);
	}

	private static OkHttpClient disableTLSforStaging() 
	{
		OkHttpClient client = new OkHttpClient();
		client.setHostnameVerifier(new HostnameVerifier() 
		{

			public boolean verify(String hostname, SSLSession session) 
			{
				return true;
			}
		});

		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() 
				{
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {}
			} }, new SecureRandom());

			client.setSslSocketFactory(context.getSocketFactory());

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return client;
	}
}
