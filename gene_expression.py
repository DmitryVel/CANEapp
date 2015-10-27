#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
from optparse import OptionParser
import re
import sys
import os, glob
import linecache
import subprocess

parser = OptionParser()
parser.add_option("-r","--rexec", type=str, dest="rexec",
                    help="Path to R")
parser.add_option("-p","--path", type=str, dest="path",
                    help="Path to project folder")
parser.add_option("-s","--samples", type=str, dest="samples",
                    help="List of samples")
parser.add_option("-g","--groups", type=str, dest="groups",
                    help="List of groups")
parser.add_option("-f","--formats", type=str, dest="formats",
                    help="List of sinlge/paired read formats")
parser.add_option("--edgeR_options", type=str, dest="edgeR_options",
                    help="edgeR options")
parser.add_option("--DESeq_options", type=str, dest="DESeq_options",
                    help="DESeq2 options")
(options, args) = parser.parse_args()
R=options.rexec
path=options.path
gtf_file_name=os.path.join(path,"results","final.gtf")
samples=options.samples
samples=re.split(",",samples)
groups=options.groups
groups=re.split(",",groups)
group_list=[]
contrast_list=[]
contrast_listDESeq2=[]
edgeR_options=options.edgeR_options
DESeq_options=options.DESeq_options
formats=options.formats
if edgeR_options!="SKIP":
    exact=re.split("\:",edgeR_options)[0]
    GLM=re.split("\:",edgeR_options)[1]
    edgeR_FDR=float(re.split("\:",edgeR_options)[2])
    edgeR_FDR_method=re.split("\:",edgeR_options)[3]
    if edgeR_FDR_method=="null":
        edgeR_FDR_method="BH"
    contrasts=re.split("\:",edgeR_options)[4]
    contrasts=re.split("-",contrasts)
    contrasts=contrasts[:-1]
    for i in xrange(len(contrasts)):
        contrast=contrasts[i]
        contrast_list.append(re.split("vs",contrast))
if DESeq_options!="SKIP":
    DESeq_FDR=float(re.split("\:",DESeq_options)[0])
    DESeq_FDR_method=re.split("\:",DESeq_options)[1]
    if DESeq_FDR_method=="null":
        DESeq_FDR_method="BH"
    contrasts=re.split("\:",DESeq_options)[2]
    contrasts=re.split("-",contrasts)
    contrasts=contrasts[:-1]
    for i in xrange(len(contrasts)):
        contrast=contrasts[i]
        contrast_listDESeq2.append(re.split("vs",contrast))
for i in xrange(len(groups)):
    group=groups[i]
    group_name=re.split("\:",group)[0]
    sample_size=re.split("\:",group)[1]
    group_list.append([group_name,int(sample_size)])

def get_boundaries(coordinate_list):
    maximum=coordinate_list[0]
    minimum=coordinate_list[0]
    for coordinate in coordinate_list:
        if coordinate>maximum:
            maximum=coordinate
        if coordinate<minimum:
            minimum=coordinate
    return [minimum,maximum]
        

