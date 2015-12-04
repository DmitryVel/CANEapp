#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
import os
from optparse import OptionParser
import re
import sys
import os, glob
import subprocess
import multiprocessing
from ftplib import FTP
import urllib2
from posixpath import basename, dirname
from urlparse import urlparse
import shutil
import platform

parser = OptionParser()
parser.add_option("-i", "--input", type=str, dest="input",
                    help="The name of input file")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
folders=[]
(options, args) = parser.parse_args()
project_file_name=options.input
project_file=open(project_file_name, "r")
project_lines=project_file.readlines()
home_folder=options.folder
project_dir=os.path.dirname(project_file_name)
status_file=open(os.path.join(project_dir,"status.txt"),"w")
status_file.close()
mem_config_location="/proc/meminfo"
mem_file=open(mem_config_location,"r")
total_mem_line=mem_file.next()
total_mem_split=re.split("\s+",total_mem_line)
total_mem=int(total_mem_split[1])
os_name=platform.linux_distribution()[0]

def verify_install():
    global os_name
    incomplete=0
    temp_tools_file=open(os.path.join(home_folder,"Pipeline/tools","temp_tools.txt"),"w")
    tools_file=open(os.path.join(home_folder,"Pipeline/tools","tools.txt"),"r")
    update_file=open(os.path.join(home_folder,"Pipeline/tools/CANE_update.txt"),"r")
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    tophat_line=tools_file.next()
    tophat_folder=re.split("\s+",tophat_line)[0]
    tophat_line=update_file.next()
    tophat_link=re.split("\s+",tophat_line)[0]
    tophat_version=re.split("\s+",tophat_line)[1]
    if not os.path.isfile(os.path.join(tophat_folder,"tophat")):
        incomplete=1
        try:
            os.system("rm -r "+tophat_folder)
        except:
            print "No folder exists"
        install_TopHat(tophat_link,tophat_version,temp_tools_file)
    STAR_line=tools_file.next()
    STAR_file=re.split("\s+",STAR_line)[0]
    STAR_folder=dirname(dirname(STAR_file))
    STAR_line=update_file.next()
    STAR_link=re.split("\s+",STAR_line)[0]
    STAR_version=re.split("\s+",STAR_line)[1]
    if not os.path.isfile(STAR_file):
        incomplete=1
        try:
            os.system("rm -r "+STAR_folder)
        except:
            print "No folder exists"
        install_STAR(STAR_link,STAR_version,temp_tools_file)
    cuff_line=tools_file.next()
    cuff_folder=re.split("\s+",cuff_line)[0]
    cuff_line=update_file.next()
    cuff_link=re.split("\s+",cuff_line)[0]
    cuff_version=re.split("\s+",cuff_line)[1]
    if not os.path.isfile(os.path.join(cuff_folder,"cufflinks")):
        incomplete=1
        try:
            os.system("rm -r "+cuff_folder)
        except:
            print "No folder exists"
        install_Cufflinks(cuff_link,cuff_version,temp_tools_file)
    CNCI_line=tools_file.next()
    CNCI_file=re.split("\s+",CNCI_line)[0]
    CNCI_folder=dirname(CNCI_file)
    CNCI_line=update_file.next()
    CNCI_link=re.split("\s+",CNCI_line)[0]
    CNCI_version=re.split("\s+",CNCI_line)[1]
    if not os.path.isfile(CNCI_file):
        incomplete=1
        try:
            os.system("rm -r "+CNCI_folder)
        except:
            print "No folder exists"
        install_CNCI(CNCI_link,CNCI_version,temp_tools_file)
    Primer3_line=tools_file.next()
    Primer3_file=re.split("\s+",Primer3_line)[0]
    Primer3_folder=dirname(dirname(Primer3_file))
    Primer3_line=update_file.next()
    Primer3_link=re.split("\s+",Primer3_line)[0]
    Primer3_version=re.split("\s+",Primer3_line)[1]
    if not os.path.isfile(Primer3_file):
        incomplete=1
        try:
            os.system("rm -r "+Primer3_folder)
        except:
            print "No folder exists"
        install_Primer3(Primer3_link,Primer3_version,temp_tools_file)
    samtools_line=tools_file.next()
    samtools_folder=re.split("\s+",samtools_line)[0]
    samtools_line=update_file.next()
    samtools_link=re.split("\s+",samtools_line)[0]
    samtools_version=re.split("\s+",samtools_line)[1]
    if not os.path.isfile(os.path.join(samtools_folder,"samtools")):
        incomplete=1
        try:
            os.system("rm -r "+samtools_folder)
        except:
            print "No folder exists"
        install_samtools(samtools_link,samtools_version,temp_tools_file)
    bowtie_line=tools_file.next()
    bowtie_folder=re.split("\s+",bowtie_line)[0]
    bowtie_line=update_file.next()
    bowtie_link=re.split("\s+",bowtie_line)[0]
    bowtie_version=re.split("\s+",bowtie_line)[1]
    if not os.path.isfile(os.path.join(bowtie_folder,"bowtie2")):
        incomplete=1
        try:
            os.system("rm -r "+bowtie_folder)
        except:
            print "No folder exists"
        install_bowtie(bowtie_link,bowtie_version,temp_tools_file)
    SRA_line=tools_file.next()
    SRA_file=re.split("\s+",SRA_line)[0]
    SRA_folder=dirname(dirname(SRA_file))
    SRA_line=update_file.next()
    SRA_link=re.split("\s+",SRA_line)[0]
    if os_name=='CentOS' or os_name=='Red Hat Enterprise Linux Server' or os_name=='Fedora':
        SRA_link=re.split("\|",SRA_link)[0]
    else:
        if os_name=='Ubuntu':
            SRA_link=re.split("\|",SRA_link)[1]
        else:
            SRA_link=re.split("\|",SRA_link)[0]
    SRA_version=re.split("\s+",SRA_line)[1]
    if not os.path.isfile(SRA_file):
        incomplete=1
        try:
            os.system("rm -r "+SRA_folder)
        except:
            print "No folder exists"
        install_SRA(SRA_link,SRA_version,temp_tools_file)
    HTSeq_line=tools_file.next()
    HTSeq_file=re.split("\s+",HTSeq_line)[0]
    HTSeq_folder=dirname(dirname(HTSeq_file))
    HTSeq_line=update_file.next()
    HTSeq_link=re.split("\s+",HTSeq_line)[0]
    HTSeq_version=re.split("\s+",HTSeq_line)[1]
    if not os.path.isfile(HTSeq_file):
        incomplete=1
        try:
            os.system("rm -r "+HTSeq_folder)
        except:
            print "No folder exists"
        install_HTSeq(HTSeq_link,HTSeq_version,temp_tools_file)
    R_line=tools_file.next()
    R_file=re.split("\s+",R_line)[0]
    R_folder=dirname(dirname(R_file))
    R_line=update_file.next()
    R_link=re.split("\s+",R_line)[0]
    R_version=re.split("\s+",R_line)[1]
    if not os.path.isfile(R_file):
        incomplete=1
        try:
            os.system("rm -r "+R_folder)
        except:
            print "No folder exists"
        install_R(R_link,R_version,temp_tools_file)
    temp_tools_file.close()
    os.system("rm "+os.path.join(home_folder,"Pipeline/tools","temp_tools.txt"))
    if incomplete==0:
        return True
    else:
        return False
    
    

