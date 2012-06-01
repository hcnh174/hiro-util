package edu.hiro.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

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
		//System.out.println("randomInteger: ("+min+"-"+max+")");
		if (min==max)
			max+=1;
		return min+randomInteger(max-min);
	}
	
	public static Float randomFloat()
	{
		Random random=new Random();
		return random.nextFloat();
	}
	
	public static Float randomFloat(float max)
	{
		return randomFloat()*max;
	}
//	
//	public static Float randomFloat(float min, float max)
//	{
//		return randomInteger((int)min,(int)max)+randomFloat();
//	}
//	
	public static Float randomFloat(float min, float max)
	{
		//System.out.println("randomFloat: ("+min+"-"+max+")");
		if (min>=0 && max<=1)
			return min+randomFloat(max-min);		
		float offset=0;
		if (min<0)
			offset=Math.abs(min);
		float value=randomInteger((int)(min+offset),(int)(max+offset))+randomFloat();
		value=value-offset;
		//System.out.println("random with negative min ("+min+"-"+max+"). offset="+offset+", value="+value);
		return value;
	}
	
	public static Boolean randomBoolean()
	{
		return randomBoolean(0.5f);
	}
	
	public static Boolean randomBoolean(double prob_true)
	{
		return randomFloat()<=prob_true;
	}
	
	public static String randomText(List<String> items)
	{
		return items.get(RandomHelper.randomInteger(items.size()));
	}
	
	public static String randomText(String ... args)
	{
		List<String> items=Arrays.asList(args);
		return randomText(items);
	}
		
	public static Date randomDate()
	{
		return DateHelper.setDate(1950+RandomHelper.randomInteger(60), RandomHelper.randomInteger(12)+1, RandomHelper.randomInteger(30)+1);
	}
	
	public static Date randomDate(int minyear)
	{
		return DateHelper.setDate(minyear+RandomHelper.randomInteger(60), RandomHelper.randomInteger(12)+1, RandomHelper.randomInteger(30)+1);
	}
	
	public static Date randomDate(int minyear, int maxyear)
	{
		int diff=maxyear-minyear+1;
		return DateHelper.setDate(minyear+RandomHelper.randomInteger(diff), RandomHelper.randomInteger(12)+1, RandomHelper.randomInteger(30)+1);
	}
	
	public static Date randomDate(Date mindate)
	{
		return DateHelper.addWeeks(mindate, RandomHelper.randomInteger(52*3));//up to three years later
	}
	
	public static Date randomDate(Date mindate, Date maxdate)
	{
		int minutes=DateHelper.getDuration(mindate, maxdate); //in minutes
		int weeks=minutes/(60*24*7);
		return DateHelper.addWeeks(mindate, RandomHelper.randomInteger(weeks));
	}
	
	public static <T> T randomItem(List<T> items)
	{
		return items.get(RandomHelper.randomInteger(items.size()));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T randomEnum(T values[])
	{
		//Enum<?> values[] = cls.getEnumConstants();
		//return (Enum<T>)values[RandomHelper.randomInteger(values.length)];
		return values[RandomHelper.randomInteger(values.length)];
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
	
	public static char randomLetter()
	{
		return StringHelper.alphabet.charAt(randomInteger(26));
	}
	
	public static String randomWords(int min, int max)
	{
		int length=min+randomInteger(max);
		List<String> buffer=Lists.newArrayList();
		for (int index=0;index<length;index++)
		{
			buffer.add(randomWord(3,15));
		}
		return StringHelper.join(buffer," ");
	}
	
	public static String randomWord(int min, int max)
	{
		int length=min+randomInteger(max);
		List<String> buffer=Lists.newArrayList();
		for (int index=0;index<length;index++)
		{
			buffer.add(""+randomLetter());
		}
		return StringHelper.join(buffer,"");
	}
}
