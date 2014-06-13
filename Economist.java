/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package economist;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

/**
 *
 * @author alex
 */
public class Economist {

    private static final String LOGIN_PAGE = "https://www.economist.com/user/login";
    private static final String INDEX_PAGE = "http://www.economist.com/printedition/";
    private static final String LOGOUT_PAGE = "http://www.economist.com/logout";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cm);
        
        System.out.println("Process started.");
        autentica();
        System.out.println("Authenticated.");
        processa();
        logout();
    }
    
    
    public static void processa() {
        String page = WebTools.getPage(INDEX_PAGE);
        System.out.println ("Table of contents obtained.");
                
        int inicio = page.indexOf("<div id=\"section", page.indexOf("<div class=\"view-content\">")),
                fim = page.lastIndexOf("<div ", page.indexOf("<h4>Economic and financial indicators"));
        
        String content = page.substring(inicio, fim);
        
        inicio = page.indexOf("<link rel=\"canonical\" href=\"") + "<link rel=\"canonical\" href=\"".length();
        fim = page.indexOf("\"", inicio);
        inicio = page.lastIndexOf("/", fim) + 1;
        
        String timeStamp = page.substring(inicio, fim);
        
        while (content.indexOf("class=\"comment-icon\"") != -1) {
            inicio = content.lastIndexOf("<a href=", content.indexOf("class=\"comment-icon\""));
            fim = content.indexOf("</a>", inicio) + "</a>".length();
            content = content.substring(0, inicio) + content.substring(fim, content.length());
        }
        
        while (content.indexOf("<div class=\"uk-flag\">") != -1) {
            inicio = content.indexOf("<div class=\"uk-flag\">");
            fim = content.indexOf("</div>", inicio) + "</div>".length();
            content = content.substring(0, inicio) + content.substring(fim, content.length());
        }
        
        while (content.indexOf("<div class=\"article\">") != -1) {
            inicio = content.indexOf("</div>", content.indexOf("<div class=\"article\">"));
            fim = inicio + "</div>".length();
            content = content.substring(0, inicio) + "ZZZWWWKKK" +
                    content.substring(fim, content.length());
            
            inicio = content.indexOf("<div class=\"article\">");
            fim = inicio + "<div class=\"article\">".length();
            content = content.substring(0, inicio) + "WWWKKKZZZ" +
                    content.substring(fim, content.length());
        }
        
        while (content.indexOf("<div ") != -1) {
            inicio = content.indexOf("<div ");
            fim = content.indexOf(">", inicio);
            content = content.substring(0, inicio) + 
                    content.substring(fim + 1, content.length());
        }
       
        content = content.replace("</div>", "");
        content = content.replace("ZZZWWWKKK", "</div>");
        content = content.replace("WWWKKKZZZ", "<div class=\"article\">");
        content = content.replace("<h5>", "<div class=\"subsection\">");
        content = content.replace("<h4>", "<div class=\"section\">");
        content = content.replace("</h5>", "</div>");
        content = content.replace("</h4>", "</div>");
        
        content = "<div class=\"title\">The Economist - "+ timeStamp+"</div>\n" + content; 
        WebTools.makeDirs(timeStamp);
        
        System.out.println("Obtaining cover...");
        inicio = page.indexOf("<a href=\"", page.indexOf("<div class=\"issue-image\">")) + "<a href=\"".length();
        String linkCover = "http://www.economist.com" + page.substring(inicio,
                            page.indexOf("\"", inicio));
        String coverPage = WebTools.getPage(linkCover);
        inicio = coverPage.indexOf("<img src=\"", coverPage.indexOf("<div class=\"cover-content\">")) + "<img src=\"".length();
        linkCover = coverPage.substring(inicio,
                            coverPage.indexOf("\"", inicio));
        //WebTools.saveImage(linkCover, timeStamp + "/cover.jpg");
        
        BufferedImage img;
        try {
            img = ImageIO.read(new URL(linkCover));
            Image newImg = img.getScaledInstance(761, 1001, Image.SCALE_SMOOTH);
            BufferedImage bi = new BufferedImage (newImg.getWidth(null),
                    newImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics bg = bi.getGraphics();
            bg.drawImage(newImg, 0, 0, null);
            bg.dispose();
            
            ImageIO.write(bi, "jpg", new File(timeStamp + "/cover.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(Economist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Saving CSS...");
        WebTools.saveFile(timeStamp + "/economist.css", returnCSS());
        
        int position = 0;
        String article,
                linkArticle,
                codeArticle,
                nameArticle;
        int inicioCode;
        ArrayList<Article> articleList = new ArrayList();  
        
        while (content.indexOf("<a href=\"", position) != -1) {
                inicio = content.indexOf("<a href=\"", position) + "<a href=\"".length();
                fim = content.indexOf("</a>", inicio);
                
                linkArticle = "http://www.economist.com" +
                        content.substring(inicio,
                        content.indexOf("\"", inicio));
                inicioCode = content.lastIndexOf("/", content.indexOf("\"", inicio));
                codeArticle = content.substring(
                        content.lastIndexOf("/", content.indexOf("\"", inicio)),
                        content.indexOf("-", inicioCode)) + ".html";
                nameArticle = content.substring(
                        content.lastIndexOf(">", fim)+1,
                        fim).trim();
                
                if ("KAL's cartoon".equals(nameArticle)) {
                    String kalsCartoon = WebTools.getPage(linkArticle);
                    int inicioCartoon = kalsCartoon.indexOf("<img src=\"",
                            kalsCartoon.indexOf("<div class=\"content-image-full\">")) + "<img src=\"".length();
                    int fimCartoon = kalsCartoon.indexOf("\"", inicioCartoon);
                    kalsCartoon = kalsCartoon.substring(inicioCartoon, fimCartoon);
                    linkArticle = kalsCartoon;
                    codeArticle = "/kalscartoon.html";
                }
                
                articleList.add(new Article(linkArticle, nameArticle, codeArticle));
                
                fim = content.indexOf("\"", inicio);
                content = content.substring(0,
                                inicio) +
                        codeArticle.replace("/", "") +
                        content.substring(fim,
                                    content.length());
                position = content.indexOf("</a>", inicio);
        }

        WebTools.saveHTMLFile(timeStamp + "/toc.html", "Table of Contents - " + timeStamp, content);        
        
        for (int i = 0; i < articleList.size(); i++) {
            System.out.println("Obtaining "+articleList.get(i).getLink());
            
            if (!"/kalscartoon.html".equals(articleList.get(i).getCode())) {
                article = WebTools.getPage(articleList.get(i).getLink());
                article = processArticle(article);
                WebTools.saveHTMLFile(timeStamp + articleList.get(i).getCode(), articleList.get(i).getTitle(), article);
            }
            else {
                BufferedImage img2;
                try {
                    img2 = ImageIO.read(new URL(articleList.get(i).getLink()));
                    int h = img2.getHeight();
                    int w = img2.getWidth();
                    AffineTransform at = new AffineTransform();
                    at.translate(0.5*h, 0.5*w);
                    at.rotate(-Math.PI / 2);
                    at.translate(-0.5*w, -0.5*h);
                    BufferedImage bi = new BufferedImage (h, w, BufferedImage.TYPE_INT_RGB);  
                    Graphics2D bg = (Graphics2D) bi.createGraphics();
                    bg.drawImage(img2, at, null);
                    bg.dispose();
                    ImageIO.write(bi, "jpg", new File(timeStamp + "/kalscartoon.jpg"));
                    WebTools.saveHTMLFile(timeStamp + articleList.get(i).getCode(), articleList.get(i).getTitle(),
                            "<img src=\"kalscartoon.jpg\"/>\n");
                } catch (IOException ex) {
                    Logger.getLogger(Economist.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
        
        try {        
            System.out.println("Generating EPUB.");
            Book book = new Book();
            book.getMetadata().addTitle("The Economist - " + timeStamp);
            book.getMetadata().addAuthor(new Author("The Economist", "Publisher"));
            book.setCoverImage(new Resource(new FileInputStream (new File ( timeStamp + "/cover.jpg")), "cover.jpg"));            
            book.getResources().add(new Resource(new FileInputStream (new File ( timeStamp + "/" + "economist.css")), "economist.css"));
            book.addSection("Table of Contents", new Resource(new FileInputStream (new File (timeStamp + "/toc.html")), "toc.html"));
            book.getGuide().addReference(new GuideReference(new Resource(new FileInputStream (new File (timeStamp + "/toc.html")), "toc.html"), GuideReference.TOC, "toc"));
            for (int i = 0; i < articleList.size(); i++) {
                book.addSection(articleList.get(i).getTitle(), new Resource(new FileInputStream (new File (timeStamp + articleList.get(i).getCode())), articleList.get(i).getCode().replace("/", "")));
                if ("/kalscartoon.html".equals(articleList.get(i).getCode())) {
                    book.getResources().add(new Resource(new FileInputStream (new File ( timeStamp + "/" + "kalscartoon.jpg")), "kalscartoon.jpg"));                    
                }
            }
            
            WebTools.makeDirs("./mobi");
            
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(book, new FileOutputStream("./mobi/economist-" + timeStamp + ".epub"));

            book = null;
            epubWriter = null;
            System.gc();
            System.out.println("Generating Kindle file.");
            try {
                Thread.sleep(2000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Runtime.getRuntime().exec("kindlegen ./mobi/economist-" + timeStamp + ".epub");
            //pr.waitFor();
            
            System.out.println("Deleting folder.");
            if (!WebTools.deleteDirectory(new File(timeStamp))) {
               System.out.println("The folder was not deleted. Do it manually.");
            }
            System.out.println("Economist " + timeStamp + " finished!");                        
        }
        catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    
    
    private static String processArticle (String article) {
        article = article.substring(
                //article.indexOf("<div id=\"ec-article-body"),
                //article.indexOf("<!-- /#ec-article-body -->"));
                article.indexOf("<article>"),
                article.indexOf("</article>"));        
        if ( article.contains("<div class=\"related-items\">") ){
            article = article.substring(0,
                article.indexOf("<div class=\"related-items\">")) +
                article.substring(article.lastIndexOf("</div>",
                            article.indexOf("<p", article.indexOf("<div class=\"related-items\">")))+
                            "</div>".length(),
                    article.length());
        }
        
        if ( article.contains("<a href=\"http://twitter.com/share\"") ){
            article = article.substring(0,
                article.indexOf("<a href=\"http://twitter.com/share\"")) +
                article.substring(article.indexOf("</a>",
                            article.indexOf("<a href=\"http://twitter.com/share\""))+
                            "</a>".length(),
                    article.length());
        }

        while (article.contains("<span class='caption'>")) {
            article = article.substring(0,
                            article.indexOf("<span class='caption'>")) +
                    article.substring(
                            article.indexOf("</span>", article.indexOf("<span class='caption'>")) + "</span>".length(),
                            article.length());
        }        
        
        while (article.contains("<p class=\"ec-article-info\">")) {
            article = article.substring(0,
                            article.indexOf("<p class=\"ec-article-info\">")) +
                    article.substring(
                            article.indexOf("</p>", article.indexOf("<p class=\"ec-article-info\">")) + "</p>".length(),
                            article.length());
        }
        
        while (article.contains("<aside ")) {
            article = article.substring(0,
                            article.indexOf("<aside ")) +
                    article.substring(
                            article.indexOf("</aside>", article.indexOf("<aside ")) + "</aside>".length(),
                            article.length());
        }        
        
        while (article.contains("<h1 ")) {
            article = article.substring(0,
                            article.indexOf("<h1 ")) + "VVVJJJEEE" +
                    article.substring(
                            article.indexOf(">", article.indexOf("<h1 "))+ 1,
                            article.length());
        }
        article = article.replace("</h1>", "LLLKKKWWW");

        while (article.contains("<h2 ")) {
            article = article.substring(0,
                            article.indexOf("<h2 ")) + "VVVJJJEEE" +
                    article.substring(
                            article.indexOf(">", article.indexOf("<h2 "))+ 1,
                            article.length());
        }
        article = article.replace("</h2>", "LLLKKKWWW");        

        while (article.contains("<h3 ")) {
            article = article.substring(0,
                            article.indexOf("<h3 ")) + "VVVJJJEEE" +
                    article.substring(
                            article.indexOf(">", article.indexOf("<h3 "))+ 1,
                            article.length());
        }
        article = article.replace("</h3>", "LLLKKKWWW");        
        
        article = article.replace("<blockquote>", "MMMZZZWWW").replace("</blockquote>", "UUUKKKEEE");
        article = article.replace("<p>", "ZZZWWWKKK").replace("</p>", "KKKZZZWWW");
        article = article.replace("<strong>", "DDDMMMGGG").replace("</strong>", "PPPRRRGGG");
        article = article.replace("<em class=\"Bold\">", "BBBYYYUUU").replace("</em>", "IIIWWWKKK");
        
        while (article.contains("<")) {
            article = article.substring(0,
                            article.indexOf("<")) +
                    article.substring(
                            article.indexOf(">", article.indexOf("<")) + 1,
                            article.length());
        }
        
        article = article.replace("MMMZZZWWW","<i>").replace("UUUKKKEEE", "</i>");
        article = article.replace("ZZZWWWKKK", "<p class=\"text\">").replace("KKKZZZWWW", "</p>");
        article = article.replace("DDDMMMGGG", "<strong>").replace("PPPRRRGGG", "</strong>");
        article = article.replace("VVVJJJEEE", "<div class=\"section\">").replace("LLLKKKWWW", "</div>");
        article = article.replace("BBBYYYUUU", "<strong>").replace("IIIWWWKKK", "</strong>");
        
        return article;
    }
    
    public static void logout () {
        String result = WebTools.getPage(LOGOUT_PAGE);
        //WebTools.saveFile("tmp/logout.html", result);
    }
    
    public static void autentica () {
        String page = WebTools.getPage(LOGIN_PAGE);
        
        String loginForm = page.substring(
                page.indexOf("<form action=\"https://www.economist.com/user/login"),
                page.indexOf("</form>", page.indexOf("<form action=\"https://www.economist.com/user/login")));
        
        Map<String, String> data = new HashMap<>();
        
        Properties prop = new Properties();
	InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            data.put("name", prop.getProperty("username"));
            data.put("pass", prop.getProperty("password"));
	} catch (IOException ex) {
            System.out.println("Error when opening properties file.");
	} finally {
            if (input != null) {
		try {
                    input.close();
		} catch (IOException e) {
                    System.out.println("Error when closing properties file.");
		}
            }
	}        
        

        
        String value;
        int inicio, fim;
        
        inicio = loginForm.indexOf("value=\"", loginForm.indexOf("name=\"persistent_login\"")) + "value=\"".length();
        fim = loginForm.indexOf("\"", inicio);
        value = loginForm.substring(inicio, fim);
        data.put("persistent_login", value);

        inicio = loginForm.indexOf("value=\"", loginForm.indexOf("name=\"form_build_id\"")) + "value=\"".length();
        fim = loginForm.indexOf("\"", inicio);
        value = loginForm.substring(inicio, fim);
        data.put("form_build_id", value);
        
        inicio = loginForm.indexOf("value=\"", loginForm.indexOf("name=\"form_id\"")) + "value=\"".length();
        fim = loginForm.indexOf("\"", inicio);
        value = loginForm.substring(inicio, fim);
        data.put("form_id", value);

        inicio = loginForm.indexOf("value=\"", loginForm.indexOf("name=\"securelogin_original_baseurl\"")) + "value=\"".length();
        fim = loginForm.indexOf("\"", inicio);
        value = loginForm.substring(inicio, fim);
        data.put("securelogin_original_baseurl", value);
       
        Set keys = data.keySet();
        Iterator keyIter = keys.iterator();

        String content = "";
        for(int j = 0; keyIter.hasNext(); j++) {
            Object key = keyIter.next();
            if (j != 0) {
                content += "&";
            }
            try {
                content += key + "=" + URLEncoder.encode(data.get((String)key), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Economist.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        WebTools.postContent(LOGIN_PAGE, content);
    }
    
    private static String returnCSS(){
        String CSS = ".section {\n" +
                "    font-weight:bold;\n" +
                "    margin-bottom: 1em;\n" +
                "    text-align: left;\n" +
                "}\n" +
                "\n" +
                ".subsection {\n" +
                "    font-style:italic;\n" +
                "    text-align: left;\n" +
                "}\n" +
                "\n" +
                ".title {\n" +
                "    font-weight: bold;\n" +                
                "    text-align: left;\n" +
                "}\n" +
                "\n" +
                ".article {\n" +
                "    text-align: left;\n" +
                "    margin-left: 1em;\n" +
                "}\n"+
                "\n" +
                ".text {\n" +
                "    text-indent: 5%;\n" +
                "    margin-top: 0;\n" +
                "    margin-bottom: 0;\n" +
                "}";
        return CSS;
    }    
}