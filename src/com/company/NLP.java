package com.company;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NLP
{
    private static final String DOCUMENTS_PATH = "./res/contents/";
    private int _verbCount = 0;
    private int _nounCount = 0;
    private int _adjectiveCount = 0;
    private int _adverbCount = 0;
    private int _totalTokensCount = 0;

    private PrintStream _statisticsWriter;

    private SentenceModel _sentenceModel;
    private TokenizerModel _tokenizerModel;
    private DictionaryLemmatizer _lemmatizer;
    private PorterStemmer _stemmer;
    private POSModel _posModel;
    private TokenNameFinderModel _peopleModel;
    private TokenNameFinderModel _placesModel;
    private TokenNameFinderModel _organizationsModel;

    private HashMap<String, Integer> peopleMap = new HashMap<>();

    public static void main(String[] args)
    {
        NLP statictics = new NLP();
        statictics.run();
    }

    public void run()
    {
        try
        {
            initModelsStemmerLemmatizer();

            File dir = new File(DOCUMENTS_PATH);
            File[] reviews = dir.listFiles((d, name) -> name.endsWith(".txt"));

            _statisticsWriter = new PrintStream("statistics.txt", "UTF-8");

            Arrays.sort(reviews, Comparator.comparing(File::getName));
            for (File file : reviews)
            {
                System.out.println("Movie: " + file.getName().replace(".txt", ""));
                _statisticsWriter.println("Movie: " + file.getName().replace(".txt", ""));

                String text = new String(Files.readAllBytes(file.toPath()));
                processFile(text);

                _statisticsWriter.println();
            }

            overallStatistics();
            _statisticsWriter.close();

        } catch (IOException ex)
        {
            Logger.getLogger(NLP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initModelsStemmerLemmatizer()
    {
        try
        {
//        TODO: load all OpenNLP models (+Porter stemmer + lemmatizer)
            _sentenceModel = new SentenceModel(new File(OpenNLP.SENTENCE_MODEL));
            _tokenizerModel = new TokenizerModel(new File(OpenNLP.TOKENIZER_MODEL));
            _lemmatizer = new DictionaryLemmatizer(new File(OpenNLP.LEMMATIZER_DICT));
            _stemmer = new PorterStemmer();
            _posModel = new POSModel(new File(OpenNLP.POS_MODEL));
            _peopleModel = new TokenNameFinderModel(new File(OpenNLP.NAME_MODEL));
            _placesModel = new TokenNameFinderModel(new File(OpenNLP.LOCATION_MODEL));
            _organizationsModel = new TokenNameFinderModel(new File(OpenNLP.ORGANIZATION_MODEL));


        } catch (IOException ex)
        {
            Logger.getLogger(NLP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processFile(String text)
    {
        // TODO: process the text to find the following statistics:
        // For each movie derive:
        //    - number of sentences
        int noSentences = 0;
        //    - number of tokens
        int noTokens = 0;
        //    - number of (unique) stemmed forms
        int noStemmed = 0;
        //    - number of (unique) words from a dictionary (lemmatization)
        int noWords = 0;
        //    -  people

        Span people[];
                //    - locations
        Span locations[] = new Span[] { };
        //    - organisations
        Span organisations[] = new Span[] { };

        // TODO + compute the following overall (for all movies) POS tagging statistics:

        //    - percentage number of adverbs (class variable, private int _verbCount = 0)
        //    - percentage number of adjectives (class variable, private int _nounCount = 0)
        //    - percentage number of verbs (class variable, private int _adjectiveCount = 0)
        //    - percentage number of nouns (class variable, private int _adverbCount = 0)
        //    + update _totalTokensCount

        // ------------------------------------------------------------------

        // TODO derive sentences (update noSentences variable)
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(_sentenceModel);
        noSentences = sentenceDetectorME.sentDetect(text).length;

        // TODO derive tokens and POS tags from text
        // (update noTokens and _totalTokensCount)
        TokenizerME tokenizerME = new TokenizerME(_tokenizerModel);
        String[]tokens = tokenizerME.tokenize(text);
        noTokens = tokens.length;

        // TODO perform stemming (use derived tokens)
        // (update noStemmed)

        Set <String> stems = new HashSet <>();

        ArrayList<String> tempTokens = new ArrayList<>();
        for (String token : tokens)
        {
            token = token.toLowerCase().replaceAll("[^a-z0-9]", "");
            tempTokens.add(token);
        }

        tempTokens.removeAll(Collections.singleton(""));
        tokens = tempTokens.toArray(tokens);

        for(String s : tokens){
            if(s!=null)
                stems.add(_stemmer.stem(s));
        }
        noStemmed = stems.size();


        // TODO perform lemmatization (use derived tokens)
        // (remove "O" from results - non-dictionary forms, update noWords)
        POSTaggerME posTaggerME = new POSTaggerME(_posModel);

        tokens = tokenizerME.tokenize(text);

        //czy tutaj trzeba użyć przerobionych tokenów? wtedy error
        String[] tags = posTaggerME.tag(tokens);

        String res[] = _lemmatizer.lemmatize(tokens, tags);
        ArrayList<String> lems = new ArrayList<String>(Arrays.asList(res));
        lems.removeAll(Collections.singleton("O"));

        noWords = lems.size();


        // TODO derive people, locations, organisations (use tokens),
        // (update people, locations, organisations lists).
        NameFinderME nameFinderME = new NameFinderME(_peopleModel);
        people = nameFinderME.find(tokens);
        nameFinderME = new NameFinderME(_placesModel);
        locations = nameFinderME.find(tokens);
        //    - organisations
        nameFinderME = new NameFinderME(_organizationsModel);
        organisations= nameFinderME.find(tokens);

        // TODO update overall statistics - use tags and check first letters
        // (see https://www.clips.uantwerpen.be/pages/mbsp-tags; first letter = "V" = verb?)

        for(String tag: tags){
            _totalTokensCount++;
            switch(tag.charAt(0)){
                case 'M' :
                    _verbCount++;
                    break;
                case 'V' :
                    _verbCount++;
                    break;
                case 'N' :
                    _nounCount++;
                    break;
                case 'J' :
                    _adjectiveCount++;
                    break;
                case 'R' :
                    _adverbCount++;
                    break;

            }
        }

        // ------------------------------------------------------------------

        saveResults("Sentences", noSentences);
        saveResults("Tokens", noTokens);
        saveResults("Stemmed forms (unique)", noStemmed);
        saveResults("Words from a dictionary (unique)", noWords);

        saveNamedEntities("People", people, tokens);
        saveNamedEntities("Locations", locations, tokens);
        saveNamedEntities("Organizations", organisations, tokens);

        //tutaj dodaj ludzi do listy
        for(String s : spansToStrings(people, tokens)){

            if(peopleMap.get(s.toLowerCase())==null)
                peopleMap.put(s.toLowerCase(), 1);
            else
                peopleMap.replace(s.toLowerCase(), peopleMap.get(s.toLowerCase())+1);
        }

    }

    public void printLinkedPeople(int maxPeople){
        Object[] a = peopleMap.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });
        for (int i=0; i<maxPeople; i++) {
            Object e = a[i];
            System.out.println(((Map.Entry<String, Integer>) e).getKey() + " : "
                    + ((Map.Entry<String, Integer>) e).getValue());
        }
    }

    private String[] spansToStrings(Span spans[], String tokens[])
    {
        StringBuilder s = new StringBuilder();
        for (int sp = 0; sp < spans.length; sp++)
        {
            for (int i = spans[sp].getStart(); i < spans[sp].getEnd(); i++)
            {
                s.append(tokens[i]);
                if (i < spans[sp].getEnd() - 1) s.append(" ");
            }
            if (sp < spans.length - 1) s.append(",");
        }

        return s.toString().split(",");
    }

    private void saveResults(String feature, int count)
    {
        String s = feature + ": " + count;
        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void saveNamedEntities(String entityType, Span spans[], String tokens[])
    {
        StringBuilder s = new StringBuilder(entityType + ": ");
        for (int sp = 0; sp < spans.length; sp++)
        {
            for (int i = spans[sp].getStart(); i < spans[sp].getEnd(); i++)
            {
                s.append(tokens[i]);
                if (i < spans[sp].getEnd() - 1) s.append(" ");
            }
            if (sp < spans.length - 1) s.append(", ");
        }

        System.out.println("   " + s);
        _statisticsWriter.println(s);
    }

    private void overallStatistics()
    {
        _statisticsWriter.println("---------OVERALL STATISTICS----------");
        DecimalFormat f = new DecimalFormat("#0.00");

        if (_totalTokensCount == 0) _totalTokensCount = 1;
        String verbs = f.format(((double) _verbCount * 100) / _totalTokensCount);
        String nouns = f.format(((double) _nounCount * 100) / _totalTokensCount);
        String adjectives = f.format(((double) _adjectiveCount * 100) / _totalTokensCount);
        String adverbs = f.format(((double) _adverbCount * 100) / _totalTokensCount);

        _statisticsWriter.println("Verbs: " + verbs + "%");
        _statisticsWriter.println("Nouns: " + nouns + "%");
        _statisticsWriter.println("Adjectives: " + adjectives + "%");
        _statisticsWriter.println("Adverbs: " + adverbs + "%");

        System.out.println("---------OVERALL STATISTICS----------");
        System.out.println("Adverbs: " + adverbs + "%");
        System.out.println("Adjectives: " + adjectives + "%");
        System.out.println("Verbs: " + verbs + "%");
        System.out.println("Nouns: " + nouns + "%");
    }

}