def install_numpy():
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file("http://sourceforge.net/projects/numpy/files/NumPy/1.9.2/numpy-1.9.2.zip","numpy-1.9.2.zip")
    subprocess.call(["unzip","numpy-1.9.2.zip"])
    os.system("rm "+"numpy-1.9.2.zip")
    os.chdir(os.path.join(home_folder,"Pipeline/tools","numpy-1.9.2"))
    subprocess.call(["python","setup.py","install","--user"])
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    
def install_essentials():
    op_sys="undetermined"
    global os_name
    if os_name=='CentOS' or os_name=='Red Hat Enterprise Linux Server' or os_name=='Fedora':
        try:                    #For CentOS, Fedora and RedHat
            download_file("http://psychiatry.med.miami.edu/documents/research/CANE_library_CentOS.sh",os.path.join(home_folder,"CANE_library_CentOS.sh"))
            os.system("chmod -R 750 "+os.path.join(home_folder,"CANE_library_CentOS.sh"))
            subprocess.call(os.path.join(home_folder,"CANE_library_CentOS.sh"))
        except:
            print "You don't have administrative rights"
    else:
        if os_name=='Ubuntu':
            try:
                download_file("http://psychiatry.med.miami.edu/documents/research/CANE_library_Ubuntu.sh",os.path.join(home_folder,"CANE_library_Ubuntu.sh"))
                os.system("chmod -R 750 "+os.path.join(home_folder,"CANE_library_Ubuntu.sh"))
                subprocess.call(os.path.join(home_folder,"CANE_library_Ubuntu.sh"))    
            except:
                print "You don't have administrative rights"
    install_numpy()

      