def extract_gene_coordinates(gtf_file):
    out_file=open(os.path.join(path,"locations.txt"),"w")
    current_locus="none"
    current_strand="none"
    current_chromo="none"
    for gtf_line in gtf_file:
        gtf_split=re.split("\s+",gtf_line)
        start=int(gtf_split[3])
        end=int(gtf_split[4])
        gtf_gene_id=gtf_split[9]
        gtf_gene_id=gtf_gene_id[1:-2]
        gene_chunk=gtf_split[15]
        gene_chunk=gene_chunk[1:-2]
        if gtf_gene_id==current_locus:
            locations.append(start)
            locations.append(end)
            gene_list=gene_list+re.split(",",gene_chunk)
            gtf_gene_entry=[current_locus,gene_list,locations]
        else:
            if current_locus!="none":
                gene_list=gtf_gene_entry[1]
                gene_entry=""
                gene_mask=[]
                for gene in gene_list:
                    gene=re.split("\|",gene)[0]
                    if gene not in gene_mask:
                        gene_mask.append(gene)
                        if gene[:5]!="CUFF.":
                            gene_entry=gene_entry+","+gene
                if gene_entry=="":
                    gene_entry="-"
                else:
                    gene_entry=gene_entry[1:]
                coordinate=get_boundaries(locations)
                location=current_chromo+":"+str(coordinate[0])+"-"+str(coordinate[1])+":"+current_strand
                out_file.write(current_locus+"\t"+gene_entry+"\t"+location+"\n")
            current_locus=gtf_gene_id
            current_strand=gtf_split[6]
            current_chromo=gtf_split[0]
            gene_list=[]
            locations=[]
            locations.append(start)
            locations.append(end)
            gene_list=gene_list+re.split(",",gene_chunk)
            gtf_gene_entry=[current_locus,gene_list,locations]
    gene_list=gtf_gene_entry[1]
    gene_entry=""
    gene_mask=[]
    for gene in gene_list:
        gene=re.split("\|",gene)[0]
        if gene not in gene_mask:
            gene_mask.append(gene)
            if gene[:5]!="CUFF.":
                gene_entry=gene_entry+","+gene
    if gene_entry=="":
        gene_entry="-"
    else:
        gene_entry=gene_entry[1:]
    coordinate=get_boundaries(locations)
    location=current_chromo+":"+str(coordinate[0])+"-"+str(coordinate[1])+":"+current_strand
    out_file.write(current_locus+"\t"+gene_entry+"\t"+location+"\n")
    out_file.close()


def make_sign_gtf(sign_genes,sign_gtf_file):
    gtf_file=open(gtf_file_name,"r")
    for line in gtf_file:
        split=re.split("\s+",line)
        locus_id=split[9]
        locus_id=locus_id[1:-2]
        for i in xrange(len(sign_genes)):
            sign_locus=sign_genes[i]
            if sign_locus==locus_id:
                sign_gtf_file.write(line)
    sign_gtf_file.close()
    gtf_file.close()

def format_output_DESeq2():
    DESeq2_final_file=open(os.path.join(path,"results","HTSeq_DESeq.txt"),"w")
    DESeq2_final_file.write("Locus ID\tGene name\t Locus\tGene type")
    combined_counts_file=open(os.path.join(path,"combined_counts.txt"),"r")
    classification_file=open(os.path.join(path,"classification.txt"),"r")
    location_file=open(os.path.join(path,"locations.txt"),"r")
    header=combined_counts_file.next()
    header_split=re.split("\s+",header)
    header_split=header_split[1:-1]
    DESeq2_files=[]
    sign_genes=[]
    for i in xrange(len(header_split)):
        header_element=header_split[i]
        DESeq2_final_file.write("\t"+header_element)
    positions=[]
    N=0
    for element in group_list:
        group=element[0]
        N=N+element[1]
        DESeq2_final_file.write("\t"+group+" average count")
        positions.append(N)
    for filename in os.listdir(path):
        if filename[-10:]=="DESeq2.txt":
            file=open(os.path.join(path,filename),"r")
            file.next()
            DESeq2_files.append(file)
            group_label=filename[:-10]
            DESeq2_final_file.write("\tFC_"+group_label+"_et")
            DESeq2_final_file.write("\tp_"+group_label+"_et")
            DESeq2_final_file.write("\tFDR_"+group_label+"_et")
    DESeq2_final_file.write("\n")
    for count_line in combined_counts_file:
        locus_id=re.split("\s+",count_line)[0]
        counts=re.split("\s+",count_line)[1:]
        for class_line in classification_file:
            ref_locus_id=re.split("\s+",class_line)[0]
            if locus_id==ref_locus_id:
                classification=re.split("\s+",class_line)[1]
                break
        for location_line in location_file:
            ref_locus_id=re.split("\s+",location_line)[0]
            if locus_id==ref_locus_id:
                gene_entry=re.split("\s+",location_line)[1]
                location=re.split("\s+",location_line)[2]
                DESeq2_final_file.write(locus_id+"\t"+gene_entry+"\t"+location+"\t"+classification)
                break
        group1_counts=0
        group2_counts=0
        counts=counts[:-1]
        average_counts=0
        average_count_list=[]
        N=0
        positions_local=positions
        for i in xrange(len(counts)):
            count=counts[i]
            DESeq2_final_file.write("\t"+count)
            if i<positions_local[0]:
                average_counts=average_counts+int(count)
                N=N+1
            if i+1==positions_local[0]:
                average_count_list.append(str(average_counts/N))
                average_counts=0
                N=0
                positions_local=positions_local[1:]                   
        for average_count in average_count_list:
            DESeq2_final_file.write("\t"+average_count)
        for file in DESeq2_files:
            stats_line=file.next()
            stats_line_split=re.split("\s+",stats_line)
            DESeq2_final_file.write("\t"+stats_line_split[2])
            DESeq2_final_file.write("\t"+stats_line_split[5])
            DESeq2_final_file.write("\t"+stats_line_split[6])
            if stats_line_split[6]!="NA":
                if float(stats_line_split[6])<DESeq_FDR:
                    sign_genes.append(ref_locus_id)
        DESeq2_final_file.write("\n")
    DESeq2_final_file.close()
    classification_file.close()
    location_file.close()
    sign_gtf_file=open(os.path.join(path,"results","significant_DESeq2.gtf"),"w")
    make_sign_gtf(sign_genes,sign_gtf_file)

