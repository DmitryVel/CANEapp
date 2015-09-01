#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
from optparse import OptionParser
import os
import re
import sys
import os, glob
import subprocess

parser = OptionParser()
parser.add_option("--LSF", type=str, dest="LSF",
                    help="LSF settings")
parser.add_option("--total_samples", type=str, dest="total_samples",
                    help="Total number of samples")
parser.add_option("--sample_number", type=str, dest="sample_number",
                    help="Cufflinks options")
parser.add_option("--cuff", type=str, dest="cuff",
                    help="Cufflinks options")
parser.add_option("-r", "--reference", type=str, dest="reference",
                    help="Reference")
parser.add_option("--aligner", type=str, dest="aligner",
                    help="Aligner")
parser.add_option("--tools", type=str, dest="tools",
                    help="Tools")
parser.add_option("-i", "--input", type=str, dest="input",
                    help="The name of reads file")
parser.add_option("-o", "--output", type=str, dest="output",
                    help="Output location")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path to working directory")
parser.add_option("-t", "--trimming", type=str, dest="trimming",
                    help="Trim/do not trim (yes/no)")
parser.add_option("-c", "--cores", type=str, dest="cores",
                    help="Number of cores")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
(options, args) = parser.parse_args()
LSF=options.LSF
if LSF!="none":
    LSF=re.split(",",LSF)
aligner=options.aligner
total_samples=options.total_samples
sample_number=options.sample_number
cuff_options=options.cuff
cuff_options=re.split("\s+",cuff_options)
path=options.path
reference=options.reference
reference=re.split(",",reference[1:-1])
tools=options.tools
tools=re.split(",",tools[1:-1])
cores=options.cores
trimming=options.trimming
temp_file = open(os.path.join(path,"temp_file_1"), "r")
inputFileName = options.input
outFileName=options.output
inputFile = open(inputFileName, "r")
home_folder=options.folder
path_split=re.split(os.path.sep,path)
project_dir=os.path.join(home_folder,path_split[-2])
status_file=open(os.path.join(project_dir,"status.txt"),"a")
adaptor=temp_file.readlines()
adaptor_split=re.split("\s+", adaptor[0])
adaptor_3=adaptor_split[2]
adaptor_5=adaptor_split[3]
fragment_length=adaptor_split[1]
fragment_length=float(fragment_length)
fragment_SD=adaptor_split[4]
fragment_SD=float(fragment_SD)
library_type=adaptor_split[6]
i=0
quality_change=0
trimmed=0
longer=0


def allindices(string, sub, listindex, offset):
	if (string.find(sub) == -1):
		return listindex
	else:
		offset = string.index(sub)+offset
		listindex.append(offset)
		string = string[(string.index(sub)+1):]
		return allindices(string, sub, listindex, offset+1)
	    
def test(line, adaptor_5, adaptor_3):
    if re.search(adaptor_5,line):
            indexes=allindices(line, adaptor_5, [], 0)
            index_5=indexes[-1]
            index_5=index_5+len(adaptor_5)+1
            if re.search(adaptor_3,line):
                indexes=allindices(line, adaptor_3, [], 0)
                index_3=indexes[0]
                trunc=line[index_5:index_3]
            else:
                trunc=line[index_5:]
    else:
        if re.search(adaptor_3,line):
            indexes=allindices(line, adaptor_3, [], 0)
            index_3=indexes[0]
            if re.search(adaptor_5,line):
                indexes=allindices(line, adaptor_5, [], 0)
                index_5=indexes[-1]
                index_5=index_5+len(adaptor_5)+1
                trunc=line[index_5:index_3]
            else:
                trunc=line[:index_3]
        else:
            trunc=line
    if len(trunc)>24:
        return 0
    else:
        return 1

if trimming=="yes":
    fileOut = open(os.path.join(path,outFileName), "w")
    status_file.write("Trimming read file "+sample_number+" of "+total_samples+"\n")
    status_file.close()
    for line in inputFile:
        try:
            line2=inputFile.next()
        except:
            break
        line3=inputFile.next()
        line4=inputFile.next()
        short=test(line2,adaptor_5, adaptor_3)
        i=i+1
        new_line=line[-1]
        if short==0:
            longer=longer+1
            fileOut.write(line)
            index_5=0
            index_3=len(line2)-1
            if re.search(adaptor_5,line2):
                indexes=allindices(line2, adaptor_5, [], 0)
                index_5=indexes[-1]
                index_5=index_5+len(adaptor_5)+1
                if re.search(adaptor_3,line2):
                    indexes=allindices(line2, adaptor_3, [], 0)
                    index_3=indexes[0]
                    fileOut.write(line2[index_5:index_3])
                    fileOut.write(new_line)
                    trimmed=trimmed+1
                else:
                    trimmed=trimmed+1
                    fileOut.write(line2[index_5:])
            else:
                if re.search(adaptor_3,line2):
                    indexes=allindices(line2, adaptor_3, [], 0)
                    index_3=indexes[0]
                    if re.search(adaptor_5,line2):
                        indexes=allindices(line2, adaptor_5, [], 0)
                        index_5=indexes[-1]
                        index_5=index_5+len(adaptor_5)+1
                        fileOut.write(line2[index_5:index_3])
                        fileOut.write(new_line)
                        trimmed=trimmed+1
                    else:
                        trimmed=trimmed+1
                        fileOut.write(line2[:index_3])
                        fileOut.write(new_line)
                else:
                    fileOut.write(line2)
            fileOut.write(line3)
            fileOut.write(line4[index_5:index_3])
            fileOut.write(new_line)
    print "Sample: "+inputFileName           
    print "Original reads: "+str(i)
    print "Trimmed reads: "+str((trimmed*100)/i)
    print "Discarded reads: "+str(100*(i-longer)/i)
    fileOut.close()