def get_project_info(project_lines):
    global total_mem
    options_split=re.split("\|",project_lines[3])
    basic_options=options_split[0]
    basic_options_split=re.split("\;",basic_options)
    species=basic_options_split[3]
    print "Species: "+ species
    assembly=basic_options_split[4]
    print "Assembly: "+ assembly
    fasta=basic_options_split[5]
    print "Fasta: "+ fasta
    gtf=basic_options_split[6]
    print "GTF: "+ gtf
    aligner=basic_options_split[10]
    print "Aligner: "+ aligner
    try:
        LSF=re.split("\t",project_lines[1])[5]
        LSF=re.split(",",LSF)
        total_mem=100000000
    except:
        LSF=[]
    return [species,assembly,fasta,gtf,aligner,LSF]
    
def build_bowtie_index(species,assembly,fasta_filename,index_base,reference_file,LSF):
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Preparing Bowtie index...\n")
    status_file.close()
    index_dir=os.path.join(home_folder,"Pipeline","ref",index_base)
    os.mkdir(index_dir)
    if LSF!=[]:
        LSFProject=LSF[4]
        LSFProject=LSFProject.replace("\n","")
        LSFProject=LSFProject.replace("\r","")
        LSF_run=['bsub','-o',os.path.join(home_folder,"Pipeline","ref","tophat.out"),'-e',os.path.join(home_folder,"Pipeline","ref","tophat.err"),'-q',"general",'-R','"rusage[mem='+"28000"+'] span[hosts=1]"',"-n","1","-W","160:00","-P",LSFProject]
    else:
        LSF_run=[]
    subprocess.call(LSF_run+[os.path.join(home_folder,"Pipeline/tools/bowtie2-2.2.5/bowtie2-build"),os.path.join(home_folder,"Pipeline/ref",fasta_filename),os.path.join(index_dir,index_base+"_bowtie")])
    if LSF!=[]:
        while True:
            try:
                tophat_out=open(os.path.join(home_folder,"Pipeline","ref","tophat.out"),"r")
                tophat_out.close()
                os.system("cp "+os.path.join(home_folder,"Pipeline","ref","tophat.out")+" "+os.path.join(project_dir,"logs"))
                os.system("cp "+os.path.join(home_folder,"Pipeline","ref","tophat.err")+" "+os.path.join(project_dir,"logs"))
                os.system("rm "+os.path.join(home_folder,"Pipeline","ref","tophat.out"))
                os.system("rm "+os.path.join(home_folder,"Pipeline","ref","tophat.err"))
                break
            except:
                continue
    reference_file.write(species+"\t"+assembly+"\t"+os.path.join(index_dir,index_base+"_bowtie")+"\n")
    shutil.copy2(os.path.join(home_folder,"Pipeline/ref",fasta_filename),os.path.join(index_dir,index_base+"_bowtie.fa"))
        
def build_STAR_index(species,assembly,fasta_filename,index_base,reference_file,LSF):
    STAR_proc=int(total_mem/50000000)
    if LSF!=[]:
        if STAR_proc>int(LSF[2]):
            STAR_proc=int(LSF[2])
        LSFProject=LSF[4]
        LSFProject=LSFProject.replace("\n","")
        LSFProject=LSFProject.replace("\r","")
        LSF_run=['bsub','-o',os.path.join(home_folder,"Pipeline","ref","STAR.out"),'-e',os.path.join(home_folder,"Pipeline","ref","STAR.err"),'-q',"bigmem",'-R','"rusage[mem='+"100000"+'] span[hosts=1]"',"-n","2","-W","24:00","-P",LSFProject]
    else:
        LSF_run=[]
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Preparing STAR index...\n")
    cores=multiprocessing.cpu_count()
    index_dir=os.path.join(home_folder,"Pipeline/ref",index_base)+"_STAR"    
    status_file.close()
    print(str(LSF_run+[os.path.join(home_folder,"Pipeline/tools/STAR-master/source/STAR"),"--runThreadN",str(STAR_proc),"--runMode","genomeGenerate","--genomeDir",index_dir,"--genomeFastaFiles",os.path.join(home_folder,"Pipeline/ref",fasta_filename)]))
    os.mkdir(index_dir)
    subprocess.call(LSF_run+[os.path.join(home_folder,"Pipeline/tools/STAR-master/source/STAR"),"--runThreadN",str(STAR_proc),"--runMode","genomeGenerate","--genomeDir",index_dir,"--genomeFastaFiles",os.path.join(home_folder,"Pipeline/ref",fasta_filename)])
    if LSF!=[]:
        while True:
            try:
                STAR_out=open(os.path.join(home_folder,"Pipeline","ref","STAR.out"),"r")
                STAR_out.close()
                os.system("cp "+os.path.join(home_folder,"Pipeline","ref","STAR.out")+" "+os.path.join(project_dir,"logs"))
                os.system("cp "+os.path.join(home_folder,"Pipeline","ref","STAR.err")+" "+os.path.join(project_dir,"logs"))
                os.system("rm "+os.path.join(home_folder,"Pipeline","ref","STAR.out"))
                os.system("rm "+os.path.join(home_folder,"Pipeline","ref","STAR.err"))
                break
            except:
                continue
    reference_file.write(species+"\t"+assembly+"\t"+index_dir+"\n")
                           
