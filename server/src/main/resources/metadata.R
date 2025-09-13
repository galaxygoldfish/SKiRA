# R
suppressWarnings(suppressMessages({
  library(Seurat)
  library(jsonlite)
}))

# Read arguments
args <- commandArgs(trailingOnly = TRUE)
top.dir <- if (length(args) >= 1 && nzchar(args[[1]])) args[[1]] else {
  # Fallback to the same default directory used by the app (adjust as needed)
  "C:/Users/Sebastian/Documents/Research/KillifishEmbryogenesis_scRNAseq"
}

top.dir <- gsub("\\\\", "/", top.dir)

obj.path <- file.path(top.dir, "Rds", "merge.allTimepoints.clean.final.rds")
if (!file.exists(obj.path)) {
  stop(paste0("Merged Seurat object not found at: ", obj.path))
}

merge <- readRDS(obj.path)

# Extract timepoints and genes
timepoints <- unique(as.character(merge$orig.ident))
timepoints[4] <- "115hpf"

genes <- rownames(merge)

# Emit compact JSON to stdout
cat(toJSON(list(genes = genes, timepoints = timepoints), auto_unbox = TRUE))