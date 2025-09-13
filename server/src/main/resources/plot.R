#Packages
library(Seurat)
library(tidyverse)
library(gridExtra)
library(plotly)

# Parse args
args <- commandArgs(trailingOnly = TRUE)
gene <- args[[1]]
timepoint <- args[[2]]

# Build a safe stem: e.g., "pou5f3_115hpf"
safe <- function(x) gsub("[^A-Za-z0-9._-]", "_", x)
stem <- paste0(safe(gene), "_", safe(timepoint))

# Unique temp file in the current working dir (or use tempdir())
outfile <- tempfile(pattern = paste0(stem, "_"), tmpdir = getwd(), fileext = ".png")

#note: change this path as needed to fit where your files are locally. 
top.dir <- "C:/Users/Sebastian/Documents/Research/KillifishEmbryogenesis_scRNAseq"
#if on lucy:
#top.dir <- "C:/Users/sydsat/OneDrive - UW/KillifishEmbryogenesis_scRNAseq"

#Color schemes
nice.cols <- c("grey","#7B2C7E","#BA4281","#F36875","#FFA974","#FCEED0")

#The dataframe that I used to update LOC numbers as best as I could. This dataframe is helpful to reference when you need to look up a LOC number / see if a gene exists in the data set. 
NCBI.anno <- read_table(paste0(top.dir, "/Csvs/GCF_027789165.1_UI_Nfuz_MZM_1.0_feature_table.txt"))

#A list of individually clustered timepoints (mito genes have been removed, QC cleaning has been performed, and only mito-high cells are kept (except for the exception of the EVL and YSL)). Good if you want to look at expression within an individual timepoint. Do not compare expression across timepoints, as each timepoint is normalized to itself.
pip.list <- readRDS(paste0(top.dir, "/Rds/pip.list.clean.final.Rds"))

pip.list[[4]]$orig.ident <- "115hpf"

#Can be useful to make each timepoint and individual
#pip52 <- pip.list[[1]]
#pip72 <- pip.list[[2]]
#pip96 <- pip.list[[3]]
#pip115 <- pip.list[[4]]

#This is a single seurat object that contains all 4 timepoints merged together. Good if you want to look at trends over time because the expression is normalized across timepoints. 
merge <- readRDS(paste0(top.dir, "/Rds/merge.allTimepoints.clean.final.rds"))

names(pip.list) <- c("52hpf", "72hpf", "96hpf", "115hpf")

print(names(pip.list))

if (timepoint %in% names(pip.list)) {
    obj <- pip.list[[timepoint]]
} else {
    stop(paste("Timepoint not found:", timepoint))
}

p <- FeaturePlot(obj, features = gene, order = TRUE) +
  scale_color_gradientn(colours = nice.cols) +
  ggtitle(paste0(gene, " ", unique(obj$orig.ident))) +
  coord_fixed(ratio = 1)


  png(filename = outfile, width = 1000, height = 1000, res = 144)
print(p)
dev.off()

cat(outfile, file = stdout())  # optional: print path so caller can read it