def prepare_classification(gtf_filename):
    classification_filename=gtf_filename[:-4]
    gtf_file=open(gtf_filename,"r")
    classification_file=open(classification_filename,"w")
    current_gene="none"
    classification_file.write("Gene\t")
    classification_file.write("Biotype\n")
    for line in gtf_file:
        split=re.split("\s+",line)
        for i in xrange(len(split)):
            split_element=split[i]
            if split_element=="gene_name":
                gene=split[i+1]
                gene=gene[1:-2]
            if split_element=="gene_biotype":
                gene_type=split[i+1]
                gene_type=gene_type[1:-2]
        if current_gene!=gene:
            if current_gene!="none":
                classification_file.write(current_gene+"\t")
                classification_file.write(current_gene_type+"\n")
            current_gene_type=gene_type
            current_gene=gene
    classification_file.write(current_gene+"\t")
    classification_file.write(current_gene_type+"\n")
  
def download_file(download_url,target):
    subprocess.call(["wget","--no-check-certificate","--output-document",target,download_url])
    print("Completed")
    
def prepare_gtf(gtf_filename):
    gtf_file=open(gtf_filename,"r")
    ref_gtf_file=open(gtf_filename[:-4]+"_ref.gtf","w")
    mask_gtf_file=open(gtf_filename[:-4]+"_mask.gtf","w")
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Preparing transcriptome reference...\n")
    status_file.close()
    for line in gtf_file:
        split=re.split("\s+",line)
        entry_type=split[2]
        gene_type=split[1]
        if entry_type=="exon" or entry_type=="CDS":
            if len(split)>23:
                gene_type2=split[23]
                gene_type2=gene_type2[1:-2]
            else:
                gene_type2="none"
            if gene_type=="rRNA" or gene_type=="Mt_tRNA" or gene_type=="tRNA" or gene_type2=="rRNA" or gene_type2=="Mt_tRNA" or gene_type2=="tRNA":
                mask_gtf_file.write(line)
            else:
                ref_gtf_file.write(line)
    ref_gtf_file.close()
    mask_gtf_file.close()
    gtf_file.close()
    os.system("rm "+gtf_filename)
    
def get_reference_fa(species,assembly,aligner,fasta,reference_file,LSF):
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Downloading genome reference...\n")
    status_file.close()
    parse_object = urlparse(fasta)
    filename=basename(parse_object.path)
    download_file(fasta,filename)
    os.system('gunzip *.gz')
    index_dir=os.path.join(home_folder,"Pipeline/ref",assembly)
    fasta_path=os.path.join(home_folder,"Pipeline/ref",filename[:-3])
    reference_file.write(species+"\t"+assembly+"\t"+fasta_path+"\n")
    subprocess.call([os.path.join(home_folder,"beta/faToTwoBit"),filename[:-3],filename[:-5]+"2bit"])
    reference_file.write(species+"\t"+assembly+"\t"+os.path.join(home_folder,'Pipeline/ref',filename[:-5]+"2bit")+"\n")
    if aligner=="tophat":
        build_bowtie_index(species,assembly,filename[:-3],assembly,reference_file,LSF)
    else:
        build_STAR_index(species,assembly,filename[:-3],assembly,reference_file,LSF)

