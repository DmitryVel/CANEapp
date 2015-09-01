#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
import os
from optparse import OptionParser
import re
import sys
import os, glob
import subprocess
import multiprocessing
import math

parser = OptionParser()
parser.add_option("-c", "--count", type=str, dest="count",
                    help="Path to htseq-count")
parser.add_option("-s", "--strand", type=str, dest="strand",
                    help="Strand selection")
parser.add_option("-i", "--input", type=str, dest="input",
                    help="Input bam file")
parser.add_option("-g", "--gtf", type=str, dest="gtf",
                    help="Input GTF file")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path to sample folder")
parser.add_option("--sam", type=str, dest="sam",
                    help="Path to samtools")
parser.add_option("--mainpath", type=str, dest="mainpath",
                    help="Path to main folder")
(options, args) = parser.parse_args()
count_path=options.count
strand=options.strand
sample=options.input
gtf_file=options.gtf
path=options.path
samtools=options.sam
mainpath=options.mainpath
HTSeq_progress_file=open(os.path.join(mainpath,"HTSeq_progress.txt"),"a")

def main():
    f = open(os.path.join(path,"counts.txt"), "w")
    print str([samtools,"sort","-n","-O","sam","-T",os.path.join(path,"temp"),"-o",os.path.join(path,"Aligned_sorted.sam"),sample])
    subprocess.call([samtools,"sort","-n","-O","sam","-T",os.path.join(path,"temp"),"-o",os.path.join(path,"Aligned_sorted.sam"),sample])
    print str([count_path,"-s",strand,os.path.join(path,"Aligned_sorted.sam"),gtf_file])
    subprocess.call([count_path,"-s",strand,os.path.join(path,"Aligned_sorted.sam"),gtf_file], stdout=f)
    f.close()
    HTSeq_progress_file.write("done\n")
    HTSeq_progress_file.close()
    
main()
