package com.soundcu.ofac.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.springframework.http.HttpStatusCode;
import java.time.OffsetDateTime;

public class ServiceError
{
	private int code;
	private HttpStatusCode status;
	private String message;
	
	public ServiceError(int code, HttpStatusCode status, String message)
	{
		this.code = code;
		this.status = status;
		this.message = message;
	}

	public int getCode()
	{
		return code;
	}

	public int getError()
	{
		return status.value();
	}

	public String getMessage()
	{
		return message;
	}
	
	public String getTimestamp()
	{
		TimeZone zone = TimeZone.getTimeZone("America/Los_Angeles");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormatter.setTimeZone(zone);
        return dateFormatter.format(new Date()) + OffsetDateTime.now().getOffset().toString();
	}
}
