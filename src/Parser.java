

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	
	/**
	 * This class is used to create input for COnfECt tool
	 * by deleting values. 
	 */
	
	public static void parser(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Main.output+"/trace"+f.getName())));
		Pattern p1 = Pattern.compile("=[^;<]*;");
		Pattern p2 = Pattern.compile("=.*\\)");
		String s = null, line = br.readLine();
		while(line != null) {
			Matcher m = p1.matcher(line);
			while(m.find()) {
				s = line.replace(m.group(), ";");
				line = s;
				m = p1.matcher(line);
			}
			m = p2.matcher(line);
			if(m.find()) {
				s = line.replace(m.group(), ")");
			}
			bw.write(s+"\n");
			line = br.readLine();
		}
		br.close();
		bw.close();		
	}
	
}
