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
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
folders=[]
(options, args) = parser.parse_args()
folder=options.folder

os.chdir(folder)
subprocess.call(["tar","-zcvf","logs.tar.gz","logs"])
tar_log=open(os.path.join(folder,"tar.log"),"w")
tar_log.write("Logs compressed!")
tar_log.close()

