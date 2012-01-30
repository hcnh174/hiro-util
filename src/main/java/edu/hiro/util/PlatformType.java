package edu.hiro.util;

public enum PlatformType
{
	AIX("AIX",Platform.UNIX),
	DIGITAL_UNIX("Digital Unix",Platform.UNIX),
	FREE_BSD("FreeBSD",Platform.UNIX),
	HP_UX("HP UX",Platform.UNIX),
	IRIX("Irix",Platform.UNIX),
	LINUX("Linux",Platform.UNIX),
	MAC("Mac OS",Platform.UNIX),
	MPE("MPE/iX",Platform.OTHER),
	NETWARE("Netware 4.11",Platform.OTHER),
	OS2("OS/2",Platform.OTHER),
	SOLARIS("Solaris",Platform.UNIX),
	WIN2000("Windows 2000",Platform.WINDOWS),
	WIN95("Windows 95",Platform.WINDOWS),
	WIN98("Windows 98",Platform.WINDOWS),
	WINNT("Windows NT",Platform.WINDOWS),
	WINXP("Windows XP",Platform.WINDOWS),
	WIN7("Windows 7",Platform.WINDOWS),
	WINVISTA("Windows NT (unknown)",Platform.WINDOWS);
	
	public enum Platform{WINDOWS,UNIX,OTHER};
	protected String osname;
	protected Platform platform;
		
	PlatformType(String osname, Platform platform)
	{
		this.osname=osname;
		this.platform=platform;
	}
	
	public String getOsname(){return this.osname;}
	public Platform getPlatform(){return this.platform;}
	
	public boolean isWindows(){return this.platform==Platform.WINDOWS;};
	public boolean isUnix(){return this.platform==Platform.UNIX;};
	
	public static PlatformType find(String osname)
	{
		System.out.println(osname);
		for (PlatformType type : values())
		{
			if (type.getOsname().equals(osname))
				return type;
		}
		throw new CException("unrecognized os name: "+osname);
	}
	
	public static PlatformType find()
	{
		String osname=System.getProperty("os.name");
		return find(osname);
	}
	
	public static String getBatchFileExtension()
	{
		PlatformType.Platform platform=find().getPlatform();
		if (platform==PlatformType.Platform.WINDOWS)
			return ".bat";
		else if (platform==PlatformType.Platform.UNIX)
			return ".sh";
		else return ".sh";
	}
}
