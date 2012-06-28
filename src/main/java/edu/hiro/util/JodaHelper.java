package edu.hiro.util;

import org.joda.time.DateTime;

public class JodaHelper
{
	public static DateTime createDate(int year, int month, int day)
	{
		return new DateTime(year,month,day, 0, 0, 0, 0);
	}
}
