package org.apache.lucene.analysis.th;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import org.apache.lucene.analysis.th.LexTo;

public class LangChecker{
	public static Set<String> lexitron;
	
	public static void createEnviroment(){
	
		try{
			lexitron = new HashSet<String>();
			FileInputStream fis = new FileInputStream("lexitronutf.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
		
			while((line = br.readLine()) != null){
				lexitron.add(line);
			};
			br.close();
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	
	public static float check(String content, LexTo lexto){
		return lexto.match(content);
	}

	
	
}
