package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.*;

public class Spider {
    private static final int MAX_PAGES_TO_SEARCH = 200;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();

    private List<String> successPagesUrls = new ArrayList<String>();


    /**
     * Our main launching point for the Spider's functionality. Internally it creates spider legs
     * that make an HTTP request and parse the response (the web page).
     *
     * @param url
     *            - The starting point of the spider
     * @param searchWord
     *            - The word or string that you are searching for
     */
    public void search(String url, String searchWord, int maxPagesToSearch)
    {

        Set<String> pagesVisited = new HashSet<String>();
        List<String> pagesToVisit = new LinkedList<String>();
        List<String> successPagesUrls = new ArrayList<String>();

        while(this.pagesVisited.size() < maxPagesToSearch)
        {
            String currentUrl;
            SpiderLeg leg = new SpiderLeg();
            if(this.pagesToVisit.isEmpty())
            {
                currentUrl = url;
                this.pagesVisited.add(url);
            }
            else
            {
                currentUrl = this.nextUrl();
            }
            leg.crawl(currentUrl); // Lots of stuff happening here. Look at the crawl method in
            // SpiderLeg
            boolean success = leg.searchForWord(searchWord);
            if(success)
            {
                this.successPagesUrls.add(currentUrl);
                System.out.println(String.format("**Success** Word %s found at %s", searchWord, currentUrl));
            }
            this.pagesToVisit.addAll(leg.getLinks());
        }
        System.out.println("\n**Done** Visited " + this.pagesVisited.size() + " web page(s) \n Found "
                + this.successPagesUrls.size() + " pages containing the keyword.");
    }

    public void searchAll(String url, int maxPagesToSearch)
    {
        Set<String> pagesVisited = new HashSet<String>();
        List<String> pagesToVisit = new LinkedList<String>();
        List<String> successPagesUrls = new ArrayList<String>();
        while(this.pagesVisited.size() < maxPagesToSearch)
        {
            String currentUrl;
            SpiderLeg leg = new SpiderLeg();
            if(this.pagesToVisit.isEmpty())
            {
                currentUrl = url;
                this.pagesVisited.add(url);
            }
            else
            {
                currentUrl = this.nextUrl();
            }
            leg.crawl(currentUrl); // Lots of stuff happening here. Look at the crawl method in
            // SpiderLeg
                this.successPagesUrls.add(currentUrl);

            this.pagesToVisit.addAll(leg.getLinks());
        }
        System.out.println("\n**Done** Visited " + this.pagesVisited.size() + " web page(s) \n Found "
                + this.successPagesUrls.size() + " pages containing the keyword.");
    }


    /**
     * Returns the next URL to visit (in the order that they were found). We also do a check to make
     * sure this method doesn't return a URL that has already been visited.
     *
     * @return
     */

    private String nextUrl()
    {
        String nextUrl;
        do
        {
            nextUrl = this.pagesToVisit.remove(0);
        } while(this.pagesVisited.contains(nextUrl));
        this.pagesVisited.add(nextUrl);
        return nextUrl;
    }

    private void deleteFiles(String path){
        try {
            for (File file : new java.io.File(path).listFiles())
                if (!file.isDirectory())
                    file.delete();
        }
        catch(Exception ignored){}
    }

    public void saveFoundPages(){
        int i=0;
        deleteFiles(".\\res\\pages\\");
        new File(".\\res\\pages").mkdirs();
        for(String s : successPagesUrls){
            System.out.println("Saving... "+ s);
            try {
                OutputStream out = new FileOutputStream(".\\res\\pages\\" + i++ + ".html");
                URL url = new URL(s);
                URLConnection conn = url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                copy(is, out);
                is.close();
                out.close();
            }
            catch (Exception ignored){}

        }
    }

    public void saveFoundPagesContent(){
        int i=0;
        deleteFiles(".\\res\\contents\\");
        new File(".\\res\\contents").mkdirs();
        for(String s : successPagesUrls){
            System.out.println("Saving... "+ s);
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(".\\res\\contents\\" + i++ + ".txt"));
                Document doc = Jsoup.connect(s).get();
                Elements ps = doc.select("p");
                out.write(ps.text());
                out.close();
            }
            catch (Exception ignored){}
        }
    }



    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[4096];
        while (true) {
            int numBytes = from.read(buffer);
            if (numBytes == -1) {
                break;
            }
            to.write(buffer, 0, numBytes);
        }
    }
}
