package dongle;
public class Response 
{

	public final int responseCode;
	public final String apiResponse;

	public Response(int responseCode, String apiResponse) 
	{
		this.responseCode = responseCode;
		this.apiResponse = apiResponse;
	}

	@Override
	public String toString() 
	{
		return "RequestResult [resultCode=" + responseCode + ", apiResponse=" + apiResponse
				+ "]";
	}

}