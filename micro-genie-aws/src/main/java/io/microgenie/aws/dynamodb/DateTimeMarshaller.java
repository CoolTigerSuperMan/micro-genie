package io.microgenie.aws.dynamodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

/**
 * Marshal to 8601
 * 
 * @author shawn
 */
public class DateTimeMarshaller implements DynamoDBMarshaller<Date> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeMarshaller.class);
	
	private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public String encodeDate(Date date) {
		final SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		final String result = dateFormatter.format(date);
		return result.substring(0, result.length() - 2) + ":"
				+ result.substring(result.length() - 2);
	}

	public Date decodeDate(String value) throws ParseException {
		String javaValue = value.substring(0, value.length() - 3)
				+ value.substring(value.length() - 2);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		return dateFormatter.parse(javaValue);
	}

	@Override
	public String marshall(Date date) {
		return this.encodeDate(date);
	}

	@Override
	public Date unmarshall(Class<Date> clazz, String value) {
		try {
			return this.decodeDate(value);
		} catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
