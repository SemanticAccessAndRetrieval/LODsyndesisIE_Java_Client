
/*  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2020 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.lodsyndesis.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Mountantonakis
 *
 * This Class is used to send requests to LODsyndesisIE rest API. 
 */
public class LODsyndesisIERestClient {

    private HttpClient client;
    private HttpGet getEntitiesOfText;
    private HttpGet exportAsRDFa;
    private HttpGet getTriplesOfEntities;
    private HttpGet findRelatedFacts;
    private HttpGet textEntitiesDatasetDiscovery;
    private static final String URL ="https://demos.isl.ics.forth.gr/LODsyndesisIE/rest-api";

    private String serviceName;

    private DecimalFormat df = new DecimalFormat(".##");

    /**
     * Used to open connection with client and LODsyndesis
     */
    public LODsyndesisIERestClient() {
        client = HttpClientBuilder.create().build();
        df.setRoundingMode(RoundingMode.DOWN);

    }

    /**
     * Retrieve the entities of a given text
     *
     * @param text the input text
     * @param ERTools the combination of entity extraction tools. Options [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP, WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
     * @param equivalentURIs true for retrieving the equivalent URIs for each entity, fakse otherwise
     * @param provenance  true for retrieving the equivalent URIs for each entity, fakse otherwise
     * @return the entities of a text, and possibly their equivalent URIs and provenance
     */
    public String getEntitiesOfText(String text,String ERTools,String equivalentURIs,String provenance) throws IOException {
        try {
            serviceName = "getEntities";
            getEntitiesOfText = new HttpGet(URL + "/" + serviceName + "?text=" + text+"&ERtools="+ERTools+"&equivalentURIs="+equivalentURIs+"&provenance="+provenance);
            
            getEntitiesOfText.addHeader(ACCEPT, "text/tsv");
            getEntitiesOfText.addHeader(CONTENT_TYPE, "text/tsv");
           // System.out.println(getEntitiesOfText);
            ArrayList<ArrayList<String>> result = getContent(getEntitiesOfText);
            String output="";
              for (ArrayList<String> triple : result) {
                for(int i=0;i<triple.size();i++){
                    output+=triple.get(i);
                    if(i+1==triple.size()){
                        output+="\n";
                    }
                    else if(triple.get(i).startsWith("http")){
                        output+="\t";
                    }
                    else
                        output+=" ";
                }}
              output=output.replace(" http","\thttp:");
            return output;
        } catch (Exception ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void printEntities(String output){
        System.out.println("\nThe entities of the given text and their data\n");
        String[] split=output.split("\n");
        for(int i=1;i<split.length;i++){
            String[] line=split[i].split("\t");
            System.out.println("Recognized Entity: "+ line[0]);
            System.out.println("DBpedia URI of "+ line[0]+": "+line[1]);
            System.out.println("LODsyndesis URI of "+ line[0]+": "+line[2]);
            if(line.length>3){
                if(split[0].contains("Equivalent"))
                    System.out.println("Equivalent URIs of "+ line[0]+": "+line[3]);
                else
                    System.out.println("Provenance of "+ line[0]+": "+line[3]);
            }
            if(line.length>4)
                System.out.println("Provenance of "+ line[0]+": "+line[4]);
            System.out.println("");
        }
        System.out.println("");
    }

    /**
     * Retrieve the given text annotated in HTML+RDFa Format 
     *
     * @param text the input text
     * @param ERTools the combination of entity extraction tools. Options [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP, WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
     * @return the given text annotated in HTML+RDFa Format
     */
    public String exportAsRDFa(String text,String ERTools) throws IOException {
        try {
            serviceName = "exportAsRDFa";
            exportAsRDFa = new HttpGet(URL + "/" + serviceName + "?text=" + text+"&ERtools="+ERTools);
            exportAsRDFa.addHeader(ACCEPT, "text/html");
            exportAsRDFa.addHeader(CONTENT_TYPE, "text/html");
            
            ArrayList<ArrayList<String>> result = getContent(exportAsRDFa);
            String output="";
              for (ArrayList<String> triple : result) {
                for(int i=0;i<triple.size();i++){
                    output+=triple.get(i);
                    if(i+1==triple.size()){
                        output+="\n";
                    }
                    else{
                        output+=" ";
                    }
                }}
            return output;
        } catch (Exception ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Retrieve the triples of each of the recognized entities
     *
     * @param text the input text
     * @param ERTools the combination of entity extraction tools. Options [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP, WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
     * @return the triples of each of the recognized entities
     */
    public String getTriplesOfEntities(String text,String ERTools) throws IOException {
        try {
            serviceName = "getTriplesOfEntities";
            getTriplesOfEntities = new HttpGet(URL + "/" + serviceName + "?text=" + text+"&ERtools="+ERTools);
            getTriplesOfEntities.addHeader(ACCEPT, "application/n-quads");
            getTriplesOfEntities.addHeader(CONTENT_TYPE, "application/n-quads");
            
            ArrayList<ArrayList<String>> result = getContent(getTriplesOfEntities);
            String output="";
              for (ArrayList<String> triple : result) {
                for(int i=0;i<triple.size();i++){
                    output+=triple.get(i);
                    if(i+1==triple.size()){
                        output+="\n";
                    }
                    else{
                        output+=" ";
                    }
                }}
            return output;
        } catch (Exception ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
  /**
     * Retrieve the related facts of the given text
     *
     * @param text the input text
     * @param ERTools the combination of entity extraction tools. Options [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP, WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
     * @return the related facts of the given text
     */
    public String findRelatedFacts(String text,String ERTools) throws IOException {
        try {
            serviceName = "findRelatedFacts";
            findRelatedFacts = new HttpGet(URL + "/" + serviceName + "?text=" + text+"&ERtools="+ERTools);
            findRelatedFacts.addHeader(ACCEPT, "application/n-triples");
            findRelatedFacts.addHeader(CONTENT_TYPE, "application/n-triples");
            
            ArrayList<ArrayList<String>> result = getContent(findRelatedFacts);
            String output="";
              for (ArrayList<String> triple : result) {
                for(int i=0;i<triple.size();i++){
                    output+=triple.get(i);
                    if(i+1==triple.size()){
                        output+="\n";
                    }
                    else{
                        output+=" ";
                    }
                }}
            return output;
        } catch (Exception ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

     /**
     * ecognizes all the entities of a given text by using any combination of three recognition tools and LODsyndesis.
      For the recognized entities, it returns the top-K subset of datasets a) whose union contains the most triples for these entities (coverage), or b) that contains the most common triples for these entities (commonalities).
     *
     * @param text the input text
     * @param ERTools the combination of entity extraction tools. Options [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP, WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
     * @param subsetSize It can be any integere of the following: [1,2,3,4,5]. The value "2", returns the most connected pairs of datasets, "3" the most connected triads of datasets, and so on
     * @param topK It can be any integer greater than 0, i.e., for showing the top-k connected datasets
     * @param type It can be any of the following: [coverage,commonalities]
     * @return the related facts of the given text
     */
    public String textEntitiesDatasetDiscovery(String text,String ERTools, int subsetSize,int topK,String type) throws IOException {
        try {
            serviceName = "textEntitiesDatasetDiscovery";
            textEntitiesDatasetDiscovery = new HttpGet(URL + "/" + serviceName + "?text=" + text+"&ERtools="+ERTools+"&subsetSize="+subsetSize+"&topK="+topK+"&measurementType="+type);
            textEntitiesDatasetDiscovery.addHeader(ACCEPT, "text/csv");
            textEntitiesDatasetDiscovery.addHeader(CONTENT_TYPE, "text/csv");
            
            ArrayList<ArrayList<String>> result = getContent(textEntitiesDatasetDiscovery);
            String output="";
              for (ArrayList<String> triple : result) {
                for(int i=0;i<triple.size();i++){
                    output+=triple.get(i);
                    if(i+1==triple.size()){
                        output+="\n";
                    }
                    else{
                        output+=" ";
                    }
                }}
            return output;
        } catch (Exception ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
   
 

    /**
     * Used to execute the request, receive the response in JSON format and
     * produce an interpretable structure with it.
     *
     * @param request
     * @return An interpretable structure that contains current service
     * response.
     * @throws IOException
     */
    private ArrayList<String> getJsonContent(HttpGet request) throws IOException {

        try {
            HttpResponse response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            ArrayList<String> result = new ArrayList<>();
            String line = rd.readLine();
            // If there is an error, return an empty arrayList
            if (line.startsWith("<!DOCTYPE")) {
                Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.WARNING, line);
                return new ArrayList<>();
            }

            JSONObject jsonObject = new JSONObject("{candidates: " + line + "}");
            JSONArray candidates = jsonObject.getJSONArray(("candidates"));

            for (int i = 0; i < candidates.length(); i++) {
                JSONObject uri = candidates.getJSONObject(i);
                result.add(uri.getString("uri"));
            }

            return result;
        } catch (JSONException ex) {
            Logger.getLogger(LODsyndesisIERestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to transform a fact (predicate into URL valid substring). i.e.
     * replaces white spaces with %20.
     *
     * @param fact
     * @return return fact as a valid URL substring.
     */
    private String getURLEncodedFact(String fact) {
        String URLEncodedFact = "";
        String[] factSplited = fact.split(" ");
        int cnt = 0;
        for (String subFact : factSplited) {
            cnt++;
            if (cnt == factSplited.length) {
                URLEncodedFact += subFact;
            } else {
                URLEncodedFact += subFact + "%20";
            }
        }
        return URLEncodedFact;
    }

    /**
     * Used to execute the request, receive the response in n-quads or n-triples
     * format and produce an interpretable structure with it.
     *
     * @param request
     * @return An interpretable structure that contains current service
     * response.
     * @throws IOException
     */
    private ArrayList<ArrayList<String>> getContent(HttpGet request) throws IOException {

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        String line = "";

        while ((line = rd.readLine()) != null) {
            String[] lineSplited = line.split("\\s+");
            ArrayList<String> lineSplitedClean = new ArrayList<>();
            for (String lineUnit : lineSplited) {
                if (lineUnit.equals(".")) {
                    continue;
                } else {
                    lineSplitedClean.add(lineUnit);
                }
            }
            result.add(lineSplitedClean);
        }
        return result;
    }

   

    
    public static void main(String[] args) throws IOException {
        LODsyndesisIERestClient chanel = new LODsyndesisIERestClient();

        //ERTools [WAT, StanfordCoreNLP, DBpediaSpotlight, WAT_and_StanfordCoreNLP,
        //WAT_and_DBpediaSpotlight, StanfordCoreNLP_and_DBpediaSpotlight, All]
        
        String  text="Nikos Kazantzakis was born in Heraklion, Crete. Widely considered a giant of modern Greek "
                + "literature, he was nominated for the Nobel Prize in Literature in nine different"
                + " years. Kazantzakis' novels included Zorba the Greek "
                + "(published 1946 as Life and Times of Alexis Zorbas), Christ Recrucified (1948), "
                + "Captain Michalis (1950, translated Freedom and Death), and The "
                + "Last Temptation of Christ (1955). His fame spread in the "
                + "English-speaking world due to cinematic adaptations of Zorba the Greek (1964) "
                + "and The Last Temptation of Christ (1988). He translated also"
                + " a number of notable works into Modern Greek, such as the Divine Comedy,"
                + " Thus Spoke Zarathustra and the Iliad.  Late in 1957, even though suffering "
                + "from leukemia, he set out on one last trip to China and Japan. "
                + "Falling ill on his return flight, he was transferred to Freiburg, Germany, where he died.";
        text=text.replace(" ","%20");
        
        // Find the entities of the givenn text, their equivalent URIs and their provenance by using WAT
        String ERTools="WAT";
        String output1=chanel.getEntitiesOfText(text,ERTools,"true","true");
        chanel.printEntities(output1);
        
        //export the given text annotated by using WAT and DBpedia Spotlight
        ERTools="WAT_and_DBpediaSpotlight";
        String output2=chanel.exportAsRDFa(text,ERTools);
        System.out.println("\nThe annotated text in HTML+RDFa format\n"+output2);
        
//      find related facts of the given text by using all three ERTools
        ERTools="All";
        String output3=chanel.findRelatedFacts(text, ERTools);
        System.out.println("\nThe related facts of the given text\n"+output3);

        
        String text2 = "Heraklion is located in Crete";
        text2=text2.replace(" ","%20");
        
        //find the triples of the recognized entities
        ERTools="WAT_and_DBpediaSpotlight";
        String output4=chanel.getTriplesOfEntities(text2,ERTools);
        System.out.println("\nThe triples of the recognized entities\n"+output4);

       
//      find the the top-10 triads of datasets containing the most triples for the recognized entities.
        ERTools="WAT_and_DBpediaSpotlight";
        String output5=chanel.textEntitiesDatasetDiscovery(text2,ERTools,3,10,"coverage");
        System.out.println("\nThe top-10 triads of datasets containing the most triples for the recognized entities\n"+output5);


        //For finding the equivalent URIs, the provenance, all the facts, and the top-K datasets of
        //a single entity, please use the LODsyndesis JAVA REST Client

    }
}
