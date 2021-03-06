/*
 RDS Surveyor -- RDS decoder, analyzer and monitor tool and library.
 For more information see
   http://www.jacquet80.eu/
   http://rds-surveyor.sourceforge.net/
 
 Copyright (c) 2009, 2010 Christophe Jacquet

 This file is part of RDS Surveyor.

 RDS Surveyor is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 RDS Surveyor is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser Public License for more details.

 You should have received a copy of the GNU Lesser Public License
 along with RDS Surveyor.  If not, see <http://www.gnu.org/licenses/>.

*/

package eu.jacquet80.rds.core;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import eu.jacquet80.rds.app.Application;
import eu.jacquet80.rds.app.oda.ODA;
import eu.jacquet80.rds.log.RDSTime;


public class TunedStation extends Station {
	private SortedMap<Integer, Station> otherNetworks;  // maps ON-PI -> OtherNetwork
	private int[][] groupStats = new int[17][2];
	private Date date = null;
	private TimeZone timeZone = new SimpleTimeZone(0, "");
	private String datetime = "";
	private RDSTime streamTimeForDate = null;
	private Application[] applications = new Application[32];
	private List<Application> applicationList = new ArrayList<Application>();
	private boolean diMusic;
	private boolean diStereo, diArtif, diCompressed, diDPTY;
	private int totalBlocks, totalBlocksOk;
	private int[] latestBlocksOk = new int[25];
	private int latestBlocksOkPtr = 0;
	private int latestBlocksOkCount = 0;
	private int ecc, language;
	private Text rt = new Text(64);
	private UnicodeString lps = new UnicodeString(64, StandardCharsets.UTF_8);
	private ServiceStat serviceStat;
	
	private final List<String> trafficEvents = new ArrayList<String>();
	
	private Map<Integer, Integer> odas = new HashMap<Integer, Integer>();
	private Map<Integer, Application> odaApps = new HashMap<Integer, Application>();
	
	
	public TunedStation(int pi, RDSTime time) {
		reset(pi);
		pingPI(time);
		registerForcedODAs();
	}
	
	public TunedStation(RDSTime time) {
		this(0, time);
	}

	
	protected void reset(int pi) {
		super.reset(pi);
		
		// reset radiotext
		rt.reset();
		
		synchronized(this) {
			otherNetworks = new TreeMap<Integer, Station>();
		}
		
		for(int i=0; i<16; i++)
			for(int j=0; j<2; j++)
				groupStats[i][j] = 0;
		
		date = null;
		
		for(int i=0; i<groupStats.length; i++)
			for(int j=0; j<2; j++)
				groupStats[i][j] = 0;
		totalBlocks = 0;
		totalBlocksOk = 0;
		
		applications = new Application[32];
		
		serviceStat = new ServiceStat();
	}

	
	public String toString() {
		StringBuffer res = new StringBuffer();
		//System.out.println("pi=" + pi + ", ps=" + new String(ps) + ", time=" + timeOfLastPI);
		res.append(String.format("PI=%04X    Station name=\"%s\"    PS=\"%s\"    Time=%s", pi, getStationName(), ps.toString(), timeOfLastPI.toString()));
		
		res.append(String.format("\nRT = \"%s\"", rt.toString()));

		synchronized(this) {
			for(Station on : otherNetworks.values()) res.append("\nON: ").append(on);
		}
		
		// AFs
		res.append("\n").append(afsToString());
		
		//res.append("\n\t\tquality = " + quality);
		
		res.append("\n" + groupStats());
		
		if(date != null) res.append("\nLatest CT: " + date);
		
		res.append("\nPTY: " + pty + " -> " + getPTYlabel());
		if(ptyn != null) res.append(", PTYN=" + ptyn);
		
		res.append("\nDI: ")
				.append(!diStereo ? "Mono" : "Stereo").append(", ")
				.append(!diArtif ? "Not artificial head" : "Artificial head").append(", ")
				.append(!diCompressed ? "Not compressed" : "Compressed").append(", ")
				.append(!diDPTY ? "Static PTY" : "Dynamic PTY")
				.append("\n");
		
		if(ecc != 0) {
			res.append("Country: " + RDS.getISOCountryCode((pi>>12)&0xF, ecc)).append("\n");
		}
		
		if(language < RDS.languages.length) res.append("Language: ").append(RDS.languages[language][0]).append("\n");
				
		
		return res.toString();
	}
	
	public String groupStats() {
		StringBuffer res = new StringBuffer();
		for(int i=0; i<16; i++)
			for(int j=0; j<2; j++)
				if(groupStats[i][j] > 0) res.append(String.format("%d%c: %d,   ", i, (char)('A' + j), groupStats[i][j]));
		res.append("U: " + groupStats[16][0]);
		return res.toString();
	}
	
