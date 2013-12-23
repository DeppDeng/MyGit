package pers.diskMng;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackupFilesUtil {

	private static String myConfig = "./myConfig.properties";
	private Properties p;
	
	public BackupFilesUtil(){
		if(p == null){
			p = new Properties();
		}
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(myConfig));
			p.load(in);
		}catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		BackupFilesUtil instance = new BackupFilesUtil();
//		instance.createFolderForMovies();
		instance.renameMp4FileNameForIPod();
		
	}

	public void createFolderForMovies() throws IOException {

		String movieParentFolder = (String) p.get("localTestFolder");

		File movieFolder = new File(movieParentFolder);

		File[] files = movieFolder.listFiles();

		int fileCount = files.length;
		if (fileCount > 0) {
			for (int i = 0; i < fileCount; i++) {
				File file = files[i];
				if (file.isFile()) {
					String fileName = file.getName().substring(0,
							file.getName().lastIndexOf("."));
					File folder = new File(file.getParentFile()
							.getAbsoluteFile() + "/" + fileName);
					if (!folder.exists()) {
						folder.mkdir();
					}
					file.renameTo(new File(folder, file.getName()));
				}
			}

		}

	}

	public void checkSuffix() throws IOException {
		String movieParentFolder = (String) p.get("portable3");
		File movieFolder = new File(movieParentFolder);
		File[] files = movieFolder.listFiles();

		int fileCount = files.length;
		if (fileCount > 0) {
			for (int i = 0; i < fileCount; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					File[] innerFiles = file.listFiles();
					if (innerFiles.length == 1) {
						File theOnlyFile = innerFiles[0];
						if (file.getName().equalsIgnoreCase(
								theOnlyFile.getName())) {
							System.out.println(theOnlyFile.getName());
						}
					}
				}
			}
		}
	}

	public void renameMp4FileNameForIPod() {
		String expression = "S\\d{2}E\\d{2}";
		String prefix = "老爸老妈的浪漫史";
		String suffix = ".mp4";
		Pattern pattern = Pattern.compile(expression);
		
		String mp4FolderStr = (String) p.get("mp4Folder");

		File mp4Folder = new File(mp4FolderStr);

		File[] files = mp4Folder.listFiles();

		int fileCount = files.length;
		if (fileCount > 0) {
			for (int i = 0; i < fileCount; i++) {
				File file = files[i];
				if (!file.isDirectory()) {
					Matcher matcher = pattern.matcher(file.getName());
					if(matcher.find()){
						String modifiedFileName = prefix+"_"+file.getName().substring(matcher.start(), matcher.end())+suffix;
						file.renameTo(new File(mp4Folder+modifiedFileName));
					}
				}
			}
		}
	}
	
	
	public void testRE(){
		String fileName = "[YYeTs][How.I.Met.Your.Mother][S01E01][CN][DVDRip]V2";
		
		String expression = "S\\d{2}E\\d{2}";
		String prefix = "老爸老妈的浪漫史";
		
		Pattern pattern = Pattern.compile(expression);
		
		Matcher matcher = pattern.matcher(fileName);
		
		if(matcher.find()){
			String modifiedFileName = prefix+"_"+fileName.substring(matcher.start(), matcher.end());
			
			System.out.println("StartIndex:" + matcher.start());
			System.out.println("EndIndex:" + matcher.end());
			System.out.println("Matched String:" + fileName.substring(matcher.start(), matcher.end()));
			System.out.println(modifiedFileName);
		}
		
	}
}
