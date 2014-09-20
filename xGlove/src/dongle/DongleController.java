package dongle;

import dongle.Info;
import dongle.Response;
import dongle.WebHelpers;

import java.net.MalformedURLException;
import java.net.URL;

public class DongleController 
{
	private WebHelpers webhelpers;
	URL toggleUrl;
	
	public DongleController() 
	{
		webhelpers = new WebHelpers();
		webhelpers.initialize();
		
		//Build toggle request URL
		try {
			toggleUrl = new URL(Info.URL + Info.VERSION_CODE + Info.DEVICES + "/" + 
							Info.DEVICE_ID + Info.ACTION_TOGGLE);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void toggle() {		
		//Make request
		Response response = null;
		if(toggleUrl != null) 
			response = webhelpers.post(toggleUrl, "access_token=" + Info.TOKEN);
		
		//Print response
		if(response != null) 
			System.out.println(response.toString());
	}

}
