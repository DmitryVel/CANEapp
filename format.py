#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

import os
from optparse import OptionParser
import re
import sys
import os, glob
import subprocess

parser = OptionParser()
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
parser.add_option("-s", "--samples", type=str, dest="samples",
                    help="List of samples")
(options, args) = parser.parse_args()
path=options.path
diff_file=open(os.path.join(path,"gene_exp.diff"),"r")
diff_line=diff_file.next()
gtf_file=open(os.path.join(path,"results","final.gtf"),"r")
samples=options.samples
samples=samples.replace(" ","")
samples=samples.replace("\'","")
samples_split=re.split("\[",samples)
samples_split=samples_split[2:]
samples=[]
group_names=[]
out_path=os.path.join(path,"results")
sign_gtf_file=open(os.path.join(out_path,"significant_Cuffdiff.gtf"),"w")
progress_file=open(os.path.join(path,"progress.txt"),"a")
run_info=open(os.path.join(path,"run.info"),"r")
run_info.next()
cuffdiff_info=run_info.next()
cuffdiff_split=re.split("\s+",cuffdiff_info)
FDR_threshold=0.05
for i in xrange(len(cuffdiff_split)):
    cuffdiff_element=cuffdiff_split[i]
    if cuffdiff_element=="--FDR":
        FDR_threshold=float(cuffdiff_split[i+1])

for i in xrange(len(samples_split)/2):
    i=i*2
    group=samples_split[i]
    group=group[:-1]
    group_names.append(group)
    sample_list=samples_split[i+1]
    sample_list=sample_list[:-3]
    sample_list=re.split("\,",sample_list)
    samples.append([group,sample_list])

def main():
    combinations=[]
    global sign_genes
    sign_genes=[]
    for i in xrange(len(group_names)):
        group_name=group_names[i]
        for j in xrange(len(group_names)):
            group_name2=group_names[j]
            combination=[group_name,group_name2]
            if group_name!=group_name2 and combination not in combinations and [group_name2,group_name] not in combinations:
                combinations.append(combination)
    for i in xrange(len(combinations)):
        combination=combinations[i]
        group1=combination[0]
        group2=combination[1]
        output_file=open(os.path.join(out_path,group1+"_vs_"+group2+"_Cuffdiff.txt"),"w")
        format_out(combination,output_file)
    make_sign_gtf(sign_genes)

def format_out(combination,output_file):
    tracking_file=open(os.path.join(path,"genes.fpkm_tracking"),"r")
    expression_file=open(os.path.join(path,"genes.read_group_tracking"),"r")
    expression_file.next()
    tracking_file.next()
    annotation_file=open(os.path.join(path,"class.txt"),"r")
    group1=combination[0]
    group2=combination[1]
    output_file.write("Locus ID")
    output_file.write("\t")
    output_file.write("Gene name")
    output_file.write("\t")
    output_file.write("Locus")
    output_file.write("\t")
    output_file.write("Gene type")
    output_file.write("\t")
    for i in xrange(len(samples)):
        group=samples[i]
        sample_list=group[1]
        group_name=group[0]
        if group_name==group1 or group_name==group2:
            for j in xrange(len(sample_list)):
                sample=sample_list[j]
                output_file.write(sample)
                output_file.write("\t")
                output_file.write("\t")
    output_file.write(group1)
    output_file.write("\t")
    output_file.write("\t")
    output_file.write(group2)
    output_file.write("\t")
    output_file.write("\t")
    output_file.write("log2(fold change)")
    output_file.write("\t")
    output_file.write("p value")
    output_file.write("\t")
    output_file.write("FDR")
    output_file.write("\n")
    for line in tracking_file:
        counts1=0
        counts2=0
        N1=0
        N2=0
        split_tracking=re.split("\s+",line)
        diff_line=diff_file.next()
        diff_split=re.split("\s+",diff_line)
        FDR=diff_split[12]
        if split_tracking[0]!="tracking_id":
            locus_id=split_tracking[0]
            gene_name=split_tracking[4]
            locus_coordinates=split_tracking[6]
            for ann_line in annotation_file:
                split_annotation=re.split("\s+",ann_line)
                annotation_locus=split_annotation[0]
                if annotation_locus==locus_id:
                    gene_class=split_annotation[1]
                    break
            expression=[]
            for i in xrange(len(samples)):
                group=samples[i]
                sample_list=group[1]
                for j in xrange(len(sample_list)):
                    expression_line=expression_file.next()
                    split_expression=re.split("\s+",expression_line)
                    group_name=split_expression[1]
                    if group_name==group1 or group_name==group2:
                        replicate=split_expression[2]
                        read=split_expression[3]
                        FPKM=split_expression[6]
                        if group_name==group1:
                            counts1=counts1+float(read)
                            N1=N1+1
                        if group_name==group2:
                            counts2=counts2+float(read)
                            N2=N2+1
                        expression.append([replicate,group_name,read,FPKM])
            output_file.write(locus_id)
            output_file.write("\t")
            output_file.write(gene_name)
            output_file.write("\t")
            output_file.write(locus_coordinates)
            output_file.write("\t")
            output_file.write(gene_class)
            output_file.write("\t")
            if float(FDR)<FDR_threshold:
                if locus_id not in sign_genes:
                    sign_genes.append(locus_id)
            for i in xrange(len(samples)):
                group=samples[i]
                sample_list=group[1]
                ref_group=group[0]
                for j in xrange(len(sample_list)):
                    for l in xrange(len(expression)):
                        expression_element=expression[l]
                        group_name=expression_element[1]
                        read=expression_element[2]
                        FPKM=expression_element[3]
                        replicate=expression_element[0]
                        if group_name==group1 and int(replicate)==j and group_name==ref_group:
                            output_file.write(read)
                            output_file.write("\t")
                            output_file.write(FPKM)
                            output_file.write("\t")
                        else:
                            if group_name==group2 and int(replicate)==j and group_name==ref_group:
                                output_file.write(read)
                                output_file.write("\t")
                                output_file.write(FPKM)
                                output_file.write("\t")
            if diff_split[4]==group1 and diff_split[5]==group2:
                aver_FPKM1=diff_split[7]
                aver_FPKM2=diff_split[8]
                output_file.write(str(counts1/N1))
                output_file.write("\t")
                output_file.write(aver_FPKM1)
                output_file.write("\t")
                output_file.write(str(counts2/N2))
                output_file.write("\t")
                output_file.write(aver_FPKM2)
                output_file.write("\t")
                output_file.write(diff_split[9])
                output_file.write("\t")
                output_file.write(diff_split[11])
                output_file.write("\t")
                output_file.write(diff_split[12])
        output_file.write("\n")
    output_file.close()
    tracking_file.close()
    expression_file.close()
    annotation_file.close()

def make_sign_gtf(sign_genes):
    for line in gtf_file:
        split=re.split("\s+",line)
        locus_id=split[9]
        locus_id=locus_id[1:-2]
        for i in xrange(len(sign_genes)):
            sign_locus=sign_genes[i]
            if sign_locus==locus_id:
                sign_gtf_file.write(line)
    sign_gtf_file.close()
    
main()
