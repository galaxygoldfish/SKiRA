# R
repo <- "https://cloud.r-project.org"
options(repos = c(CRAN = repo))

libDir <- "C:/Users/Sebastian/Downloads/RLib"
dir.create(libDir, showWarnings = FALSE, recursive = TRUE)
.libPaths(c(libDir, .libPaths()))   # ensure R can find packages installed into libDir

if (!requireNamespace("BiocManager", quietly = TRUE)) {
  install.packages("BiocManager", lib = libDir)
}
if (!requireNamespace("BiocManager", quietly = TRUE)) {
  stop("Failed to install BiocManager into `libDir`")
}

if (!requireNamespace("viridis", quietly = TRUE)) {
  install.packages("viridis", lib = libDir)
}
suppressWarnings(suppressMessages(library("viridis", lib.loc = libDir, character.only = TRUE)))

required_packages <- c("Seurat", "tidyverse", "gridExtra", "plotly", "jsonlite")
for (pkg in required_packages) {
  if (!requireNamespace(pkg, quietly = TRUE)) {
    BiocManager::install(pkg, lib = libDir)
  }
  suppressWarnings(suppressMessages(library(pkg, character.only = TRUE, lib.loc = libDir)))
}

# Read arguments
args <- commandArgs(trailingOnly = TRUE)
top.dir <- if (length(args) >= 1 && nzchar(args[[1]])) args[[1]] else {
  # Fallback to the same default directory used by the app (adjust as needed)
  "C:/Users/Sebastian/Documents/Research/KillifishEmbryogenesis_scRNAseq"
}

top.dir <- gsub("\\\\", "/", top.dir)

obj.path <- file.path(top.dir, "merge.rds")
if (!file.exists(obj.path)) {
  stop(paste0("Merged Seurat object not found at: ", obj.path))
}

merge <- readRDS(obj.path)

# Extract timepoints and genes
timepoints <- unique(as.character(merge$orig.ident))
timepoints[4] <- "115hpf"

# Add 'all' as the first timepoint option for UI selection
timepoints <- c("All timepoints", timepoints)

genes <- rownames(merge)

# Emit compact JSON to stdout
cat(toJSON(list(genes = genes, timepoints = timepoints), auto_unbox = TRUE))