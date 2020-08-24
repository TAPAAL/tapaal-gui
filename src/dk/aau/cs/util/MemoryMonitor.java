package dk.aau.cs.util;

import com.sun.jna.*;
import dk.aau.cs.debug.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryMonitor {

	private static long PID = -1;
	private static Semaphore busy = new Semaphore(1);
	private static double peakMemory = -1;
	private static Boolean cumulativePeakMemory = false;
	private static DecimalFormat formatter = null;
	
	private static DecimalFormat getFormatter(){
		if(formatter == null){
			formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
			formatter.setGroupingUsed(false);
			formatter.setMaximumFractionDigits(0);
		}
		return formatter;
	}

	public static void attach(Process p){
		PID = p.pid();
		
		if( ! cumulativePeakMemory) {
			peakMemory = -1;
		}
		
		cumulativePeakMemory = false;
	}
	
	public static void detach(){
		PID = -1;
		peakMemory = -1;
	}

	public static boolean isAttached(){
		return PID != -1;
	}
	
	public static void cumulateMemory() {
		cumulativePeakMemory = true;
	}

	public static String getUsage(){
		if(busy.tryAcquire()){
			double memory = -1;
			if(Platform.isWindows()){
				try { 
					Process p = Runtime.getRuntime().exec("tasklist /FI \"pid eq "+PID+"\" /FO \"LIST\""); 
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream())); 

					StringBuilder s = new StringBuilder();
					String tmp = input.readLine();
					while(tmp != null){
						s.append(tmp.trim());
						tmp = input.readLine();
					}

					Pattern pattern = Pattern.compile(".*?([.,0-9]*) K.*");
					Matcher m = pattern.matcher(s.toString());
					if(m.matches()){
						memory = Double.parseDouble(m.group(1).replace(".", "").replace(",", ""))/1024;
					}
				} catch (IOException e) {
				    Logger.log(e);
				} 
			}else{
				try {
					Process p = Runtime.getRuntime().exec("ps -p "+PID+" -o rss"); 
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String s = input.readLine(); //Actual memory usage is second line output
                    s = input.readLine();

                    if (s!=null) { //Not sure why s would be null, but it seems to happen some time on MacOS
                        memory = Double.parseDouble(s.replace(" ", "")) / 1024;
                    }
				} catch (IOException e) {
                    Logger.log(e);
				} 
			}

			busy.release();
			if(memory < 0){
				return null;
			}else{
				if(memory > peakMemory)	peakMemory = memory;
				return getFormatter().format(memory) + " MB";
			}
		}else{
			return null;
		}
	}

	public static String getPeakMemory(){
		return peakMemory == -1? "N/A":getFormatter().format(Math.ceil(peakMemory)) + " MB";
	}
	
	public static int getPeakMemoryValue(){
		return peakMemory == -1? 0:(int) Math.ceil(peakMemory);
	}
}
