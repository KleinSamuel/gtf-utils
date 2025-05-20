package com.github.kleinsamuel;

import com.github.kleinsamuel.gtfutils.GtfConfig;
import com.github.kleinsamuel.gtfutils.GtfFile;
import com.github.kleinsamuel.gtfutils.feature.GeneFeature;
import com.github.kleinsamuel.gtfutils.feature.GtfFeature;

import java.nio.file.Path;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) {

        Path p = Path.of("/home/sam/Data/reference-genomes/ensembl/113/homo_sapiens/gtf/Homo_sapiens.GRCh38.113.gtf.gz");
//        Path p = Path.of("/home/sam/Data/reference-annotation/gencode/47/homo_sapiens/gtf/gencode.v47.annotation.gtf.gz");
//        Path p = Path.of("/home/sam/Data/gobi/exonskipping/Homo_sapiens.GRCh37.67.gtf");
//        Path p = Path.of("/home/sam/Data/reference-annotation/gencode/1/homo_sapiens/gtf/gencode_data.rel1.v2.gtf.gz");

        try {

            GtfFile gtf = new GtfFile(p.toFile());

            gtf.parseNextContig();

            System.out.println(gtf.getAllGeneFeatureIds().size());

//            gtf.getAllGeneFeatureIds().stream().toList().forEach(geneId -> {
//
//                GeneFeature geneFeature = gtf.getGeneFeature(geneId);
//
//                geneFeature.getTranscripts().forEach(transcript -> {
//
//                    if (transcript.getFeatures(GtfConfig.TYPE_EXON_DEFAULT).size() > 1) {
//                        System.out.println(transcript.getTranscriptId());
//                        System.exit(1);
//                    }
//                });
//
//            });

            GeneFeature gene = gtf.getGeneFeature("ENSG00000306579");

            gene.getTranscripts().forEach(transcript -> {
                System.out.println(transcript.getTranscriptId());

                for (GtfFeature exon : transcript.getFeatures(GtfConfig.TYPE_EXON_DEFAULT)) {
                    System.out.println(exon.getBaseData().getStart() + "\t" + exon.getBaseData().getEnd());
                }

                System.out.println("introns");

                for (GtfFeature intron : transcript.getFeatures(GtfConfig.TYPE_INTRON_DEFAULT)) {
                    System.out.println(intron.getBaseData().getStart() + "\t" + intron.getBaseData().getEnd());
                }

            });

//            gtf.parseNextContig();
//
//            System.out.println(gtf.getAllGeneFeatureIds().size());

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}