/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package economist;

/**
 *
 * @author alex
 */
public class Article {
    private String link;
    private String title;
    private String code;
    
    public Article() {
        link = "";
        title = "";
        code = "";
    }
    
    public Article (String linkAux, String titleAux, String codeAux) {
        link = linkAux;
        title = titleAux;
        code = codeAux;
    }
    
    public void setLink (String linkAux) {
        link = linkAux;
    }
    
    public String getLink () {
        return link;
    }
    
    public void setCode (String codeAux) {
        code = codeAux;
    }
    
    public String getCode () {
        return code;
    }
    
    public void setTitle (String titleAux) {
        title = titleAux;
    }
    
    public String getTitle () {
        return title;
    }    
}
