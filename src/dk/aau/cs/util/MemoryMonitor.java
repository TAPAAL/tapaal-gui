package dk.aau.cs.util;

import com.sun.jna.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryMonitor {

	static interface Kernel32 extends Library {

		public static Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

		public int GetProcessId(Long hProcess);
	}

	private static int PID = -1;
	private static Semaphore busy = new Semaphore(1);
	
	public static void attach(Process p){
		PID = getPid(p);
		System.out.println("Process "+PID+" started.");
	}
	
	public static void detach(Process p){
		if(PID == getPid(p)){
			PID = -1;
			System.out.println("MemoryMonitor detached.");
		}
	}
	
	public static boolean isAttached(){
		return PID != -1;
	}
	
	public static String getUsage(){
		if(busy.tryAcquire()){
			double memory = -1;
			DecimalFormat formatter = new DecimalFormat("#,##0.0");
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
	                memory = Double.parseDouble(m.group(1))/1024;
				} catch (Exception e) { 
	           	  System.err.println("Error reading memory: "+e.getMessage());
	           	  return e.getMessage();
	             } 
			}else{
				 try { 
	                 Process p = Runtime.getRuntime().exec("ps -p "+PID+" -o rss"); 
	                 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream())); 
	                 //Actual memory usage is second line output
	                 String s = input.readLine();
	                 s = input.readLine();
	                 memory = Double.parseDouble(s.replace(" ", ""))/1024;
	              } catch (Exception e) { 
	            	  System.err.println("Error reading memory: "+e.getMessage());
	              } 
			}
			
			busy.release();
			
			if(memory < 0){
				return "N/A";
			}else if(memory >= 1024){
				return formatter.format(memory/1024) + " GB";
			}else{
				return formatter.format(memory) + " MB";
			}
		}else{
			return null;
		}
	}

	private static int getPid(Process p) {
		Field f;

		try{
			if (Platform.isWindows()) {
				f = p.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				int pid = Kernel32.INSTANCE.GetProcessId((Long) f.get(p));
				return pid;
			} else {
				f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				int pid = (Integer) f.get(p);
				return pid;
			}
		}catch(Exception e){
			return -1;
		}
	}
}
