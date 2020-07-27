package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

	Properties prop = null;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Config config = new Config("config/hadoop.properties");
		System.out.println(config.getValue("DEP_CON_ACDC"));
	}
	
	public Config(String filename){
		prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
		if(inputStream!=null){
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			System.out.println("sup?");
		}
	}
	public  String getValue(String key){
		return prop.getProperty(key);
	}
}
