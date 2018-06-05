package com.company;

public class Main {

    public static void main(String[] args) {
        Spider spider = new Spider();
        spider.search("http://filmweb.pl/", "robert de niro", 2000);

        System.out.println("\n znalezione strony : \n");
        spider.saveFoundPages();
    }
}
