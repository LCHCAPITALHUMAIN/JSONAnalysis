/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

/**Class that includes several functions that perform JSON parsing using JSON simple parser
 * The functions perform the following operations:
 * 
 * 1)GoogleJSONParsing: 
 *   Input: receives the JSON response by Google Custom search API in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 2)YahooJSONParsing
 *   Input: receives the JSON response by Yahoo! Boss API in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 3)BingAzureJSONParsing
 *   Input: receives the JSON response by Bing search API in Azure in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 4)SindiceTripleParse
 *   Input: receives the JSON response by Sindice Live API v2
 *   Output: the number of semantic triples recognized in a url
 *            the namespaces that were used in the url
 * 5)DiffbotParsing
 *   Input: receives the JSON response by Diffbot Article API
 *   Output: the (semantic) tags included in the JSON response
 * 6)YahooEntityJSONParsing
 *   Input: receives the JSON response by Yahoo Content Analysis API
 *          It also receives the query for which the corresponding urls to the search engine results
 *          were provided as input to Yahoo Content Analysis API
 *   Output: the semantic entities and categories recognized
 *           the number of semantic entities and categories which include the query as one of their terms
 * 7)GetYoutubeDetails
 *   Input: receives the JSON response by YouTube Data API v3 in a string
 *   Output: the textual details (title, description) regarding the video searched
 * 8)removeChars
 *   Input: receives text 
 *   Output: removes useless characters from the text
 * @author Themis Mavridis
 */
 
public class JSONAnalysis {
    public static String[] links;
    public static String[] links_yahoo_bing;
    public static Map.Entry[] entries_yahoo_bing;
    public static int triple_cnt;
    public static int ent_query_cnt=0;
    public static int cat_query_cnt=0;
    
    JSONAnalysis(){links=new String[10];}
    JSONAnalysis(int results_number){links_yahoo_bing=new String[results_number];}

    public String[] GoogleJsonParsing(String input)  {
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create a map
            JSONObject json = (JSONObject) parser.parse(input);         
            //Get a set of the entries
            Set set = json.entrySet();
            //Create an iterator
            Iterator iterator = set.iterator();
            //Find the entry that contain the part of JSON that contains the link
            int i=0;
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("items")){
                    JSONArray jsonarray = (JSONArray) entry.getValue();
                    //find the key=link entry which contains the link
                    Iterator iterator_jsonarray= jsonarray.iterator();
                    while(iterator_jsonarray.hasNext()){
                        JSONObject next = (JSONObject) iterator_jsonarray.next();
                        links[i] = next.get("link").toString();
                        i++;
                    }
                }
            }
            return links;
        } catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            return links;
        }
    }
    public String[] YahooJsonParsing(String input,int yahoo_result_number){
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of yahoo jsonmap
            String you = entry.getValue().toString();
           json = (Map) parser.parse(you);
             set = json.entrySet();
             arr = set.toArray();
            entry = (Map.Entry) arr[1];
            //***get to third level of yahoo jsonmap
           you = entry.getValue().toString();
           
           json = (Map) parser.parse(you);
             set = json.entrySet();
             arr = set.toArray();
            entry = (Map.Entry) arr[0];
            you = entry.getValue().toString();
            JSONArray json_arr = (JSONArray) parser.parse(you);
            for (int j = 0; j < yahoo_result_number; j++) {
                Map json_new = (Map) json_arr.get(j);
                Set set_new = json_new.entrySet();
                Object[] arr_new = set_new.toArray();
                for (int k = 0; k < arr_new.length; k++) {
                    entries_yahoo_bing[k] = (Map.Entry) arr_new[k];
                }
                //find the entry that has label "link" in ordet to get the link
                for (int y = 0; y < arr_new.length; y++) {
                    if (entries_yahoo_bing[y].getKey().toString().equalsIgnoreCase("url")) {
                        links_yahoo_bing[j] = (String) entries_yahoo_bing[y].getValue().toString();
                    }
                }
                
            }
            return links_yahoo_bing;
        } catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            return links_yahoo_bing;
        }
}

