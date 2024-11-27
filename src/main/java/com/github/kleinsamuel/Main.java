package com.github.kleinsamuel;

import com.github.kleinsamuel.gtfutils.GtfFile;

import java.nio.file.Path;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) {

//        Path p = Path.of("/home/sam/Data/reference-genomes/ensembl/113/homo_sapiens/gtf/Homo_sapiens.GRCh38.113.gtf.gz");
//        Path p = Path.of("/home/sam/Data/reference-annotation/gencode/47/homo_sapiens/gtf/gencode.v47.annotation.gtf.gz");
//        Path p = Path.of("/home/sam/Data/gobi/exonskipping/Homo_sapiens.GRCh37.67.gtf");
        Path p = Path.of("/home/sam/Data/reference-annotation/gencode/1/homo_sapiens/gtf/gencode_data.rel1.v2.gtf.gz");

        try {

            new GtfFile(p.toFile());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}