def format_output_GLM():
    GLM_final_file=open(os.path.join(path,"results","HTSeq_edgeR_LM.txt"),"w")
    GLM_final_file.write("Locus ID\tGene name\t Locus\tGene type")
    combined_counts_file=open(os.path.join(path,"combined_counts.txt"),"r")
    classification_file=open(os.path.join(path,"classification.txt"),"r")
    location_file=open(os.path.join(path,"locations.txt"),"r")
    header=combined_counts_file.next()
    header_split=re.split("\s+",header)
    header_split=header_split[1:-1]
    edgeR_GLM_files=[]
    sign_genes=[]
    for i in xrange(len(header_split)):
        header_element=header_split[i]
        GLM_final_file.write("\t"+header_element)
    positions=[]
    N=0
    for element in group_list:
        group=element[0]
        N=N+element[1]
        GLM_final_file.write("\t"+group+" average count")
        positions.append(N)
    for filename in os.listdir(path):
        if filename[-7:]=="GLM.txt":
            file=open(os.path.join(path,filename),"r")
            file.next()
            edgeR_GLM_files.append(file)
            group_label=filename[:-7]
            GLM_final_file.write("\tFC_"+group_label+"_et")
            GLM_final_file.write("\tp_"+group_label+"_et")
            GLM_final_file.write("\tFDR_"+group_label+"_et")
    GLM_final_file.write("\n")
    gtf_gene_entry=[]
    locations=[]
    gene_list=[]
    for count_line in combined_counts_file:
        locus_id=re.split("\s+",count_line)[0]
        counts=re.split("\s+",count_line)[1:]
        for class_line in classification_file:
            ref_locus_id=re.split("\s+",class_line)[0]
            if locus_id==ref_locus_id:
                classification=re.split("\s+",class_line)[1]
                break
        for location_line in location_file:
            ref_locus_id=re.split("\s+",location_line)[0]
            if locus_id==ref_locus_id:
                gene_entry=re.split("\s+",location_line)[1]
                location=re.split("\s+",location_line)[2]
                GLM_final_file.write(locus_id+"\t"+gene_entry+"\t"+location+"\t"+classification)
                break
        group1_counts=0
        group2_counts=0
        counts=counts[:-1]
        average_counts=0
        average_count_list=[]
        N=0
        positions_local=positions
        for i in xrange(len(counts)):
            count=counts[i]
            GLM_final_file.write("\t"+count)
            if i<positions_local[0]:
                average_counts=average_counts+int(count)
                N=N+1
            if i+1==positions_local[0]:
                average_count_list.append(str(average_counts/N))
                average_counts=0
                N=0
                positions_local=positions_local[1:]                   
        for average_count in average_count_list:
            GLM_final_file.write("\t"+average_count)
        for file in edgeR_GLM_files:
            stats_line=file.next()
            stats_line_split=re.split("\s+",stats_line)
            GLM_final_file.write("\t"+stats_line_split[1])
            GLM_final_file.write("\t"+stats_line_split[4])
            GLM_final_file.write("\t"+stats_line_split[5])
            if float(stats_line_split[5])<edgeR_FDR:
                sign_genes.append(ref_locus_id)
        GLM_final_file.write("\n")
    GLM_final_file.close()
    classification_file.close()
    location_file.close()
    sign_gtf_file=open(os.path.join(path,"results","significant_edgeR_LM.gtf"),"w")
    make_sign_gtf(sign_genes,sign_gtf_file)