public String[] BingAzureJsonParsing(String input,int bing_result_number) {
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            JSONObject jsonmap = (JSONObject) parser.parse(input);
            // Get a set of the entries
            Set set = jsonmap.entrySet();
            Iterator iterator=set.iterator();
            int i=0;
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("d")){
                    JSONObject jsonobject=(JSONObject) entry.getValue();
                    JSONArray jsonarray = (JSONArray) jsonobject.get("results");
                    Iterator jsonarrayiterator=jsonarray.listIterator();
                    while(jsonarrayiterator.hasNext()){
                        JSONObject linkobject= (JSONObject) jsonarrayiterator.next();
                        links_yahoo_bing[i]=linkobject.get("Url").toString();
                        i++;
                    }
                }
            }
            return links_yahoo_bing;
        }
        catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            return links_yahoo_bing;
        }
            
}
public boolean[] SindiceTripleParse(String input) {
        try {
            boolean[] namespaces=new boolean[39];
            if(input.length()>0){
                //Create a parser
                JSONParser parser = new JSONParser();
                //Create the map
                Map json = (Map) parser.parse(input);
                // Get a set of the entries
                Set set = json.entrySet();
                Object[] arr = set.toArray();
                int flagresults=0;
                int flagstatus=0;
                Map.Entry entry;
                for(int j=0;j<arr.length;j++){
                    entry = (Map.Entry) arr[j];
                    if(entry.getKey().toString().equalsIgnoreCase("extractorResults")){
                        flagresults=j;
                    }
                    if(entry.getKey().toString().equalsIgnoreCase("status")){
                        flagstatus=j;
                    }
                }
                Map.Entry entrystatus=(Map.Entry) arr[flagstatus];
                if(entrystatus.getValue().toString().equalsIgnoreCase("ok")){
                    entry=(Map.Entry) arr[flagresults];

                    String you = entry.getValue().toString();
                    json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("metadata")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    //****get to the third level of bing jsonmap
                    you = entry.getValue().toString();
                   json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("explicit")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    you = entry.getValue().toString();
                   json = (Map) parser.parse(you);
                    set = json.entrySet();
                    arr = set.toArray();

                    for(int j=0;j<arr.length;j++){
                        entry = (Map.Entry) arr[j];
                        if(entry.getKey().toString().equalsIgnoreCase("bindings")){
                            flagresults=j;
                        }
                    }
                    entry = (Map.Entry) arr[flagresults];
                    JSONArray entry_new=(JSONArray)entry.getValue();
                    for(int p=0;p<entry_new.size();p++){
                        json = (Map) entry_new.get(p);
                        set = json.entrySet();
                        arr = set.toArray();
                        for(int kj=0;kj<arr.length;kj++){
                            entry = (Map.Entry) arr[kj];
                            if(entry.getKey().toString().contains("p")){
                                JSONObject jo= (JSONObject) entry.getValue();
                                String next = jo.get("value").toString();
                                if(next.contains("http://purl.org/vocab/bio/0.1/")){
                                    namespaces[0]=true;
                                }

                                if(next.contains("http://purl.org/dc/elements/1.1/")){
                                    namespaces[1]=true;
                                }
                                if(next.contains("http://purl.org/coo/n")){
                                    namespaces[2]=true;
                                }
                                if(next.contains("http://web.resource.org/cc/")){
                                    namespaces[3]=true;
                                }
                                if(next.contains("http://diligentarguont.ontoware.org/2005/10/arguonto")){
                                    namespaces[4]=true;
                                }
                                if(next.contains("http://usefulinc.com/ns/doap")){
                                    namespaces[5]=true;
                                }
                                if(next.contains("http://xmlns.com/foaf/0.1/")){
                                    namespaces[6]=true;
                                }
                                if(next.contains("http://purl.org/goodrelations/")){
                                    namespaces[7]=true;
                                }
                                if(next.contains("http://purl.org/muto/core")){
                                    namespaces[8]=true;
                                }
                                if(next.contains("http://webns.net/mvcb/")){
                                    namespaces[9]=true;
                                }
                                if(next.contains("http://purl.org/ontology/mo/")){
                                    namespaces[10]=true;
                                }
                                if(next.contains("http://purl.org/innovation/ns")){
                                    namespaces[11]=true;
                                }
                                if(next.contains("http://openguid.net/rdf")){
                                    namespaces[12]=true;
                                }
                                if(next.contains("http://www.slamka.cz/ontologies/diagnostika.owl")){
                                    namespaces[13]=true;
                                }
                                if(next.contains("http://purl.org/ontology/po/")){
                                    namespaces[14]=true;
                                }
                                if(next.contains("http://purl.org/net/provenance/ns")){
                                    namespaces[15]=true;
                                }
                                if(next.contains("http://purl.org/rss/1.0/modules/syndication")){
                                    namespaces[16]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/ns")){
                                    namespaces[17]=true;
                                }
                                if(next.contains("http://madskills.com/public/xml/rss/module/trackback/")){
                                    namespaces[18]=true;
                                }
                                if(next.contains("http://rdfs.org/ns/void")){
                                    namespaces[19]=true;
                                }
                                if(next.contains("http://www.fzi.de/2008/wise/")){
                                    namespaces[20]=true;
                                }
                                if(next.contains("http://xmlns.com/wot/0.1")){
                                    namespaces[21]=true;
                                }
                                if(next.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns")){
                                    namespaces[22]=true;
                                }
                                if(next.contains("http://www.w3.org/")&next.contains("rdf-schema")){
                                    namespaces[23]=true;
                                }
                                if(next.contains("http://www.w3.org/")&next.contains("XMLSchema#")){
                                    namespaces[24]=true;
                                }
                                if(next.contains("http://www.w3.org")&&next.contains("owl")){
                                    namespaces[25]=true;
                                }
                                if(next.contains("http://purl.org/dc/terms/")){
                                    namespaces[26]=true;
                                }
                                if(next.contains("http://www.w3.org/")&&next.contains("vcard")){
                                    namespaces[27]=true;
                                }
                                if(next.contains("http://www.geonames.org/ontology")){
                                    namespaces[28]=true;
                                }
                                if(next.contains("http://search.yahoo.com/searchmonkey/commerce/")){
                                    namespaces[29]=true;
                                }
                                if(next.contains("http://search.yahoo.com/searchmonkey/media/")){
                                    namespaces[30]=true;
                                }
                                if(next.contains("http://cb.semsol.org/ns#")){
                                    namespaces[31]=true;
                                }
                                if(next.contains("http://blogs.yandex.ru/schema/foaf/")){
                                    namespaces[32]=true;
                                }
                                if(next.contains("http://www.w3.org/2003/01/geo/wgs84_pos#")){
                                    namespaces[33]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/ns#")){
                                    namespaces[34]=true;
                                }
                                if(next.contains("http://rdfs.org/sioc/types#")){
                                    namespaces[35]=true;
                                }
                                if(next.contains("http://smw.ontoware.org/2005/smw#")){
                                    namespaces[36]=true;
                                }
                                if(next.contains("http://purl.org/rss/1.0/")){
                                    namespaces[37]=true;
                                }
                                if(next.contains("http://www.w3.org/2004/12/q/contentlabel#")){
                                    namespaces[38]=true;
                                }
                            }
                        }
                    }
                }
            }
            return namespaces;
        }
        catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            boolean[] namespaces=new boolean[40];
            return namespaces;
        }
        catch (Exception x) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, x);
            boolean[] namespaces=new boolean[40];
            return namespaces;
        }

}
public String DiffbotParsing(String input){
        String output=""; 
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of  jsonmap to get the tags
            Object value = entry.getValue();
            String you = entry.getValue().toString();
            output=removeChars(you).toLowerCase();
            return output;
        }
          catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            output="fail";
             return output;
        }


}
public String[] YahooEntityJsonParsing(String input, String quer){
        try {
            ent_query_cnt=0;
            cat_query_cnt=0;
            String categories=new String();
            String entities=new String();
            categories="";
            entities="";
            String[] output=new String[2];
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of yahoo jsonmap
            String you = entry.getValue().toString();
            json = (Map) parser.parse(you);
            set = json.entrySet();
            arr = set.toArray();
            
            searchforresult:
            for (int kj=0;kj<arr.length;kj++){
                entry = (Map.Entry) arr[kj];
                if(entry.getKey().toString().contains("results")){ 
                    break searchforresult;
                }
            }
            
            //***get to third level of yahoo jsonmap
            //fix to search value = result
            if(entry.getValue()!=null){
                you = entry.getValue().toString();
                json = (Map) parser.parse(you);
                set = json.entrySet();
                arr = set.toArray();//here we have in arr[0] the categories related to the url and in arr[1] the entities related to
                //--we get the categories first
                for(int jk=0;jk<arr.length;jk++){
                    entry = (Map.Entry) arr[jk];
                    if(entry.getKey().toString().contains("yctCategories")){
                        you = entry.getValue().toString();
                        json = (Map) parser.parse(you);
                        set = json.entrySet();
                        Object[] arr_cat = set.toArray();
                        for (int ip=0;ip<arr_cat.length;ip++){
                            entry = (Map.Entry) arr_cat[ip];
                            if(entry.getKey().toString().contains("yctCategory")){
                                you = entry.getValue().toString();
                                if(you.startsWith("[")){
                                    JSONArray json_arr = (JSONArray) parser.parse(you);
                                    for(int ka=0;ka<json_arr.size();ka++){
                                            json = (Map) json_arr.get(ka);
                                            set = json.entrySet();
                                            arr_cat = set.toArray();
                                            for(int kj=0;kj<arr_cat.length;kj++){
                                                entry = (Map.Entry) arr_cat[kj];
                                                if(entry.getKey().toString().contains("content")){
                                                    categories=categories+"+"+entry.getValue().toString();

                                                }
                                            }
                                    }
                                }
                                if(you.startsWith("{")){
                                    json = (Map) parser.parse(you);
                                    set = json.entrySet();
                                    arr_cat = set.toArray();
                                    for(int ka=0;ka<arr_cat.length;ka++){
                                        entry = (Map.Entry) arr_cat[ka];
                                        if(entry.getKey().toString().contains("content")){
                                            categories=categories+"+"+entry.getValue().toString();
                                        }
                                    }
                                }     
                            }
                        }
                    }
                    //--we get the entities now
                    if(entry.getKey().toString().contains("entities")){
                        you = entry.getValue().toString();
                        json = (Map) parser.parse(you);
                        set = json.entrySet();
                        Object[] arr_ent = set.toArray();
                        for (int ip=0;ip<arr_ent.length;ip++){
                            entry = (Map.Entry) arr_ent[ip];
                            if(entry.getKey().toString().contains("entity")){
                                you = entry.getValue().toString();
                                if(you.startsWith("[")){
                                    JSONArray json_arr = (JSONArray) parser.parse(you);
                                    for(int ka=0;ka<json_arr.size();ka++){
                                        json = (Map) json_arr.get(ka);
                                        set = json.entrySet();
                                        arr_ent = set.toArray();
                                        for(int kj=0;kj<arr_ent.length;kj++){
                                            entry = (Map.Entry) arr_ent[kj];
                                            if(entry.getKey().toString().contains("text")){
                                                you = entry.getValue().toString();
                                                json = (Map) parser.parse(you);
                                                set = json.entrySet();
                                                arr_ent = set.toArray();
                                                for(int kai=0;kai<arr_ent.length;kai++){
                                                    entry = (Map.Entry) arr_ent[kai];
                                                    if(entry.getKey().toString().contains("content")){
                                                        entities = entities+"+"+entry.getValue().toString(); 
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if(you.startsWith("{")){
                                    json = (Map) parser.parse(you);
                                    set = json.entrySet();
                                    arr_ent = set.toArray();
                                    for(int ka=0;ka<arr_ent.length;ka++){
                                        entry = (Map.Entry) arr_ent[ka];
                                        if(entry.getKey().toString().contains("text")){
                                            you = entry.getValue().toString();
                                            json = (Map) parser.parse(you);
                                            set = json.entrySet();
                                            arr_ent = set.toArray();
                                            for(int kai=0;kai<arr_ent.length;kai++){
                                                entry = (Map.Entry) arr_ent[kai];
                                                if(entry.getKey().toString().contains("content")){
                                                    entities = entities+"+"+entry.getValue().toString(); 
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String queries[] = quer.split("\\+");
            for(int e=0;e<queries.length;e++){
                if(entities.toLowerCase().contains(queries[e])){
                    ent_query_cnt++;
                }
                if(categories.toLowerCase().contains(queries[e])){
                    cat_query_cnt++;
                }
            }
            entities=entities.substring(1);
            categories=categories.substring(1);
            output[0]=categories;
            output[1]=entities;
            return output;
        } catch (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            String[] output=new String[2];
            output[0]="fail";
            output[1]="fail";
            return output;
        }
}
public int GetEntQuerCnt(){
    return ent_query_cnt;
}
public int GetCatQuerCnt(){
    return cat_query_cnt;
}
public String GetYoutubeDetails(String line) {
        try {
            String output = "";
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(line);
            // Get a set of the entries
            Set set = json.entrySet();
            Iterator iterator=set.iterator();
            Map.Entry entry = null;
            boolean flagfound = false;
            while(iterator.hasNext()&&!flagfound){
                entry= (Map.Entry) iterator.next();
                if(entry.getKey().toString().equalsIgnoreCase("items")){
                    flagfound=true;
                }
            }
            JSONArray jsonarray=(JSONArray) entry.getValue();
            Iterator iteratorarray = jsonarray.iterator();
            flagfound=false;
            JSONObject get =null;
            while(iteratorarray.hasNext()&&!flagfound){
                JSONObject next = (JSONObject) iteratorarray.next();
                if(next.containsKey("snippet")){
                     get = (JSONObject) next.get("snippet");
                     flagfound=true;
                }
            }
            String description="";
            String title="";
            if(flagfound){
                if(get.containsKey("description")){
                    description=get.get("description").toString();
                }
                if(get.containsKey("title")){
                    title=get.get("title").toString();
                }
                    output = description + " " + title;
                }
            return output;
        } catch(ArrayIndexOutOfBoundsException ex){
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            String output=null;
            return output;
        
        } catch  (ParseException ex) {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            String output = null;
            return output;
        } catch(Exception ex)
        {
            Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            String output=null;
            return output;
        } 
    }
    public String removeChars(String str){
        if (str != null) {
            try {
                //str = str.replaceAll("(\r\n|\r|\n|\n\r)", " "); //Clear Paragraph escape sequences
                str = str.replaceAll("\\.", " "); //Clear dots
                str = str.replaceAll("\\-", " "); //
                str = str.replaceAll("\\_", " "); //
                str = str.replaceAll(":", " ");
                str = str.replaceAll("\\+", " ");
                str = str.replaceAll("\\/", " ");
                str = str.replaceAll("\\|", " ");
                str = str.replaceAll("\\[", " ");
                str = str.replaceAll("\\?", " ");
                str = str.replaceAll("\\#", " ");
                str = str.replaceAll("\\!", " ");
                str = str.replaceAll("'", " "); //Clear apostrophes
                str = str.replaceAll(",", " "); //Clear commas
                str = str.replaceAll("@", " "); //Clear @'s (optional)
                str = str.replaceAll("$", " "); //Clear $'s (optional)
                str = str.replaceAll("\\\\", "**&**"); //Clear special character backslash 4 \'s due to regexp format
                str = str.replaceAll("&amp;", "&"); //change &amp to &
                str = str.replaceAll("&lt;", "<"); //change &lt; to <
                str = str.replaceAll("&gt;", ">"); //change &gt; to >
                //		str = str.replaceAll("<[^<>]*>"," ");		//drop anything in <>
                str = str.replaceAll("&#\\d+;", " "); //change &#[digits]; to space
                str = str.replaceAll("&quot;", " "); //change &quot; to space
                //		str = str.replaceAll("http://[^ ]+ "," ");	//drop urls
                str = str.replaceAll("-", " "); //drop non-alphanumeric characters
                str = str.replaceAll("[^0-9a-zA-Z ]", " "); //drop non-alphanumeric characters
                str = str.replaceAll("&middot;", " ");
                str = str.replaceAll("\\>", " ");
                str = str.replaceAll("\\<", " ");
                str = str.replaceAll("<[^>]*>", "");
                str = str.replaceAll("\\d"," ");
                //str=str.replaceAll("\\<.*?\\>", "");
                str = str.replace('β', ' ');
                str = str.replace('€', ' ');
                str = str.replace('™', ' ');
                str = str.replace(')', ' ');
                str = str.replace('(', ' ');
                str = str.replace('[', ' ');
                str = str.replace(']', ' ');
                str = str.replace('`', ' ');
                str = str.replace('~', ' ');
                str = str.replace('!', ' ');
                str = str.replace('#', ' ');
                str = str.replace('%', ' ');
                str = str.replace('^', ' ');
                str = str.replace('*', ' ');
                str = str.replace('&', ' ');
                str = str.replace('_', ' ');
                str = str.replace('=', ' ');
                str = str.replace('+', ' ');
                str = str.replace('|', ' ');
                str = str.replace('\\', ' ');
                str = str.replace('{', ' ');
                str = str.replace('}', ' ');
                str = str.replace(',', ' ');
                str = str.replace('.', ' ');
                str = str.replace('/', ' ');
                str = str.replace('?', ' ');
                str = str.replace('"', ' ');
                str = str.replace(':', ' ');
                str = str.replace('>', ' ');
                str = str.replace(';', ' ');
                str = str.replace('<', ' ');
                str = str.replace('$', ' ');
                str = str.replace('-', ' ');
                str = str.replace('@', ' ');
                str = str.replace('©', ' ');
                //remove space
                InputStreamReader in = new InputStreamReader(IOUtils.toInputStream(str));
                BufferedReader br = new BufferedReader(in);
                Pattern p;
                Matcher m;
                String afterReplace = "";
                String strLine;
                String inputText = "";
                while ((strLine = br.readLine()) != null) {
                    inputText = strLine;
                    p = Pattern.compile("\\s+");
                    m = p.matcher(inputText);
                    afterReplace = afterReplace + m.replaceAll(" ");
                }
                br.close();
                str = afterReplace;
                return str;
            } catch (IOException ex) {
                Logger.getLogger(JSONAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                str=null;
                return str;
            }
        } else {
            return str;
        }
    }
}




