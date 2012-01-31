package edu.hiro.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public final class RandomHelper
{
	private RandomHelper(){}
	
	public static int randomInteger(int size)
	{
		Random random=new Random();
		return random.nextInt(size);
	}
	
	public static int randomInteger(int min, int max)
	{
		return min+randomInteger(max-min);
	}
	
	public static String randomText(String ... args)
	{
		List<String> items=Arrays.asList(args);
		return items.get(RandomHelper.randomInteger(items.size()));
	}
	
	public static Date randomDate()
	{
		return DateHelper.setDate(1950+RandomHelper.randomInteger(60), RandomHelper.randomInteger(12)+1, RandomHelper.randomInteger(30)+1);
	}
	
	public static Date randomDate(int minyear)
	{
		return DateHelper.setDate(minyear+RandomHelper.randomInteger(60), RandomHelper.randomInteger(12)+1, RandomHelper.randomInteger(30)+1);
	}

//	public static String getRandomWord(int min, int max)
//	{
//		int length=min+randomInteger(max);
//		StringBuilder buffer=new StringBuilder();
//		for (int index=0;index<length;index++)
//		{
//			buffer.append(StringHelper.ALPHABET.charAt(randomInteger(26)));
//		}
//		return buffer.toString();
//	}
	
	public static String randomWords(int min, int max)
	{
		int length=min+randomInteger(max);
		StringBuilder buffer=new StringBuilder();
		for (int index=0;index<length;index++)
		{
			buffer.append(randomWord(3,15));
		}
		return buffer.toString();
	}
	
	public static String randomWord(int min, int max)
	{
		int length=min+randomInteger(max);
		StringBuilder buffer=new StringBuilder();
		for (int index=0;index<length;index++)
		{
			buffer.append(StringHelper.ALPHABET.charAt(randomInteger(26)));
		}
		return buffer.toString();
	}
}
