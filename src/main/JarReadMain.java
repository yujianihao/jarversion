package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


public class JarReadMain {
	
	public static String META_INF = "META-INF";
	
	public static String SOFA_VERSION = "sofa.version";
	
	public static String APP_VERSION = "app.version";
	
	public static String WEB_INF = "WEB-INF";
	
	public static String SOFA_CONTAINER = "sofa-container";
	
	public static String KERNEL = "kernel";
	
	public static String REPOSITORY = "repository";
	
	public static String JAR_SUFFIX = ".jar";

	private static Scanner sc;
	
	public static void main(String[] args) {
		String acsLine = "";
		File rootFile = null;
		System.out.println("请输入WEB-INF文件夹所在的全路径, 如果想终止 请输入 N ");
		sc = new Scanner(System.in);
		while(rootFile == null || !rootFile.exists()) {
			acsLine = sc.nextLine();
			if ("N".equals(acsLine)) {
				return ;
			}
			rootFile = new File(acsLine);
			if (!rootFile.exists()) {
				System.out.println("找不到输入路径下的文件，请重新输入, 如果想终止 请输入 N ");
			}
		}
		
		System.out.println("请输入需要搜索的jar信息，如果终止操作，请输入N");
		String keyword = sc.nextLine();
		keyword = StringUtils.isEmpty(keyword) ? "" : keyword.trim();
		while(!"N".equals(keyword)) {
			File[] listFiles = rootFile.listFiles();
			StringBuffer sofaSb = new StringBuffer();
			List<JarVersion> list = new ArrayList<>();
			for (File subFile : listFiles) {
				
				//读取sofa版本信息
				if (subFile.getName() != null && subFile.getName().contains(META_INF)) {
					sofaSb.append(readSofaVersion(subFile));
				}
				
				//读取jar包的信息
				if (subFile.getName() != null && subFile.getName().contains(WEB_INF)) {
					File[] listF = subFile.listFiles();
					for (File file : listF) {
						if (file != null && file.getName() != null && file.getName().equals(SOFA_CONTAINER)) {
							//目前只读取kernel 和repository文件夹下的内容
							File[] listFiles2 = file.listFiles();
							for (File file2 : listFiles2) {
								if (file2.exists() && 
										(file2.getName().equals(KERNEL) || file2.getName().equals(REPOSITORY)) && file2.isDirectory()) {
									readFileVersion(file2, keyword, list);
								}
							}
						}
					}
				}
			}
			
			readSofaHomeJarVersion(keyword, list);
			
			//list 排序，按照构建的时间 降序排列
			sofaSb.append(System.getProperty("line.separator"));
			if (list == null || list.isEmpty()) {
				sofaSb.append("找不到对应的jar信息");
			} else {
				Collections.sort(list, new Comparator<JarVersion>() {
					public int compare(JarVersion o1, JarVersion o2) {
						if (StringUtils.isEmpty(o1.getJarBuildTime())) {
							return 1;
						}
						if (StringUtils.isEmpty(o2.getJarBuildTime())) {
							return -1;
						}
						String str1 = o1.getJarBuildTime().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
						String str2 = o2.getJarBuildTime().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
						
						return str2.compareTo(str1);
					}
				});
				
				for (JarVersion jarVersion : list) {
					sofaSb.append(jarVersion.toString()).append(System.getProperty("line.separator"));
				}
			}
			
			System.out.println(sofaSb.toString());
			System.out.println();
			System.out.println("如需继续，请输入对应的jar信息，不想继续 请输入 N");
			keyword = sc.nextLine();
			keyword = StringUtils.isEmpty(keyword) ? "" : keyword.trim();
		}
		sc.close();

	}
	
	
	/**
	 * 传入搜索的jar 名称，获取对应的版本
	 * @param keyword 关键字信息
	 * @return 搜索出来的信息
	 */
	public static void readSofaHomeJarVersion(String keyword, List<JarVersion> list) {		
		//扫描sofa home下exbs文件夹
		String sofaHomePath = System.getenv("SOFACONFIG_HOME");
		File homeFile = new File(sofaHomePath);
		if (homeFile.exists()) {
			File[] listFiles = homeFile.listFiles();
			for (File file : listFiles) {
				if (file != null && file.getName().contains("exbs")) {
					readFileVersion(file, keyword, list);
				}
			}
		}
	}
	
	
	/**
	 * 获取执行文件下的jar 版本信息
	 * @param rootFile 文件夹
	 * @param keyword 关键字
	 * @param list 包含的版本信息
	 */
	public void getFile(File rootFile, String keyword, List<JarVersion> list) {
		File[] listFiles = rootFile.listFiles();
		for (File file : listFiles) {
			if (file.exists() && 
					(file.getName().equals(KERNEL) || file.getName().equals(REPOSITORY)) && file.isDirectory()) {
				readFileVersion(file, keyword, list);
			}
		}
	}
	
	/**
	 * 读取sofa 的版本信息
	 * @param file 读取file文件
	 * @return sofa的版本信息
	 */
	public static StringBuffer readSofaVersion(File file) {
		StringBuffer sb = new StringBuffer();
		File[] files = file.listFiles();
		for (File file2 : files) {
			if (file2.exists() && file2.getName().equals(SOFA_VERSION)) {
				try {
					
					sb.append("技术平台版本：");
					BufferedReader read = new BufferedReader(new FileReader(file2));
					String s = null;
					while ((s = read.readLine()) != null) {
						sb.append(" " + s);
					}
					sb.append(System.lineSeparator());
					if (read != null) {
						read.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (file2.exists() && file2.getName().equals(APP_VERSION)) {
				try {
					
					sb.append("应用系统版本：");
					BufferedReader read = new BufferedReader(new FileReader(file2));
					String s = null;
					while ((s = read.readLine()) != null) {
						sb.append(" " + s);
					}
					sb.append(System.lineSeparator());
					if (read != null) {
						read.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb;
	}
	
	/**
	 * 读文件，获取里面的版本和构建信息
	 * @param file 传入的文件信息
	 * @param keyword 输入的关键字
	 * @return 版本和构建信息
	 */
	public static void readFileVersion (File file, String keyword, List<JarVersion> list) {
		if (file == null || !file.exists()) {
			return;
		}
		
		//如果是单个的jar文件，直接读取
		if (file.getName() != null && file.getName().contains(JAR_SUFFIX) && file.getName().contains(keyword)) {
			readJar(file, keyword, list);
		} else if (file.isDirectory() && (!(file.getName().contains("lib") || file.getName().contains("3rd")))){
			//如果是文件夹，循环遍历，查找jar文件
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				readFileVersion(file2, keyword, list);
			}
		}
	}
	
	/**
	 * 传入需要读取的jar文件，将 Built-At、 Bundle-Version 的信息返回。
	 * @param file 传入jar文件信息
	 * @param keyword 输入的关键字
	 * @return 版本信息
	 */
	public static void readJar(File file, String keyword, List<JarVersion> list) {
		if (file.exists() && file.getName().contains(JAR_SUFFIX) && file.getName().contains(keyword)) {
			try {
				JarVersion jarVersion = new JarVersion();
				JarFile jarFile = new JarFile(file);
				Manifest manifest = jarFile.getManifest();
				Attributes attributes = manifest.getMainAttributes();
				String value = attributes.getValue("Built-At");
				jarVersion.setJarBuildTime(value);
				String value1 = attributes.getValue("Bundle-Version");
				jarVersion.setJarVersion(value1);
				jarVersion.setJarName(file.getName());
				list.add(jarVersion);
				if (jarFile != null) {
					jarFile.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