def format_output_et():
    et_final_file=open(os.path.join(path,"results","HTSeq_edgeR_et.txt"),"w")
    et_final_file.write("Locus ID\tGene name\t Locus\tGene type")
    combined_counts_file=open(os.path.join(path,"combined_counts.txt"),"r")
    classification_file=open(os.path.join(path,"classification.txt"),"r")
    location_file=open(os.path.join(path,"locations.txt"),"r")
    header=combined_counts_file.next()
    header_split=re.split("\s+",header)
    header_split=header_split[1:-1]
    edgeR_et_files=[]
    sign_genes=[]
    for i in xrange(len(header_split)):
        header_element=header_split[i]
        et_final_file.write("\t"+header_element)
    positions=[]
    N=0
    for element in group_list:
        group=element[0]
        N=N+element[1]
        et_final_file.write("\t"+group+" average count")
        positions.append(N)
    for filename in os.listdir(path):
        if filename[-14:]=="exact_text.txt":
            file=open(os.path.join(path,filename),"r")
            file.next()
            edgeR_et_files.append(file)
            group_label=filename[:-14]
            et_final_file.write("\tFC_"+group_label+"_et")
            et_final_file.write("\tp_"+group_label+"_et")
            et_final_file.write("\tFDR_"+group_label+"_et")
    et_final_file.write("\n")
    for count_line in combined_counts_file:
        locus_id=re.split("\s+",count_line)[0]
        counts=re.split("\s+",count_line)[1:]
        for class_line in classification_file:
            ref_locus_id=re.split("\s+",class_line)[0]
            if locus_id==ref_locus_id:
                classification=re.split("\s+",class_line)[1]
                break
        for location_line in location_file:
            ref_locus_id=re.split("\s+",location_line)[0]
            if locus_id==ref_locus_id:
                gene_entry=re.split("\s+",location_line)[1]
                location=re.split("\s+",location_line)[2]
                et_final_file.write(locus_id+"\t"+gene_entry+"\t"+location+"\t"+classification)
                break
        group1_counts=0
        group2_counts=0
        counts=counts[:-1]
        average_counts=0
        average_count_list=[]
        N=0
        positions_local=positions
        for i in xrange(len(counts)):
            count=counts[i]
            et_final_file.write("\t"+count)
            if i<positions_local[0]:
                average_counts=average_counts+int(count)
                N=N+1
            if i+1==positions_local[0]:
                average_count_list.append(str(average_counts/N))
                average_counts=0
                N=0
                positions_local=positions_local[1:]                   
        for average_count in average_count_list:
            et_final_file.write("\t"+average_count)
        for file in edgeR_et_files:
            stats_line=file.next()
            stats_line_split=re.split("\s+",stats_line)
            et_final_file.write("\t"+stats_line_split[1])
            et_final_file.write("\t"+stats_line_split[3])
            et_final_file.write("\t"+stats_line_split[4])
            if float(stats_line_split[4])<edgeR_FDR:
                sign_genes.append(ref_locus_id)
        et_final_file.write("\n")
    et_final_file.close()
    classification_file.close()
    location_file.close()
    sign_gtf_file=open(os.path.join(path,"results","significant_edgeR_et.gtf"),"w")
    make_sign_gtf(sign_genes,sign_gtf_file)
        
    

