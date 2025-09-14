#Packages
library(Seurat)
library(tidyverse)
library(gridExtra)
library(plotly)

args <- commandArgs(trailingOnly = TRUE)
gene <- args[[1]]
timepoint <- args[[2]]

flush_now <- function() {
  try(flush.console(), silent = TRUE)
}

# Announce start (0%)
cat("PROGRESS: 5\n"); flush_now()

# Build a safe stem
safe <- function(x) gsub("[^A-Za-z0-9._-]", "_", x)
stem <- paste0(safe(gene), "_", safe(timepoint))
outfile <- tempfile(pattern = paste0(stem, "_"), tmpdir = getwd(), fileext = ".png")

#note: change this path as needed to fit where your files are locally.
top.dir <- "C:/Users/Sebastian/Documents/Research/KillifishEmbryogenesis_scRNAseq"

nice.cols <- c("grey","#7B2C7E","#BA4281","#F36875","#FFA974","#FCEED0")

cat("PROGRESS: 15\n"); flush_now()
NCBI.anno <- read_table(paste0(top.dir, "/Csvs/GCF_027789165.1_UI_Nfuz_MZM_1.0_feature_table.txt"))

cat("PROGRESS: 30\n"); flush_now()
pip.list <- readRDS(paste0(top.dir, "/Rds/pip.list.clean.final.Rds"))

pip.list[[4]]$orig.ident <- "115hpf"
names(pip.list) <- c("52hpf", "72hpf", "96hpf", "115hpf")

cat("PROGRESS: 45\n"); flush_now()
if (timepoint %in% names(pip.list)) {
  obj <- pip.list[[timepoint]]
} else {
  stop(paste("Timepoint not found:", timepoint))
}

cat("PROGRESS: 65\n"); flush_now()
p <- FeaturePlot(obj, features = gene, order = TRUE) +
  scale_color_gradientn(colours = nice.cols) +
  ggtitle(paste0(gene, " ", unique(obj$orig.ident))) +
  coord_fixed(ratio = 1)

cat("PROGRESS: 85\n"); flush_now()
png(filename = outfile, width = 1000, height = 1000, res = 144)
print(p)
dev.off()

# Finalize
cat("PROGRESS: 100\n"); flush_now()
cat(paste0("OUTFILE: ", outfile, "\n")); flush_now()