	public int[][] numericGroupStats() {
		return groupStats;
	}

	public RDSTime getTimeOfLastPI() {
		return timeOfLastPI;
	}
	
	public void addGroupToStats(int type, int version, int nbOk) {
		/*
		if(type<0 || type>15) {
			type = 16;
			version = 0;
		}
		*/
		groupStats[type][version]++;
		totalBlocks += 4;
		totalBlocksOk += nbOk;
		
		latestBlocksOk[latestBlocksOkPtr] = nbOk;
		latestBlocksOkPtr = (latestBlocksOkPtr + 1) % latestBlocksOk.length;
		if(latestBlocksOkCount < latestBlocksOk.length) latestBlocksOkCount++;
	}
	
	public void addUnknownGroupToStats(int nbOk) {
		addGroupToStats(16, 0, nbOk);
	}
	
	/**
	 * Returns the block error rate, averaged over the 25 latest groups
	 * (slightly more than 2 seconds).
	 * 
	 * @return the BLER
	 */
	public double getBLER() {
		int bOk = 0;
		for(int i=0; i<latestBlocksOkCount; i++) bOk += latestBlocksOk[i];
		return 1. - ((double)bOk) / (4*latestBlocksOkCount);
	}
	
	public void setApplicationForGroup(int type, int version, Application app) {
		applications[(type<<1) | version] = app;
		applicationList.add(app);
	}
	
	public Application getApplicationForGroup(int type, int version) {
		 return applications[(type<<1) | version];
	}
	
	public List<Application> getApplications() {
		return applicationList;
	}
	
	public void setDate(Date date, String datetime, RDSTime streamTime) {
		/* leave streamTimeForDate unchanged unless date has just changed */
		if (!date.equals(this.date))
			this.streamTimeForDate = streamTime;
		this.date = date;
		this.datetime = datetime;
	}
	
	public String getDateTime() {
		return datetime;
	}
	
	public synchronized void addON(Station on) {
		otherNetworks.put(on.getPI(), on);
	}
	
	public synchronized Station getON(int onpi) {
		return otherNetworks.get(onpi);
	}
	
	public synchronized int getONcount() {
		return otherNetworks.size();
	}
	
	public synchronized Station getONbyIndex(int idx) {
		int i = 0;
		for(Station s : otherNetworks.values()) {
			if(i == idx) return s;
			i++;
		}
		return null;
	}
	
	public Text getRT() {
		return rt;
	}
	
	public UnicodeString getLPS() {
		return lps;
	}
	
	public void setMusic(boolean diMusic) {
		this.diMusic = diMusic;
	}
	
	public void setDIbit(int addr, boolean diInfo, PrintWriter console) {
		console.print("DI:");
		switch(addr) {
		case 3: console.print(diInfo ? "Ster" : "Mono"); diStereo = diInfo; break;
		case 2: console.print(diInfo ? "ArtH" : "NArH"); diArtif = diInfo; break;
		case 1: console.print(diInfo ? "Comp" : "NCmp"); diCompressed = diInfo; break;
		case 0: console.print(diInfo ? "DPTY" : "SPTY"); diDPTY = diInfo; break;
		}
		console.print(", ");
	}
	
	public int getTotalBlocks() {
		return totalBlocks;
	}
	
	public int getTotalBlocksOk() {
		return totalBlocksOk;
	}
	
	public void setECC(int ecc) {
		this.ecc = ecc;
		
		// is this an RBDS country?
		this.rbds = ecc == 0xA0 || 						// US
					ecc == 0xA1 && pi < 0xF000 ||		// Canada
					ecc == 0xA5;						// Mexico
	}
	
	public int getECC() {
		return ecc;
	}
	
	public void setLanguage(int lang) {
		this.language = lang;
	}
	
	public int getLanguage() {
		return language;
	}
	
	/**
	 * @brief Returns a {@code Date} which corresponds to the given RDS time.
	 * 
	 * @param time The RDS time
	 * @return The exact time which corresponds to {@code time}
	 */
	public Date getRealTimeForStreamTime(RDSTime time) {
		if(time == null) return null;
		return time.getRealTime(streamTimeForDate, date);
	}
	
	/**
	 * Returns the value of the Music/Speech (M/S) flag.
	 * 
	 * @return {@code true} for Music, {@code false} for Speech.
	 */
	public boolean getMusic() {
		return diMusic;
	}
	
	public boolean getStereo() {
		return diStereo;
	}
	
	public boolean getArtificialHead() {
		return diArtif;
	}
	
	public boolean getCompressed() {
		return diCompressed;
	}
	