def get_reference_gtf(species,assembly,gtf,reference_file):
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Downloading transcriptome reference...\n")
    status_file.close()
    parse_object = urlparse(gtf)
    filename=basename(parse_object.path)
    download_file(gtf,filename)
    os.system('gunzip *.gz')
    prepare_gtf(filename[:-3])
    reference_file.write(species+"\t"+assembly+"\t"+os.path.join(home_folder,'Pipeline/ref',filename[:-7]+"_ref.gtf")+"\n")
    reference_file.write(species+"\t"+assembly+"\t"+os.path.join(home_folder,'Pipeline/ref',filename[:-7]+"_mask.gtf")+"\n")
    prepare_classification(filename[:-7]+"_ref.gtf")
    reference_file.write(species+"\t"+assembly+"\t"+os.path.join(home_folder,'Pipeline/ref',filename[:-7]+"_ref")+"\n")
    
def install_STAR(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"STAR.zip")
    subprocess.call(["unzip","STAR.zip"])
    os.system("rm "+"STAR.zip")
    os.chdir(os.path.join(home_folder,"Pipeline/tools/STAR-master/source"))
    subprocess.call(["make","STAR"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools/STAR-master/source/STAR\t'))
    tools_file.write(version+"\n")

def install_TopHat(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"TopHat.tar.gz")
    subprocess.call(["tar","-zxvf","TopHat.tar.gz"])
    os.system("rm "+"TopHat.tar.gz")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-7]
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',folder_name)+'\t')
    tools_file.write(version+"\n")
    
def install_Cufflinks(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"Cufflinks.tar.gz")
    subprocess.call(["tar","-zxvf","Cufflinks.tar.gz"])
    os.system("rm "+"Cufflinks.tar.gz")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-7]
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',folder_name)+'\t')
    tools_file.write(version+"\n")

