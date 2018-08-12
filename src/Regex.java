import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
	
	//contain every regular expression of the regex file.
	ArrayList<String> alRegex;
	BufferedReader br;
	
	//constructor
	public Regex(String reg) {
		alRegex = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(new File(reg)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	//main method of the class
	public String parse(String dir) {
		String dateFormat = null;
		try {
			dateFormat = addRegex();
			br.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		check();
		return dateFormat;
	}

	
	//check if every regex contain special fields
	private void check() {
		for(int i = 0; i < alRegex.size(); i++) {
			if(!alRegex.get(i).contains("<label>")) {
				System.err.println("regex must contain a <label>");
				System.exit(3);
			}
			switch(Main.extract) {
			case "date" :
				if(!alRegex.get(i).contains("<date>")) {
					System.err.println("regex must contain a <date>");
					System.exit(3);
				}
				break;
				
			case "id" :
				if(!alRegex.get(i).contains("<idHOST>")) {
					System.err.println("regex don't contain a <idHOST>");
					System.exit(3);
				}
				break;
				
			case "dateid" :
			case "iddate" :
				if(!alRegex.get(i).contains("<date>") || !alRegex.get(i).contains("<idHOST>")) {
					System.err.println("regex must contain a <date> and a <idHOST>");
					System.exit(3);
				}
				break;
			}
		}
	}

	
	//read the regex and add them in a ArrayList (alRegex)
	private String addRegex() throws IOException {;
		Pattern p = Pattern.compile("-(\\w)\\s(.*)");
		String dateFormat = null, line = br.readLine();
		while(line != null) {
			Matcher m = p.matcher(line);
			if(m.find()) {
				if(m.group(1).equals("r")) {
					alRegex.add(m.group(2));
				}
				if(m.group(1).equals("d")) {
					dateFormat = m.group(2);
				}
			}
			line = br.readLine();
		}
		return dateFormat;
	}

}
