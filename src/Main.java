import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	
	//file to parse
	public static String dir;
	//file which is containing regex
	public static String reg;
	//display or not the algorithm duration
	public static boolean timerMode;
	//name of the output file
	public static String output;
	public static String extract;
	public static boolean selec;
	public static boolean trace = false;
	public static float factor = 10;
	
	
	//-----------------------------------------------------------
	//static attribute used for applyRegex()
	
	static double moyglob;
	static int intTmpName = 1, k = 1, inc = 0;
	static BufferedWriter bwdateid, bwiddate, bwtrash;
	static PrintTraces p;
	static HashMap<String, PrintTraces> hID;
	static HashMap<String, BufferedWriter> hIDdate;
	static Date d1;
	
	//-----------------------------------------------------------

	//main entry of the program.
	public static void main(String[] args){ 
		final long timeProg1 = System.currentTimeMillis();
		try {
			MapperOptions.setOptions(args);
		} catch (Exception e) {System.err.println("pb option"); System.exit(3);}
		createDir();
		
		Regex regex = new Regex(reg);
		String dateFormat = regex.parse(dir);
		try {
			applyRegex(regex, dateFormat);
		} catch (Exception e) {e.printStackTrace();}
		
		if(trace) {
			try {
				for(int i = 1; i < k; i++) {
					File f = new File(output+"/"+i);
					Parser.parser(f);
					f.delete();
				}
			}catch(Exception e) {e.printStackTrace();}
		}
		final long timeProg2 = System.currentTimeMillis();
		if (timerMode) {
            System.out.println("Program Duration: " + (timeProg2 - timeProg1) + " ms");
        }	
	}
	
	
	//create directory to stock generated files with the name given by the output option
	private static String createDir() {
    	String tmpName = null, fName = "test/RESULTATS/"+Main.output;
    	int i = 1;
    	File x = new File(fName);
		while(x.exists()) {
			tmpName = fName+i;
			x = new File(tmpName);
			i++;
		}
		if (tmpName != null) {
			fName = tmpName;
		}
		Main.output = fName;
		x = new File(fName);
		x.mkdirs();
		return fName;
	}
	
	
	//apply every regex on every line of the given file.
	private static void applyRegex(Regex regex, String dateFormat) throws IOException, ParseException{
		if(Main.selec) {
			File f = new File(output+"/trash");
			Main.bwtrash = new BufferedWriter(new FileWriter(f, true)); 
		}
		if(Main.extract.equals("dateid")) {
			bwdateid = new BufferedWriter(new FileWriter(new File(output+"/tmp"+intTmpName)));
			intTmpName++;
		}

		ArrayList<Pattern> alPat = new ArrayList<Pattern>();
		hID = new HashMap<String, PrintTraces>();
		hIDdate = new HashMap<String, BufferedWriter>();
		SimpleDateFormat sdf = null;
		if(dateFormat != null) {
			sdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
		}
		BufferedReader br = new BufferedReader(new FileReader(dir));
		int i = 0, cpt, nbLine = 0;
		//create and stock pattern with regex gave by the user
		for(int j = 0; j < regex.alRegex.size(); j++) {
			alPat.add(Pattern.compile(regex.alRegex.get(j)));
		}
		String line = br.readLine();
		while(line != null) {
			nbLine++;
			if(line == "") {continue;}//empty line
			cpt = 0;
			//test every regex on each line and stop when there is a match
			for(int j = 0; j < regex.alRegex.size(); j++) {
				Matcher m = alPat.get(j).matcher(line);
				if(m.find()) {
					switch(Main.extract) {
					case "id" :
						findID(m);
						break;
					
					case "date" :
						findDate(m, sdf);
						break;
						
					case "dateid" :
						findDateID(m, sdf);
						break;
						
					case "iddate" :
						findIDDate(m, sdf);
						break;
					}
					
					break;
				}
				cpt++;
			}
			if(cpt == regex.alRegex.size()) {
				if(Main.selec) {
					i++;
					Main.bwtrash.write(line+"\n");
				}
				else {
					System.err.println("line " + nbLine + " doesn't match with a regex. check your regex file");
					System.exit(3);
				}
			}
			line = br.readLine();
		}	
		br.close();
		if(Main.extract.equals("dateid")) {
			bwdateid.close();
		}
		switch(Main.extract) {
		case "date" :
			p.bw.close();
			break;
			
		case "id" :
			for(String s : hID.keySet()) {
				hID.get(s).bw.close();
			}
			break;
			
		case "dateid" :
			int num = endDateID(regex, alPat);
			k = num;
			break;
			
		case "iddate" :
			for(String s : hIDdate.keySet()) {
				hIDdate.get(s).close();
			}
			endIDDate(regex, alPat, sdf);
			break;
		}
		System.out.println("Done");
		if(Main.selec) {
			Main.bwtrash.close();
			System.out.println(i+" non-matched lines");
			System.out.println("CREATING FILE \"trash\" WHICH CONTAIN EVERY NON-MATCHED EVENT");
		} 
	}


	//use the id extract method
	public static void findID(Matcher m) throws IOException {
		if(hID.containsKey(m.group("idHOST"))) {
			hID.get(m.group("idHOST")).writeWithModif(m);
		}
		else {
			p = new PrintTraces(output+"/"+k);
			k++;
			hID.put(m.group("idHOST"), p);
			hID.get(m.group("idHOST")).writeWithModif(m);;
		}
	}
	
	//use the date extract method
	public static void findDate(Matcher m, SimpleDateFormat sdf) throws ParseException, IOException {
		double moy;
		Date d2 = sdf.parse(m.group("date"));
		//happen only at the beginning
		if(d1 == null){
			p = new PrintTraces(output+"/"+k);
			k++;
			p.writeWithModif(m);
			d1 = d2;
			return;
		}
		
		moy = d2.getTime()-d1.getTime();
		
		//happen at the beginning or when there is a cease
		if(moyglob == 0) {
			p.writeWithModif(m);
			moyglob = moy;
			inc++;
			d1 = d2;
			return;
		}
		
		//cease
		if(moy > factor*moyglob) {
			p.bw.close();
			p = new PrintTraces(output+"/"+k);
			p.writeWithModif(m);
			moyglob = 0;
			inc = 0;
			k++;
		}
		else{
			p.writeWithModif(m);
			moyglob = (moy+inc*moyglob)/(inc+1);
			inc++;
		}
		d1 = d2;
	}
	
	//use the dateid extract method : split traces in files with the date method without modifications.
	public static void findDateID(Matcher m, SimpleDateFormat sdf) throws ParseException, IOException {
		double moy;
		Date d2 = sdf.parse(m.group("date"));
		//happen only at the beginning
		if(d1 == null){
			p = new PrintTraces(output+"/"+k);
			bwdateid.write(m.group()+"\n");
			d1 = d2;
			return;
		}
		
		moy = d2.getTime()-d1.getTime();
		
		//happen at the beginning or when there is a cease
		if(moyglob == 0) {
			bwdateid.write(m.group()+"\n");
			moyglob = moy;
			inc++;
			d1 = d2;
			return;
		}
		
		//cease
		if(moy > factor*moyglob) {
			bwdateid.close();
			bwdateid = new BufferedWriter(new FileWriter(new File(output+"/tmp"+intTmpName)));
			intTmpName++;
			bwdateid.write(m.group()+"\n");
			moyglob = 0;
			inc = 0;
			k++;
		}
		else{
			bwdateid.write(m.group()+"\n");
			moyglob = (moy+inc*moyglob)/(inc+1);
			inc++;
		}
		d1 = d2;
	}
	

	private static void findIDDate(Matcher m, SimpleDateFormat sdf) throws IOException {
		if(hIDdate.containsKey(m.group("idHOST"))) {
			hIDdate.get(m.group("idHOST")).write(m.group()+"\n");
		}
		else {
			bwiddate = new BufferedWriter(new FileWriter(new File(output+"/tmp"+intTmpName)));
			intTmpName++;
			hIDdate.put(m.group("idHOST"), bwiddate);
			hIDdate.get(m.group("idHOST")).write(m.group()+"\n");
		}
	}
	
	//read every generated files to create new ones with string modifications
	public static int endDateID(Regex regex, ArrayList<Pattern> alPat) throws IOException {
		int num = 1;
		BufferedReader br;
		for(int n = 1; n < intTmpName; n++) {
			File x = new File(output+"/tmp"+n);
			br = new BufferedReader(new FileReader(x));
			hID.clear();
			String line = br.readLine();
			while(line != null) {
				if(line == "") {continue;}//empty line
				//test every regex on each line and stop when there is a match
				for(int j = 0; j < regex.alRegex.size(); j++) {
					Matcher m = alPat.get(j).matcher(line);
					if(m.find()) {
						//must rewrite the algo to change the counter and have logical names 
						if(hID.containsKey(m.group("idHOST"))) {
							hID.get(m.group("idHOST")).writeWithModif(m);
						}
						else {
							p = new PrintTraces(output+"/"+num);
							num++;
							hID.put(m.group("idHOST"), p);
							hID.get(m.group("idHOST")).writeWithModif(m);;
						}
						break;
					}
				}
				line = br.readLine();
			}
			for(String s : hID.keySet()) {
				hID.get(s).bw.close();
			}
			br.close();
			x.delete();
		}
		return num;
	}
	
	public static int endIDDate(Regex regex, ArrayList<Pattern> alPat, SimpleDateFormat sdf) throws IOException, ParseException {
		BufferedReader br;
		for(int n = 1; n < intTmpName; n++) {
			File x = new File(output+"/tmp"+n);
			br = new BufferedReader(new FileReader(x));
			String line = br.readLine();
			p = new PrintTraces(output+"/"+k);
			k++;
			while(line != null) {
				if(line == "") {continue;}//empty line
				//test every regex on each line and stop when there is a match
				for(int j = 0; j < regex.alRegex.size(); j++) {
					Matcher m = alPat.get(j).matcher(line);
					if(m.find()) {
						double moy;
						Date d2 = sdf.parse(m.group("date"));
						//happen only at the beginning
						if(d1 == null){
							p.writeWithModif(m);
							d1 = d2;
							break;
						}
						
						moy = d2.getTime()-d1.getTime();
						
						//happen at the beginning or when there is a cease
						if(moyglob == 0) {
							p.writeWithModif(m);
							moyglob = moy;
							inc++;
							d1 = d2;
							break;
						}
						
						//cease
						if(moy > factor*moyglob) {
							p.bw.close();
							p = new PrintTraces(output+"/"+k);
							p.writeWithModif(m);
							moyglob = 0;
							inc = 0;
							k++;
						}
						else{
							p.writeWithModif(m);
							moyglob = (moy+inc*moyglob)/(inc+1);
							inc++;
						}
						d1 = d2;
						break;
					}
				}
				
				line = br.readLine();
			}
			p.bw.close();
			br.close();
			x.delete();
		}
		return k;
	}

}
