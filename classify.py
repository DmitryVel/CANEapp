#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

import re
import sys
import os, glob
import linecache
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
parser.add_option("-f", "--refpath", type=str, dest="refpath",
                    help="Path to the reference folder")
parser.add_option("-i", "--input", type=str, dest="input",
                    help="The name of input file")
(options, args) = parser.parse_args()
path=options.path
ref_filename=options.refpath
ref_file=open(ref_filename,"r")
reflines=ref_file.readlines()
input_filename=options.input
input_file=open(os.path.join(path,input_filename),"r")
output_file=open(os.path.join(path,"classified.gtf"), "w")
class_output_file=open(os.path.join(path,"classification.txt"),"w")

def check_gene_names(current_lines):
    gene_names=[]
    gene_types=[]
    for i in xrange(len(current_lines)):
        current_line=current_lines[i]
        split=re.split("\s+",current_line)
        gene_name=split[15]
        gene_name=gene_name[1:-2]
        antisense=0
        linc=0
        if len(split)>21:
            linc=0
            class_code=split[21]
            class_code=class_code[1:-2]
            if class_code=="x" or class_code=="s":
                antisense=1
        else:
            linc=1
        if antisense==0 and linc==0:
            if gene_name not in gene_names:
                gene_type=check(gene_name)
                gene_types.append(gene_type)
                gene_names.append(gene_name)
    annotated=0
    if len(gene_types)==1:
        return gene_types[0]
        annotated=1
    if gene_types.count("protein_coding")==1:
        return "protein_coding"
        annotated=1
    if gene_types.count("protein_coding")>1:
        return "overlapping_loci"
        annotated=1
    if len(gene_types)==gene_types.count("lincRNA") and antisense==0:
        return "lincRNA"
        annotated=1
    if len(gene_types)==gene_types.count("lincRNA") and antisense==1:
        return "antisense"
        annotated=1
    if annotated==0:
        print "overlapping_loci"
        return "overlapping_loci"


def check(gene_name):
    for i in xrange(len(reflines)):
        ref_line=reflines[i]
        split=re.split("\s+", ref_line)
        ref_gene_name=split[0]
        gene_type=split[1]
        if ref_gene_name==gene_name:
            return gene_type
            break
    ref_file.close()
    return "unannotated"

def main():
    current_locus="none"
    current_gene="none"
    current_lines=[]
    for line in input_file:
        split=re.split("\s+", line)
        locus=split[9]
        locus=locus[1:-2]
        if len(split)>21:
            gene_name=split[15]
            gene_name=gene_name[1:-2]
        else:
            gene_name="-"
        if locus!=current_locus:
            if current_locus!="none":
                ann=0
                if intronic==1 and annotated==0 and linc==0 and antisense==0 and mixed==0 and run_on==0 and repeat==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "intronic";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("intronic")
                    class_output_file.write("\n")
                    ann=1
                if annotated==0 and mixed==1 and antisense==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "mixed";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("mixed")
                    class_output_file.write("\n")
                    ann=1
                if annotated==0 and (intronic+run_on+repeat+linc)>1:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "mixed";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("mixed")
                    class_output_file.write("\n")
                    ann=1
                if annotated==0 and (intronic+run_on+repeat+antisense)>1:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "mixed";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("mixed")
                    class_output_file.write("\n")
                    ann=1
                if intronic==0 and annotated==0 and linc==0 and antisense==0 and mixed==0 and run_on==1 and repeat==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "run-on";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("run-on")
                    class_output_file.write("\n")
                    ann=1
                if intronic==0 and annotated==0 and linc==0 and antisense==0 and mixed==0 and run_on==0 and repeat==1:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "repeat";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("repeat")
                    class_output_file.write("\n")
                    ann=1
                if antisense==1 and annotated==0 and linc==0 and mixed==1 and run_on==0 and repeat==0 and intronic==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "novel_antisense";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("novel_antisense")
                    class_output_file.write("\n")
                    ann=1
                if antisense==1 and annotated==0 and linc==0 and mixed==0 and run_on==0 and repeat==0 and intronic==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "novel_antisense";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("novel_antisense")
                    class_output_file.write("\n")
                    ann=1
                if antisense==1 and annotated==0 and linc==1 and mixed==0 and run_on==0 and repeat==0 and intronic==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "novel_antisense";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("novel_antisense")
                    class_output_file.write("\n")
                    ann=1
                if antisense==1 and annotated==0 and linc==1 and mixed==1 and run_on==0 and repeat==0 and intronic==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "novel_antisense";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("novel_antisense")
                    class_output_file.write("\n")
                    ann=1
                if annotated==1:
                    gene_type=check_gene_names(current_lines)
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type '+gene_type+';')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write(gene_type)
                    class_output_file.write("\n")
                    ann=1
                if antisense==0 and annotated==0 and linc==1 and mixed==0 and run_on==0 and repeat==0 and intronic==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "novel_lincRNA";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("novel_lincRNA")
                    class_output_file.write("\n")
                    ann=1
                if ann==0:
                    for i in xrange(len(current_lines)):
                        current_line=current_lines[i]
                        output_file.write(current_line[:-1]+' type "no_annotation";')
                        output_file.write("\n")
                    class_output_file.write(current_locus)
                    class_output_file.write("\t")
                    class_output_file.write("no_annotation")
                    print "no_annotation"
                    class_output_file.write("\n")
            current_lines=[]
            current_locus=locus
            linc=0
            antisense=0
            annotated=0
            intronic=0
            mixed=0
            repeat=0
            run_on=0
        if gene_name!="-":
            current_gene=gene_name
        current_lines.append(line)
        if len(split)>21:
            linc=0
            class_code=split[21]
            class_code=class_code[1:-2]
            if class_code=="p":
                run_on=1
            if class_code=="r":
                repeat=1
            if class_code==".":
                mixed=1
            if class_code=="i":
                intronic=1
            if class_code=="x" or class_code=="s":
                antisense=1
            if class_code=="c" or class_code=="j" or class_code=="=" or class_code=="o" or class_code=="e":
                annotated=1
        else:
            linc=1
    ann=0
    if intronic==1 and annotated==0 and linc==0 and antisense==0:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "intronic";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("intronic")
        class_output_file.write("\n")
        ann=1
    if linc==0 and annotated==0 and antisense==0 and mixed==1:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "mixed";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("mixed")
        class_output_file.write("\n")
        ann=1
    if antisense==1 and annotated==0 and linc==0:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "novel_antisense";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("novel_antisense")
        class_output_file.write("\n")
        ann=1
    if antisense==1 and annotated==0 and linc==1:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "novel_antisense";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("novel_antisense")
        class_output_file.write("\n")
        ann=1
    if annotated==1:
        gene_type="nothing"
        fusion=check_gene_names(current_lines)
        if fusion=="fusion":
            gene_type="fusion"
        else:
            gene_type=check(current_gene)
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type '+gene_type+';')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write(gene_type)
        class_output_file.write("\n")
        ann=1
    if linc==1 and annotated==0 and antisense==0:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "novel_lincRNA";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("novel_lincRNA")
        class_output_file.write("\n")
        ann=1
    if ann==0:
        for i in xrange(len(current_lines)):
            current_line=current_lines[i]
            output_file.write(current_line[:-1]+' type "spurious_transcript";')
            output_file.write("\n")
        class_output_file.write(current_locus)
        class_output_file.write("\t")
        class_output_file.write("spurious_transcript")
        class_output_file.write("\n")
    output_file.close()
    class_output_file.close()
    
main()
