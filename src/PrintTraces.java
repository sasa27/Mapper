import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;

public class PrintTraces {

	BufferedWriter bw;
	
	//constructor
	public PrintTraces(String fileName) {
		File f = new File(fileName);
		try {
			bw = new BufferedWriter(new FileWriter(f));
		} catch (IOException e) {e.printStackTrace();}
	}

	
	//sort and write into a new file.
	public void writeWithModif(Matcher m) throws IOException {
		try {
			bw.write(m.group("label")+"(");
		}catch(Exception e) {e.getStackTrace();}
		try {
			int n = 1;
			String a = m.group("param"+n).replaceFirst(":", "=");
			bw.write(a);
			n++;
			while(m.group("param"+n) != null) {
				if(m.group("param"+n).equals("")) {n++;continue;}
				a = m.group("param"+n).replaceFirst(":", "=");
				bw.write(";"+a);
				n++;
			}
		}catch(IllegalArgumentException e) {e.getStackTrace();}
		
		bw.write(")\n");
	}
}
