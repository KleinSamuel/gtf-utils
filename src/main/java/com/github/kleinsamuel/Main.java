package com.github.kleinsamuel;

import java.nio.file.Path;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) {

//        Path p = Path.of("/home/sam/Data/reference-genomes/ensembl/113/homo_sapiens/gtf/Homo_sapiens.GRCh38.113.gtf.gz");
        Path p = Path.of("/home/sam/Data/gobi/exonskipping/Homo_sapiens.GRCh37.67.gtf");

        try {

            new GtfFile(p.toFile());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}