def DESeq2():
    DESeq2_script=open(os.path.join(path,"DESeq2.R"),"w")
    DESeq2_script.write('library("DESeq2")\n')
    DESeq2_script.write('allGeneCounts<-read.table('+'"'+os.path.join(path,"combined_counts.txt")+'"'+',header=TRUE,row.names=1,sep="\\t")\n')
    DESeq2_design_file=open(os.path.join(path,"design.txt"),"w")
    DESeq2_design_file.write('\t"condition"\t"type"\n')
    N=0
    for i in xrange(len(group_list)):
        group=group_list[i]
        group_name=group[0]
        group_size=group[1]
        for j in xrange(group_size):
            sample=samples[j+N]
            read_format=formats[j+N]
            if read_format=="single":
                read_format="single-read"
            else:
                read_format="paired-end"
            DESeq2_design_file.write(sample+"\t")
            DESeq2_design_file.write(group_name+"\t")
            DESeq2_design_file.write(read_format+"\n")
        N=N+group_size
    DESeq2_design_file.close()
    DESeq2_script.write('designMatrix<-read.table('+'"'+os.path.join(path,"design.txt")+'"'+',header=TRUE,row.names=1,sep="\\t")\n')
    DESeq2_script.write('dds <- DESeqDataSetFromMatrix(countData=allGeneCounts,colData=designMatrix, design=~condition)\n')
    DESeq2_script.write('dds <- DESeq(dds)\n')
    for i in xrange(len(contrast_listDESeq2)):
        contrast=contrast_listDESeq2[i]
        group1=contrast[0]
        group2=contrast[1]
        DESeq2_script.write('results'+group1+'vs'+group2+' <- results(dds,contrast=c("condition",'+'"'+group1+'"'+','+'"'+group2+'"'+'),pAdjustMethod = '+'"'+DESeq_FDR_method+'"'+')\n')
        DESeq2_script.write('write.table('+'results'+group1+'vs'+group2+', '+'"'+os.path.join(path,group1+'vs'+group2+'DESeq2.txt')+'"'+', sep="\\t", quote=FALSE)\n')
    DESeq2_script.close()
    subprocess.call([R+"script",os.path.join(path,"DESeq2.R")])
            

def edgeR_exact():
    combined_counts_file=open(os.path.join(path,"combined_counts.txt"),"r")
    N=len(combined_counts_file.readlines())
    combined_counts_file.close()
    edgeR_script=open(os.path.join(path,"edgeR_et.R"),"w")
    edgeR_script.write('library("edgeR")\n')
    edgeR_script.write('allGeneCounts<-read.table('+'"'+os.path.join(path,"combined_counts.txt")+'"'+',header=TRUE,row.names=1,sep="\\t")\n')
    groups='group <- c('
    for i in xrange(len(group_list)):
        group=group_list[i]
        group_name=group[0]
        group_size=group[1]
        groups=groups+'rep('+'"'+group_name+'"'+','+str(group_size)+'),'
    groups=groups[:-1]+')'
    edgeR_script.write(groups+"\n")
    edgeR_script.write('dge <- DGEList(counts=allGeneCounts, group=group)\n')
    edgeR_script.write('dge <- calcNormFactors(dge)\n')
    edgeR_script.write('dge <- estimateCommonDisp(dge)\n')
    edgeR_script.write('dge <- estimateTagwiseDisp(dge)\n')
    for i in xrange(len(contrast_list)):
        contrast=contrast_list[i]
        group1=contrast[0]
        group2=contrast[1]
        edgeR_script.write('et'+group1+'vs'+group2+' <- exactTest(dge,pair=c('+'"'+group1+'"'','+'"'+group2+'"'+'))\n')
        edgeR_script.write('results'+group1+'vs'+group2+' <- topTags('+'et'+group1+'vs'+group2+',n='+str(N)+',sort.by="none",adjust.method = '+'"'+edgeR_FDR_method+'"'+')\n')
        edgeR_script.write('write.table('+'results'+group1+'vs'+group2+', '+'"'+os.path.join(path,group1+'vs'+group2+'exact_text.txt')+'"'+', sep="\\t", quote=FALSE)\n')
    edgeR_script.close()
    subprocess.call([R+"script",os.path.join(path,"edgeR_et.R")])
    

