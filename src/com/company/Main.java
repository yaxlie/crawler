package com.company;

public class Main {

    public static void main(String[] args) {
        Spider spider = new Spider();
        NLP nlp = new NLP();
        spider.searchAll("http://filmweb.pl/", 20);
        spider.saveFoundPages();
//        System.out.println("\n znalezione strony : \n");
//        spider.saveFoundPagesContent();
//        nlp.run();
//        System.out.println("\n");
//        nlp.printLinkedPeople(10);

    }
}
