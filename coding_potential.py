#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
import os
from optparse import OptionParser
import re
import sys
import os, glob
import subprocess
import multiprocessing

parser = OptionParser()
parser.add_option("--CNCI", type=str, dest="CNCI",
                    help="CNCI")
parser.add_option("-a", "--annotation", type=str, dest="annotation",
                    help="Annotation")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
parser.add_option("-c", "--cores", type=str, dest="cores",
                    help="Cores")
(options, args) = parser.parse_args()
CNCI=options.CNCI
path=options.path
annotation=options.annotation
cores=options.cores
os.chdir(path)
gtf_file=open(os.path.join(path,"classified.gtf"),"r")
classification_file=open(os.path.join(path,"classification.txt"),"r")
novel_gtf_file=open(os.path.join(path,"novel.gtf"),"w")
novel_genes=open(os.path.join(path,"novel_genes.txt"),"w")
novel_genes.write("locus_id\ttranscript_id\tstatus\tscore\tCDS_start\tCDS_end\tCDS_length\n")

def select_novel(gtf_file):
    for line in gtf_file:
        split=re.split("\s+",line)
        biotype=split[-2]
        if biotype=='"novel_antisense";' or biotype=='"novel_lincRNA";':
            novel_gtf_file.write(line)
    novel_gtf_file.close()
    gtf_file.close()


def check_coding(gtf_file):
    print str(["python",CNCI,"-f",gtf_file,"-g","-o",path,"-m","ve","-p",cores,"-d",annotation])
    subprocess.call(["python",CNCI,"-f",gtf_file,"-g","-o","coding_potential","-m","ve","-p",cores,"-d",annotation])
    

def classify_coding_noncoding():
    gtf_file=open(os.path.join(path,"classified.gtf"),"r")
    CNCI_index_file=open(os.path.join(path,"coding_potential","CNCI.index"),"r")
    annotation_lines=CNCI_index_file.readlines()
    annotation_lines=annotation_lines[1:]
    current_locus="none"
    novel_coding=[]
    for line in gtf_file:
        split=re.split("\s+",line)
        biotype=split[-2]
        if biotype=='"novel_antisense";' or biotype=='"novel_lincRNA";':
            ref_gene_ID=split[9]
            if current_locus!=ref_gene_ID:
                if current_locus!="none":
                    novel_genes.write("\n")
                    if noncoding==0:
                        novel_coding.append(current_locus)
                current_locus=ref_gene_ID
                noncoding=1
            ref_transcript_ID=split[11]
            ref_transcript_ID=ref_transcript_ID[1:-2]
            print ref_transcript_ID
            for i in xrange(len(annotation_lines)):
                annotation_line=annotation_lines[i]
                split2=re.split("\s+",annotation_line)
                transcript_ID=split2[0]
                if ref_transcript_ID==transcript_ID:
                    coding_noncoding=split2[1]
                    score=split2[2]
                    CDS_start=split2[3]
                    CDS_end=split2[4]
                    CDS_length=split2[5]
                    if coding_noncoding=="coding":
                        noncoding=0
                    novel_genes.write(ref_gene_ID+"\t"+coding_noncoding+"\t"+ref_transcript_ID+"\t"+score+"\t"+CDS_start+"\t"+CDS_end+"\t"+CDS_length+"\n")
    novel_genes.close()
    gtf_file.close()
    return novel_coding

def reclassify(novel_coding):
    final_gtf=open(os.path.join(path,"results","final.gtf"),"w")
    gtf_file=open(os.path.join(path,"classified.gtf"),"r")
    for line in gtf_file:
        split=re.split("\s+",line)
        biotype=split[-2]
        if biotype=='"novel_antisense";' or biotype=='"novel_lincRNA";':
            ref_gene_ID=split[9]
            found=0
            for i in xrange(len(novel_coding)):
                novel_coding_id=novel_coding[i]
                if novel_coding_id==ref_gene_ID:
                    found=1
                    for j in xrange(len(split)-1):
                        final_gtf.write(split[j]+"\t")
                    final_gtf.write("putative_novel_pc\n")
                    break
            if found==0:
                final_gtf.write(line)
        else:
            final_gtf.write(line)
    final_gtf.close()
    new_classification_file=open(os.path.join(path,"class.txt"),"w")
    for line in classification_file:
        split=re.split("\s+",line)
        ref_gene_ID=split[0]
        found=0
        for i in xrange(len(novel_coding)):
            novel_coding_id=novel_coding[i]
            if novel_coding_id==ref_gene_ID:
                new_classification_file.write(ref_gene_ID+"\tputative_protein_coding\n")
                found=1
                break
        if found==0:
            new_classification_file.write(line)
    new_classification_file.close()
    classification_file.close()
                    
def main():
    select_novel(gtf_file)
    check_coding("novel.gtf")
    novel_coding=classify_coding_noncoding()
    reclassify(novel_coding)

main()