def install_CNCI(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"CNCI-master.zip")
    subprocess.call(["unzip","CNCI-master.zip"])
    os.system("rm "+"CNCI-master.zip")
    os.chdir(os.path.join(home_folder,"Pipeline/tools/CNCI-master"))
    subprocess.call(["unzip","libsvm-3.0.zip"])
    os.chdir(os.path.join(home_folder,"Pipeline/tools/CNCI-master/libsvm-3.0"))
    subprocess.call(["make"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools/CNCI-master/CNCI.py'+'\t'))
    tools_file.write(version+"\n")

def install_Primer3(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"Primer3.tar.gz")
    subprocess.call(["tar","-zxvf","Primer3.tar.gz"])
    os.system("rm "+"Primer3.tar.gz")
    os.chdir(os.path.join(home_folder,'Pipeline/tools/primer3-'+version+'/src'))
    subprocess.call(["make","all"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools/primer3-'+version+'/src/primer3_core\t'))
    tools_file.write(version+"\n")

def install_samtools(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"samtools.tar.bz2")
    subprocess.call(["tar","-xvjf","samtools.tar.bz2"])
    os.system("rm "+"samtools.tar.bz2")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-8]
    os.chdir(os.path.join(home_folder,'Pipeline/tools',folder_name))
    subprocess.call(["make"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',folder_name)+'\t')
    tools_file.write(version+"\n")

def install_bowtie(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"bowtie2.zip")
    subprocess.call(["unzip","bowtie2.zip"])
    os.system("rm "+"bowtie2.zip")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-4]
    os.chdir(os.path.join(home_folder,'Pipeline/tools',"bowtie2-"+version))
    subprocess.call(["make"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',"bowtie2-"+version)+'\t')
    tools_file.write(version+"\n")

def install_ref(species,assembly,fasta,gtf,aligner,status,LSF):
    exist=0
    os.chdir(os.path.join(home_folder,"Pipeline/ref"))
    tophat_installed=0
    STAR_installed=0
    if status=="new":
        reference_file=open("reference.txt","w")
    else:
        reference_file=open("reference.txt","r")
        for line in reference_file:
            ref_line_split=re.split("\s+",line)
            if ref_line_split[0]==species and ref_line_split[1]==assembly:
                exist=1
                if line[-5:-1]=="STAR":
                    STAR_installed=1
                if line[-7:-1]=="bowtie":
                    tophat_installed=1
        reference_file.close()
        reference_file=open("reference.txt","a")
    if species=="human" and assembly=="GRCh38":
        if exist==0:
            get_reference_fa(species,assembly,aligner,fasta,reference_file,LSF)
            get_reference_gtf(species,assembly,gtf,reference_file)
        else:
            if aligner=="tophat" and tophat_installed==0:
                build_bowtie_index(species,assembly,'Homo_sapiens.GRCh38.dna.primary_assembly.fa','GRCh38',reference_file,LSF)
                tophat_installed=1
            if aligner=="STAR" and STAR_installed==0:
                build_STAR_index(species,assembly,'Homo_sapiens.GRCh38.dna.primary_assembly.fa','GRCh38',reference_file,LSF)
                STAR_installed=1
    reference_file.close()
    
def install_SRA(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"SRA_tools.tar.gz")
    subprocess.call(["tar","-zxvf","SRA_tools.tar.gz"])
    os.system("rm "+"SRA_tools.tar.gz")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-7]
    tools_file.write(os.path.join(home_folder,"Pipeline/tools",folder_name,"bin","fastq-dump")+'\t'+version+"\n")

def install_R(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"R.tar.gz")
    subprocess.call(["tar","-zxvf","R.tar.gz"])
    os.system("rm "+"R.tar.gz")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-7]
    os.chdir(os.path.join(home_folder,"Pipeline/tools",folder_name))
    subprocess.call(["./configure","--with-readline=no","--with-x=no"])
    subprocess.call(["make"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',folder_name,'bin/R')+'\t')
    tools_file.write(version+"\n")
    packages_file=open(os.path.join(home_folder,'Pipeline/tools/packages.R'),"w")
    packages_file.write('source("http://bioconductor.org/biocLite.R")\nbiocLite("DESeq2",ask=FALSE)\nbiocLite("edgeR",ask=FALSE)\nbiocLite("DESeq2",ask=FALSE)\n')
    packages_file.close()
    subprocess.call([os.path.join(home_folder,'Pipeline/tools',folder_name,'bin/R'),"CMD","BATCH",os.path.join(home_folder,'Pipeline/tools/packages.R')])

def install_HTSeq(link,version,tools_file):
    os.chdir(os.path.join(home_folder,"Pipeline/tools"))
    download_file(link,"HTSeq.tar.gz")
    subprocess.call(["tar","-zxvf","HTSeq.tar.gz"])
    os.system("rm "+"HTSeq.tar.gz")
    parse_object = urlparse(link)
    folder_name=basename(parse_object.path)[:-7]
    os.chdir(os.path.join(home_folder,"Pipeline/tools",folder_name))
    subprocess.call(["python","setup.py","install","--user"])
    tools_file.write(os.path.join(home_folder,'Pipeline/tools',folder_name,'scripts/htseq-count')+'\t')
    tools_file.write(version+"\n")

def check_update(tools_file_name,update_file_name):
    global os_name
    update_file=open(update_file_name,"r")
    tools_file=open(tools_file_name,"r")
    tools_lines=tools_file.readlines()
    tools_file.close()
    tools_file=open(tools_file_name,"w")
    tophat_line=update_file.next()
    tophat_link=re.split("\s+",tophat_line)[0]
    tophat_version=re.split("\s+",tophat_line)[1]
    ref_tophat_version=re.split("\s+",tools_lines[0])[1]
    if ref_tophat_version!=tophat_version:
        install_TopHat(tophat_link,tophat_version,tools_file)
        ref_path=re.split("\s+",tools_lines[0])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-1])
    else:
        tools_file.write(tools_lines[0])
    STAR_line=update_file.next()
    STAR_link=re.split("\s+",STAR_line)[0]
    STAR_version=re.split("\s+",STAR_line)[1]
    ref_STAR_version=re.split("\s+",tools_lines[1])[1]
    if ref_STAR_version!=STAR_version:
        install_STAR(STAR_link,STAR_version,tools_file)
        ref_path=re.split("\s+",tools_lines[1])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-3])
    else:
        tools_file.write(tools_lines[1])
    Cufflinks_line=update_file.next()
    Cufflinks_link=re.split("\s+",Cufflinks_line)[0]
    Cufflinks_version=re.split("\s+",Cufflinks_line)[1]
    ref_Cufflinks_version=re.split("\s+",tools_lines[2])[1]
    if ref_Cufflinks_version!=Cufflinks_version:
        install_Cufflinks(Cufflinks_link,Cufflinks_version,tools_file)
        ref_path=re.split("\s+",tools_lines[2])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-1])
    else:
        tools_file.write(tools_lines[2])
    CNCI_line=update_file.next()
    CNCI_link=re.split("\s+",CNCI_line)[0]
    CNCI_version=re.split("\s+",CNCI_line)[1]
    ref_CNCI_version=re.split("\s+",tools_lines[3])[1]
    if ref_CNCI_version!=CNCI_version:
        install_CNCI(CNCI_link,CNCI_version,tools_file)
        ref_path=re.split("\s+",tools_lines[3])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-2])
    else:
        tools_file.write(tools_lines[3])
    Primer3_line=update_file.next()
    Primer3_link=re.split("\s+",Primer3_line)[0]
    Primer3_version=re.split("\s+",Primer3_line)[1]
    ref_Primer3_version=re.split("\s+",tools_lines[4])[1]
    if ref_Primer3_version!=Primer3_version:
        install_Primer3(Primer3_link,Primer3_version,tools_file)
        ref_path=re.split("\s+",tools_lines[4])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-3])
    else:
        tools_file.write(tools_lines[4])
    samtools_line=update_file.next()
    samtools_link=re.split("\s+",samtools_line)[0]
    samtools_version=re.split("\s+",samtools_line)[1]
    ref_samtools_version=re.split("\s+",tools_lines[5])[1]
    if ref_samtools_version!=samtools_version:
        install_samtools(samtools_link,samtools_version,tools_file)
        ref_path=re.split("\s+",tools_lines[5])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-1])
    else:
        tools_file.write(tools_lines[5])
    bowtie_line=update_file.next()
    bowtie_link=re.split("\s+",bowtie_line)[0]
    bowtie_version=re.split("\s+",bowtie_line)[1]
    ref_bowtie_version=re.split("\s+",tools_lines[6])[1]
    if ref_bowtie_version!=bowtie_version:
        install_bowtie(bowtie_link,bowtie_version,tools_file)
        ref_path=re.split("\s+",tools_lines[6])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-1])
    else:
        tools_file.write(tools_lines[6])
    SRA_line=update_file.next()
    SRA_link=re.split("\s+",SRA_line)[0]
    if os_name=='CentOS' or os_name=='Red Hat Enterprise Linux Server' or os_name=='Fedora':
        SRA_link=re.split("\|",SRA_link)[0]
    else:
        if os_name=='Ubuntu':
            SRA_link=re.split("\|",SRA_link)[1]
    SRA_version=re.split("\s+",SRA_line)[1]
    ref_SRA_version=re.split("\s+",tools_lines[7])[1]
    if ref_SRA_version!=SRA_version:
        install_SRA(SRA_link,SRA_version,tools_file)
        ref_path=re.split("\s+",tools_lines[7])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-3])
    else:
        tools_file.write(tools_lines[7])
    HTSeq_line=update_file.next()
    HTSeq_link=re.split("\s+",HTSeq_line)[0]
    HTSeq_version=re.split("\s+",HTSeq_line)[1]
    ref_HTSeq_version=re.split("\s+",tools_lines[8])[1]
    if ref_HTSeq_version!=HTSeq_version:
	install_HTSeq(HTSeq_link,HTSeq_version,tools_file)
	ref_path=re.split("\s+",tools_lines[8])[0]
	os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-3])
    else:
        tools_file.write(tools_lines[8])
    R_line=update_file.next()
    R_link=re.split("\s+",R_line)[0]
    R_version=re.split("\s+",R_line)[1]
    ref_R_version=re.split("\s+",tools_lines[9])[1]
    if ref_R_version!=R_version:
        install_R(R_link,R_version,tools_file)
        ref_path=re.split("\s+",tools_lines[9])[0]
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        os.system("rm -r "+re.split(os.sep,ref_path)[-3])
    else:
        tools_file.write(tools_lines[9])
    tools_file.close()