	public boolean getDPTY() {
		return diDPTY;
	}
	
	public void setODA(int aid, int group, Application app) {
		odas.put(aid, group);
		odaApps.put(aid, app);
	}
	
	public Collection<Integer> getODAs() {
		return odas.keySet();
	}
	
	public int getODAgroup(int aid) {
		return odas.get(aid);
	}
	
	public Application getODAapplication(int aid) {
		return odaApps.get(aid);
	}
	
	public String getCompactGroupStats() {
		StringBuilder b = new StringBuilder();
		List<GroupStatElement> groups = new ArrayList<GroupStatElement>(32);
		for(int g = 0; g<16; g++) {
			for(int v=0; v<2; v++) {
				if(groupStats[g][v] > 0) {
					groups.add(new GroupStatElement(g, v, groupStats[g][v]));
				}
			}
		}
		
		Collections.sort(groups);
		for(GroupStatElement g : groups) {
			g.append(b).append(' ');
		}
		
		return b.toString();
	}
	
	public void addTrafficEvent(RDSTime time, String description) {
		this.trafficEvents.add(time + ": " + description);
	}
	
	public List<String> getTrafficEventsList() {
		return trafficEvents;
	}
	
	/**
	 * @brief Sets the time zone for this station.
	 * 
	 * Note that all internal time values are time zone agnostic and will not change when a different
	 * time zone is set. An exception is getDateTime, which is passed as a string but is also not
	 * altered by this method.
	 *  
	 * @param tz The new time zone.
	 */
	public void setTimeZone(TimeZone tz) {
		this.timeZone = tz;
	}
	
	/**
	 * @brief Returns the time zone for this station.
	 * 
	 * The time zone can be used to display the date and time in the station's time zone, or to
	 * calculate reference points such as "midnight" (used by some ODA applications, notably TMC).
	 * 
	 * @return The current time zone.
	 */
	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public void addServiceStat(ServiceStat statsOfGroup) {
		int totalBitCount = statsOfGroup.getTotalCount();
		
		if(totalBitCount != 64) {
			System.err.println("addServiceStat: each bit of a group's 64 bits must be assigned. " 
					+ "Only " + totalBitCount + " bits were assigned.");
		}
		
		serviceStat.merge(statsOfGroup);
	}
	
	
	/* Forced ODAs */
	
	private static Map<Integer, Integer> forcedODAs = new HashMap<Integer, Integer>();
	
	public static void addForcedODA(int groupId, int aid) {
		forcedODAs.put(groupId, aid);
	}
	
	public void registerForcedODAs() {
		for(Map.Entry<Integer, Integer> e : forcedODAs.entrySet()) {
			int group = e.getKey();
			int groupN = group>>1;
			int groupV = group & 1;
			ODA app = ODA.forAID(e.getValue());
			this.setODA(e.getValue(), e.getKey(), app);
			
			if(app != null) {
				setApplicationForGroup(groupN, groupV, app);
				app.setStation(this);
			}
		}
	}

}


class GroupStatElement implements Comparable<GroupStatElement> {
	private final int group;
	private final int groupVersion;
	private final int count;
	
	public GroupStatElement(int group, int groupVersion, int count) {
		this.group = group;
		this.groupVersion = groupVersion;
		this.count = count;
	}
	
	@Override
	public int compareTo(GroupStatElement o) {
		return o.count - count;
	}
	
	public StringBuilder append(StringBuilder b) {
		b.append(group).append(groupVersion == 0 ? 'A' : 'B');
		return b;
	}
}

class ServiceStat {
	private Map<String, Integer> stats = new HashMap<String, Integer>();
	
	public void add(String service, int bits) {
		Integer previousCount = stats.get(service);
		if(previousCount == null) previousCount = 0;
		stats.put(service, bits + previousCount);
	}
	
	public int getTotalCount() {
		int count = 0;
		for(int c : stats.values()) {
			count += c;
		}
		
		return count;
	}
	
	public void merge(ServiceStat other) {
		for(Map.Entry<String, Integer> e : other.stats.entrySet()) {
			this.add(e.getKey(), e.getValue());
		}
	}
	
	public static final String 
			OVERHEAD = "Protocol overhead",		// addressing, very basic features, etc.
			PROG_TYPE = "Program type",		// PTY + TA/TP
			PI = "Program Identification",
			NAME = "Station name",
			RT = "Radiotext",
			AF = "Alternative frequencies",
			ON = "Other networks",
			CT = "Clock time",
			PAGING = "Paging",
			IH = "In-house data",
			TDC = "Transparent data channels",
			PTYN = "Program type name",
			WASTE = "Wasted bandwidth",
			PIN = "Program Item Number",
			ODA = "ODA";
}