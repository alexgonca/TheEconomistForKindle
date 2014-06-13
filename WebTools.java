/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package economist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 *
 * @author alex
 */
public class WebTools {
    public static String getPage(String address) {
        StringBuilder textDisp = new StringBuilder();
        try {    
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();	
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    textDisp.append(line).append("\n");
                }
            }
            if (textDisp.toString().indexOf("We are unable to process your request at this time") != -1) {
                System.out.println("Delay 5s...");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                textDisp = new StringBuilder();
                textDisp.append(getPage(address));
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return textDisp.toString();
    }
    
    public static String postContent(String address, String content) {
        StringBuilder textDisp = new StringBuilder();
        try {
            // Envia o request.
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();	
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes(content);
                out.flush();
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while((line=in.readLine())!=null) {
                    textDisp.append(line).append("\n");
                    //System.out.println(line);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return textDisp.toString();
    }
    
    public static void saveHTMLFile(String path, String title, String content) {
        content =
                "<html>\n" +
                "    <head>\n" +
                "        <title>"+title+"</title>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "        <link type=\"text/css\" href=\"economist.css\" rel=\"Stylesheet\"/>\n" +
                "    </head>\n" +
                "    <body>\n" + content +
                "    </body>\n" +
                "</html>";
        saveFile (path, content);
    }

    public static void saveImage(String imageUrl, String destinationFile){
        try {
            URL url = new URL(imageUrl);
            OutputStream os;

            try (InputStream is = url.openStream()) {
                os = new FileOutputStream(destinationFile);

                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                        os.write(b, 0, length);
                }
            }
            os.flush();
            os.close();   
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

  public static boolean deleteDirectory(File path) {
    if( path.exists() ) {
      File[] files = path.listFiles();
      for(int i=0; i<files.length; i++) {
         if(files[i].isDirectory()) {
           deleteDirectory(files[i]);
         }
         else {
           files[i].delete();
         }
      }
    }
    return( path.delete() );
  }    
    
    public static void makeDirs(String path){
        try {
            File f = new File(path);
            f.mkdirs();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }    
    
    public static void saveFile(String path, String content){
        try {
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(path),"UTF-8"))) {
                out.write(content);
                out.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
} 