def main():
    project_info=get_project_info(project_lines)
    species=project_info[0]
    assembly=project_info[1]
    fasta=project_info[2]
    gtf=project_info[3]
    aligner=project_info[4]
    LSF=project_info[5]
    global os_name
    if not os.path.isfile(os.path.join(home_folder,"Pipeline/tools","tools.txt")):
        status_file=open(os.path.join(project_dir,"status.txt"),"a")
        status_file.write("Downloading and installing software...\n")
        status_file.close()
        os.mkdir(os.path.join(home_folder,"Pipeline"))
        os.mkdir(os.path.join(home_folder,"Pipeline/tools"))
        os.chdir(os.path.join(home_folder,"Pipeline/tools"))
        install_essentials()
        download_file("http://psychiatry.med.miami.edu/documents/research/CANE_update_v1.0.txt","CANE_update.txt")
        update_file=open(os.path.join(home_folder,"Pipeline/tools/CANE_update.txt"),"r")
        tophat_line=update_file.next()
        tophat_link=re.split("\s+",tophat_line)[0]
        tophat_version=re.split("\s+",tophat_line)[1]
        tools_file=open("tools.txt","w")
        install_TopHat(tophat_link,tophat_version,tools_file)
        STAR_line=update_file.next()
        STAR_link=re.split("\s+",STAR_line)[0]
        STAR_version=re.split("\s+",STAR_line)[1]
        install_STAR(STAR_link,STAR_version,tools_file)
        Cufflinks_line=update_file.next()
        Cufflinks_link=re.split("\s+",Cufflinks_line)[0]
        Cufflinks_version=re.split("\s+",Cufflinks_line)[1]
        install_Cufflinks(Cufflinks_link,Cufflinks_version,tools_file)
        CNCI_line=update_file.next()
        CNCI_link=re.split("\s+",CNCI_line)[0]
        CNCI_version=re.split("\s+",CNCI_line)[1]
        install_CNCI(CNCI_link,CNCI_version,tools_file)
        Primer3_line=update_file.next()
        Primer3_link=re.split("\s+",Primer3_line)[0]
        Primer3_version=re.split("\s+",Primer3_line)[1]
        install_Primer3(Primer3_link,Primer3_version,tools_file)
        samtools_line=update_file.next()
        samtools_link=re.split("\s+",samtools_line)[0]
        samtools_version=re.split("\s+",samtools_line)[1]
        install_samtools(samtools_link,samtools_version,tools_file)
        bowtie_line=update_file.next()
        bowtie_link=re.split("\s+",bowtie_line)[0]
        bowtie_version=re.split("\s+",bowtie_line)[1]
        install_bowtie(bowtie_link,bowtie_version,tools_file)
        SRA_line=update_file.next()
        SRA_link=re.split("\s+",SRA_line)[0]
        if os_name=='CentOS' or os_name=='Red Hat Enterprise Linux Server' or os_name=='Fedora':
            SRA_link=re.split("\|",SRA_link)[0]
        else:
            if os_name=='Ubuntu':
                SRA_link=re.split("\|",SRA_link)[1]
            else:
                SRA_link=re.split("\|",SRA_link)[0]
        SRA_version=re.split("\s+",SRA_line)[1]
        install_SRA(SRA_link,SRA_version,tools_file)
        HTSeq_line=update_file.next()
        HTSeq_link=re.split("\s+",HTSeq_line)[0]
        HTSeq_version=re.split("\s+",HTSeq_line)[1]
	install_HTSeq(HTSeq_link,HTSeq_version,tools_file)
	R_line=update_file.next()
        R_link=re.split("\s+",R_line)[0]
        R_version=re.split("\s+",R_line)[1]
        install_R(R_link,R_version,tools_file)
        tools_file.close()
        os.system("chmod -R 750 "+os.path.join(home_folder,"Pipeline/tools"))
    if not os.path.isfile(os.path.join(home_folder,"Pipeline/ref","reference.txt")):
        os.mkdir(os.path.join(home_folder,"Pipeline/ref"))
        os.chdir(os.path.join(home_folder,"Pipeline/ref"))
        install_ref(species,assembly,fasta,gtf,aligner,"new",LSF)
    else:
        download_file("http://psychiatry.med.miami.edu/documents/research/CANE_update_v1.0.txt","CANE_update.txt")
        check_update(os.path.join(home_folder,"Pipeline/tools","tools.txt"),os.path.join(home_folder,"Pipeline/tools/CANE_update.txt"))
        os.system("chmod -R 750 "+os.path.join(home_folder,"Pipeline/tools"))
        os.chdir(os.path.join(home_folder,"Pipeline/ref"))
        install_ref(species,assembly,fasta,gtf,aligner,"exist",LSF)

os.system("chmod -R 750 "+os.path.join(home_folder,"beta"))
main()
install_status=verify_install()
status_file=open(os.path.join(project_dir,"status.txt"),"a")
if install_status==False:
    install_status=verify_install()
    if install_status==False:
        status_file.write("Installation failed\n")
    else:
        status_file.write("Installation successful\n")
else:
    status_file.write("Installation successful\n")
status_file.close()
os.system("chmod -R 750 "+os.path.join(home_folder,"Pipeline/ref"))
subprocess.call(["python",os.path.join(home_folder,"beta/main.py"),"-i",project_file_name,"-f",home_folder])
