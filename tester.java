import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import dk.aau.cs.TA.DiscreetFiringAction;
import dk.aau.cs.TA.FiringAction;
import dk.aau.cs.TA.SymbolicUppaalTrace;
import dk.aau.cs.TA.TimeDelayFiringAction;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNPlace;


public class tester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		/*Pattern p2 = Pattern.compile("@[a-zA-Z](([a-zA-Z])*([0-9])*(_)*)*@");
		
		String t = "E<>@p0@=4 and @p3@=2 ";
		Matcher m = p2.matcher(t);
		
		
		while (m.find()){
			
			System.out.println("a " + m.group());
			String a = m.group();
			
			a=a.replace("@", "");
			System.out.println("b "+ a);
			
			int i = 0;
			while (i<5){
				
				System.out.println("Token" + i +"."+a);
				
				i++;
			}
			
			System.out.println(t.replace("@p0@", "xxxx"));
			
			
		}*/
		
		/*TAPN t = new TAPN();
		
		t.transformQueriesToUppaal(8, "E<> ( P1 == 1 && P10 == 1 ) || ( P8 == 1 && P4 == 1)", System.out);
		*/
	/*	
		String A="P_lodk_im";
		
		if (!(A.contains("_im") && A.equals("P_lock"))){
			System.out.println("hhey");
		} else
			System.out.println("la");*/ 

		
	/*	String a="/home/kyrke/test8.xml";
		
		String[] tmp = a.split("");
		
		System.out.println(tmp[0]);*/
/*
		ArrayList<FiringAction> trace = new ArrayList<FiringAction>();
		ArrayList<FiringAction> trace2 = new ArrayList<FiringAction>();
		ArrayList<Float> tmpAgeOfTokens = new ArrayList<Float>();

		File f = new File("/home/kyrke/tmp/uppaal");
		Reader fileReader = new FileReader(f);
		BufferedReader traceReader = new BufferedReader(fileReader);

		
		/// THIS CODE WILL NOT WORK IF WE DONT HAVE THE LOCKING TOKEN
		
		String line;
		boolean collect = false;
		int turn = 0;
		DiscreetFiringAction dfa = new DiscreetFiringAction();
		while ((line = traceReader.readLine()) != null){

			if (line.contains("Delay:")){
				trace.add(new TimeDelayFiringAction(Float.parseFloat(line.replace("Delay:", "").trim())));
			
			
			
			}else if (line.contains("Transitions:")){
				collect = true;

			
			
			}else if (line.contains("State:")){
				tmpAgeOfTokens.clear(); // Clear old values
				
				traceReader.readLine(); // Read and disregard line
				line =  traceReader.readLine();
				
				String[] tmp = line.split(" ");
				
//				Save the age of all tokens
				
				for (String s : tmp){
					
					if (!(s.contains("#depth"))){
						
						
						String[] tmp2 = s.trim().split("=");
						
						tmpAgeOfTokens.add(Float.parseFloat(tmp2[1]));
						
						
					}
					
				}		
				
				collect = false;
				continue;

			}else{
				if (collect && turn <= 1){
					if (!line.equals("") && !line.contains("tau")){
						
						
						
						String tmp = line.trim();

						//Get the transition fired
						
						int first = tmp.indexOf("{");
						int second = tmp.indexOf("}");

						tmp = tmp.substring(first+1, second);

						String tmp2[] = tmp.split(",");

						tmp = tmp2[1].replace("!", "").replace("?", "");
						String transitionname = tmp;
						
						
						
						//Get the token consumed
						
						tmp2 = line.trim().split("\\.");
						
						
						tmp = tmp2[0].replace("Token", "");
						
						int tokenConsumed = Integer.parseInt(tmp);
						String placename = tmp2[1].split("->")[0];
					
						dfa.setTrasition(transitionname.trim());
						dfa.addConsumedToken(placename, tmpAgeOfTokens.get(tokenConsumed));


						//Add the tokens consumed


						turn++;

						if (turn==2){
							turn=0;
							
							collect=false;
							trace.add(dfa);
							dfa = new DiscreetFiringAction();
							
						}
						
						
					}
				}
			}

		}



		System.out.println("Trace is1");
		for (FiringAction s : trace){
			
			System.out.println(s);
			
		}
		System.out.println("!!!");
		
		
		String last = null;
		int lastnumber = 0;
		
		DiscreetFiringAction dfatmp = new DiscreetFiringAction();
		//trace2.add(dfatmp);
		
		for (FiringAction fa : trace){
			
			if (fa instanceof DiscreetFiringAction){
				//Do stuff
				
				DiscreetFiringAction dfa1 = (DiscreetFiringAction)fa;
								
				String tmp2[] = dfa1.getTransition().split("_");
				
				
				int tmp=0;
				try {
				 tmp = Integer.parseInt(tmp2[1].replace("T", ""));
				} catch (Exception e) {
					tmp=0;
				}
				//System.out.println(lastnumber + " " + tmp);
				if ((!tmp2[0].equals(last)) ||  tmp <= lastnumber){
					
					dfatmp = new DiscreetFiringAction();
					dfatmp.setTrasition(tmp2[0].trim());
					trace2.add(dfatmp);
					
					
					
					
					last = tmp2[0];
					
				}
				lastnumber = tmp;


				
				HashMap<String, ArrayList<Float>> tmpConsumedTokens = dfa1.getConsumedTokensList();
				
				for (String s : tmpConsumedTokens.keySet()){
					
					//Only add the consumed token if it is consumed from one of
					//the original places
					if (!(s.contains("_"))){
						dfatmp.addConsumedToken(s, tmpConsumedTokens.get(s).get(0)); // XXX - only takes first token
					}
					
				}
				
				
			} else {
				//Just add the time delay
				trace2.add(fa);
				lastnumber=0;
				last="";
			}
			
			
		}
		
		
		System.out.println("Trace is");
		for (FiringAction s : trace2){
			
			System.out.println(s);
			
		}
		

	*/
		/*
	    float f = 1;
	    float a;
		System.out.println(f);
		f = f+0.5f;
		System.out.println(f);
		System.out.println(f);
		f = f+ Float.parseFloat("0.02");
		a = f+ Float.parseFloat("0.00001");
		System.out.println(f);
		//f = f+0.0001f;
		System.out.println(f);
		//f = f+0.0001f;
		System.out.println(a);*/
		
		
		
		/*File f = new File("/home/kyrke/1.trace");
		Reader fileReader = new FileReader(f);
		BufferedReader traceReader = new BufferedReader(fileReader);
		
		SymbolicUppaalTrace sut = new SymbolicUppaalTrace();
		sut.parseUppaalAbstractTrace(traceReader);*/
		
		
		float a = 1.3f;
		
		System.out.println(a);
		
		
		
	}

}