def edgeR_GLM():
    combined_counts_file=open(os.path.join(path,"combined_counts.txt"),"r")
    N=len(combined_counts_file.readlines())
    combined_counts_file.close()
    edgeR_script=open(os.path.join(path,"edgeR_GLM.R"),"w")
    edgeR_script.write('library("edgeR")\n')
    edgeR_script.write('allGeneCounts<-read.table('+'"'+os.path.join(path,"combined_counts.txt")+'"'+',header=TRUE,row.names=1,sep="\\t")\n')
    groups='group <- c('
    for i in xrange(len(group_list)):
        group=group_list[i]
        group_name=group[0]
        group_size=group[1]
        groups=groups+'rep('+'"'+group_name+'"'+','+str(group_size)+'),'
    groups=groups[:-1]+')'
    edgeR_script.write(groups+"\n")
    edgeR_script.write('dge <- DGEList(counts=allGeneCounts, group=group)\n')
    edgeR_script.write('design <- model.matrix(~group+0, data=dge$samples)\n')
    edgeR_script.write('disp <- estimateGLMCommonDisp(dge, design)\n')
    edgeR_script.write('disp <- estimateGLMTrendedDisp(disp, design)\n')
    edgeR_script.write('disp <- estimateGLMTagwiseDisp(disp, design)\n')
    edgeR_script.write('fit <- glmFit(disp, design)\n')
    for i in xrange(len(contrast_list)):
        contrast=contrast_list[i]
        group1=contrast[0]
        group2=contrast[1]
        edgeR_script.write(group1+'vs'+group2+' <- makeContrasts('+'group'+group1+'-'+'group'+group2+', levels=design)\n')
        edgeR_script.write('lrt.'+group1+'vs'+group2+' <- glmLRT(fit, contrast='+group1+'vs'+group2+')\n')
        edgeR_script.write('results'+group1+'vs'+group2+' <- topTags('+'lrt.'+group1+'vs'+group2+',n='+str(N)+',sort.by="none",adjust.method = '+'"'+edgeR_FDR_method+'"'+')\n')
        edgeR_script.write('write.table(''results'+group1+'vs'+group2+','+'"'+os.path.join(path,group1+'vs'+group2+'GLM.txt')+'"'+', sep="\\t", quote=FALSE)\n')
    edgeR_script.close()
    subprocess.call([R+"script",os.path.join(path,"edgeR_GLM.R")])

def combine():
    local_samples=samples
    combined_counts=open(os.path.join(path,"combined_counts.txt"),"w")
    for i in xrange(len(local_samples)):
        sample=local_samples[i]
        combined_counts.write("\t"+sample)
    combined_counts.write("\n")
    first_file=open(os.path.join(path,local_samples[0],"counts.txt"),"r")
    first_file_lines=first_file.readlines()
    N=len(first_file_lines)
    first_file.close()
    local_samples=local_samples[1:]
    count_files=[]
    for i in xrange(len(local_samples)):
        sample=local_samples[i]
        counts_file=open(os.path.join(path,sample,"counts.txt"),"r")
        count_files.append(counts_file)
    for j in xrange(N-5):
        line=first_file_lines[j]
        line=line[:-1]
        for i in xrange(len(count_files)):
            counts_file=count_files[i]
            next_line=counts_file.next()
            line=line+"\t"+re.split("\s+",next_line)[1]
        combined_counts.write(line+"\n")
    combined_counts.close()

gtf_file=open(os.path.join(path,"all.combined.gtf"),"r")
extract_gene_coordinates(gtf_file)
combine()
if edgeR_options!="SKIP":
    if GLM=="yes":
        edgeR_GLM()
        format_output_GLM()
    if exact=="yes":
        edgeR_exact()
        format_output_et()
if DESeq_options!="SKIP":
    DESeq2()
    format_output_DESeq2()

