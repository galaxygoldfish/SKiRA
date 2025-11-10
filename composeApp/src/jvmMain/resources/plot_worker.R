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
if (!requireNamespace("ggrepel", quietly = TRUE)) {
  suppressWarnings(suppressMessages(install.packages("ggrepel", lib = libDir)))
}
suppressWarnings(suppressMessages(library("ggrepel", lib.loc = libDir, character.only = TRUE)))
suppressWarnings(suppressMessages(library("viridis", lib.loc = libDir, character.only = TRUE)))

required_packages <- c("Seurat", "tidyverse", "gridExtra", "plotly", "jsonlite", "FNN")
for (pkg in required_packages) {
  if (!requireNamespace(pkg, quietly = TRUE)) {
    BiocManager::install(pkg, lib = libDir)
  }
  suppressWarnings(suppressMessages(library(pkg, character.only = TRUE, lib.loc = libDir)))
}
flush_now <- function() {
  try(flush.console(), silent = TRUE)
}

cat("PROGRESS: 1\n"); flush_now()

# Make this hosted in the future so that we can download automatically and then verify file is there before running
# That will be part of loading metadata
args <- commandArgs(trailingOnly = TRUE)
top.dir <- if (length(args) >= 1 && nzchar(args[[1]])) args[[1]] else {
  "C:/Users/Sebastian/Documents/test-downloade/KillifishEmbryogenesis_scRNAseq"
}

top.dir <- "C:/Users/Sebastian/Downloads/OneDrive_1_11-6-2025"

# plasma = nice.cols
plasma_colors <- c("grey","#7B2C7E","#BA4281","#F36875","#FFA974","#FCEED0")
inferno_colors <- c("grey","#FCA50A","#DC513B","#942667","#420A68","#000004")
viridis_colors <- c("grey","#FDE725","#79D151","#2A788E","#414487","#440154")
magma_colors <- c("grey","#FCFDBF","#FE9F6D","#8D2981","#3B0F70","#000004")

rainbow_color <- c("#901C43", "#C64B57", "#DFB451", "#BBDE92", "#85B295", "#9DC5DB", "#4A87B7", "#592277")

cat("PROGRESS: 15\n"); flush_now()
pip.list <- readRDS(paste0(top.dir, "/pip.list.annotated.rds"))
merge <- readRDS(paste0(top.dir, "/merge.annotated.rds"))

pip.list[[4]]$orig.ident <- "115hpf"
names(pip.list) <- c("52hpf", "72hpf", "96hpf", "115hpf")
time_to_stage_map <- c(
  "52hpf" = "dispersed",
  "72hpf" = "incipient aggregate",
  "96hpf" = "tailbud",
  "115hpf" = "8-somite",
  "all" = ""
)

cat("PROGRESS: 20\n"); flush_now()

# Ready to accept requests
con <- file("stdin", open = "r")

repeat {
  line <- tryCatch(readLines(con, n = 1, warn = FALSE), error = function(e) character())
  if (length(line) == 0) {
    # EOF or broken pipe -> exit
    break
  }
  line <- trimws(line)
  if (identical(line, "")) next
  if (identical(line, "PING")) {
    cat("PONG\n"); flush_now()
    next
  }
  req <- tryCatch(jsonlite::fromJSON(line), error = function(e) NULL)
  required_fields <- c("gene", "timepoint", "dpiExpr", "dpiCType", "colorExpr", "colorCType", "labelsExpr", "labelsDim")
  if (is.null(req) || any(sapply(required_fields, function(f) is.null(req[[f]])))) {
    cat("ERROR: bad request\n"); flush_now()
    next
  }

  gene <- as.character(req$gene)
  timepoint <- as.character(req$timepoint)
  dpiExpr <- as.integer(req$dpiExpr)
  dpiCType <- as.integer(req$dpiCType)
  colorExpr <- as.character(req$colorExpr)
  colorCType <- as.integer(req$colorCType)
  labelsExpr <- as.logical(req$labelsExpr)
  labelsDim <- as.logical(req$labelsDim)

  cat("PROGRESS: 40\n"); flush_now()

  # Support 'all' timepoint for merged object
  if (identical(timepoint, "All timepoints")) {
    obj <- merge
    timepoint_label <- "all"
  } else {
    if (!timepoint %in% names(pip.list)) {
      cat(paste0("ERROR: Timepoint not found: ", timepoint, "\n")); flush_now()
      next
    }
    obj <- pip.list[[timepoint]]
    timepoint_label <- unique(obj$orig.ident)
  }

  # Set the active identity to "cell.types" for both FeaturePlot labels and DimPlot grouping
  grouping_column <- NULL

  # Priority 1: Check for the desired 'cell.type' column (from the merged object)
  if ("cell.type" %in% names(obj@meta.data)) {
    grouping_column <- "cell.type"
  }

  # Priority 2: Check for a reasonable fallback, like the active identity (which often holds the labels)
  # Note: active.ident is technically stored separately but can be retrieved via Idents()
  # It's safest to check for a named column first, or just use Idents() for the grouping variable.
  # Given the image, the *labels themselves* are likely stored in the active identity.
  # Let's check for a column that looks like the active identity labels if 'cell.type' isn't there.
  # However, for robustness, we'll try 'seurat_clusters' as a known Seurat column name.

  if (is.null(grouping_column) && "seurat_clusters" %in% names(obj@meta.data)) {
    cat("WARNING: 'cell.type' missing. Falling back to 'seurat_clusters'.\n"); flush_now()
    grouping_column <- "seurat_clusters"
  }

  # If a grouping column was found, set the identity and ensure it's a factor
  if (!is.null(grouping_column)) {
    obj <- SetIdent(obj, value = grouping_column)
    # Convert to factor just to be safe, though Seurat often handles this
    obj@meta.data[[grouping_column]] <- factor(obj@meta.data[[grouping_column]])

  } else {
    # Final fallback: Use the current active identity, whatever it is
    grouping_column <- "active.ident"
    cat("WARNING: Neither 'cell.type' nor 'seurat_clusters' found. Using current 'active.ident'.\n"); flush_now()
  }


  safe <- function(x) gsub("[^A-Za-z0-9._-]", "_", x)
  stem <- paste0(safe(gene), "_", safe(timepoint))

  # Decide output directory: system temp
  out.dir <- tempdir()
  if (!dir.exists(out.dir)) {
    dir.create(out.dir, recursive = TRUE, showWarnings = FALSE)
  }

  outfile_feature <- tempfile(pattern = paste0(stem, "_feature_"), tmpdir = out.dir, fileext = ".png")
  outfile_dim     <- tempfile(pattern = paste0(stem, "_dim_"),     tmpdir = out.dir, fileext = ".png")

  cat("PROGRESS: 65\n"); flush_now()

  # Force fixed 15x15 square limits (centered at 0)
  # Use range [-7.5, 7.5] for both x and y
  lim_square <- c(-20, 20)

  # FeaturePlot for the requested gene

  # compute title: if all timepoints, only gene in bold italic, else include stage and label
  title_expr <- if (identical(timepoint_label, "all")) {
    bquote(bold(italic(.(gene))))
  } else {
    bquote(bold(italic(.(gene)) ~ "-" ~ .(time_to_stage_map[timepoint_label]) ~ "(" * .(timepoint_label) * ")"))
  }

  p_feature <- {
    # FeaturePlot labels (if requested via labelsExpr) now uses the "cell.types" identity set above.
    base <- FeaturePlot(obj, features = gene, reduction = "umap", order = TRUE, label = labelsExpr, repel = TRUE) +
      scale_color_gradientn(
        colours = switch(
          colorExpr,
          "viridis" = viridis_colors,
          "magma" = magma_colors,
          "inferno" = inferno_colors,
          "plasma" = plasma_colors,
          viridis_colors
        )
      ) +
      ggtitle(title_expr)

    base <- base + (if (!is.null(lim_square)) {
      coord_fixed(ratio = 1, xlim = lim_square, ylim = lim_square, expand = FALSE)
    } else {
      coord_fixed(ratio = 1)
    })
  }

  # --- Robust DimPlot Generation (p_dim) ---
  p_dim <- tryCatch({

    if ("cell.type" %in% names(obj@meta.data)) {
      obj <- SetIdent(obj, value = "cell.type")

      # NEW: Ensure the 'cell.type' column is a factor for robust ordering/coloring
      obj@meta.data$cell.type <- factor(obj@meta.data$cell.type)
    } else {
      # CRITICAL: Handle the case where the column is missing
      cat("ERROR: Metadata column 'cell.type' is missing in the Seurat object.\n"); flush_now()
      # Proceed, but DimPlot will likely fail to group, so we rely on fallback
    }




    # R
    # Use the current Seurat object (`obj`) and robustly detect the cell-type metadata column
    umap_coords <- Embeddings(obj, reduction = "umap")
    metadata <- obj@meta.data

    # pick a plausible cell-type column (try dot, underscore, or the grouping_column)
    cell_col <- if ("cell.type" %in% names(metadata)) {
      "cell.type"
    } else if (!is.null(grouping_column) && grouping_column %in% names(metadata)) {
      grouping_column
    } else {
      stop("No cell-type metadata column found (tried 'cell.type', 'cell_type' and grouping_column).")
    }



    # Attempt to generate the complex DimPlot with ggrepel
    p <- DimPlot(obj, repel = FALSE, reduction = "umap", label = TRUE, cols = custom_palette, group.by = grouping_column) + # Disable default labels, rely on ggrepel
      ggtitle(paste0("Cell types - ", time_to_stage_map[timepoint_label], " (", timepoint_label, ")")) +
      (if (!is.null(lim_square)) {
        coord_fixed(ratio = 1, xlim = lim_square, ylim = lim_square, expand = FALSE)
      } else {
        coord_fixed(ratio = 1)
      }) + NoAxes() + NoLegend() + coord_cartesian(clip = "off")

    p # Implicit return
  }, error = function(e) {
    # If complex plotting fails, create a simple, non-repelled DimPlot as a fallback
    err_msg <- paste("DimPlot failed with ggrepel:", e$message)
    cat(paste0("ERROR: ", err_msg, "\n")); flush_now()

    # Fallback DimPlot without ggrepel
    p_fallback <- DimPlot(obj, repel = TRUE, reduction = "umap", label = labelsDim) +
      ggtitle(paste0("Cell types (Fallback) - ", time_to_stage_map[timepoint_label], " (", timepoint_label, ")"))

    p_fallback # Implicit return
  })

  # set common breaks and blank the minimum (corner) label on y
  common_breaks <- seq(-20, 20, by = 10)

  p_feature <- p_feature +
    scale_x_continuous(breaks = common_breaks, labels = as.character(common_breaks)) +
    scale_y_continuous(breaks = common_breaks, labels = function(x) ifelse(x == min(common_breaks), "", as.character(x)))

  p_dim <- p_dim +
    scale_x_continuous(breaks = common_breaks, labels = as.character(common_breaks)) +
    scale_y_continuous(breaks = common_breaks, labels = function(x) ifelse(x == min(common_breaks), "", as.character(x)))


  cat("PROGRESS: 85\n"); flush_now()
  # Write FeaturePlot PNG (Must succeed)
  png(filename = outfile_feature, width = 1000, height = 1000, res = dpiExpr)
  print(p_feature)
  dev.off()

  # Write DimPlot PNG (Wrapped in tryCatch to ensure final output printing is reached)
  dim_plot_success <- tryCatch({
    png(filename = outfile_dim, width = 1000, height = 1000, res = dpiCType)
    print(p_dim)
    dev.off()
    TRUE
  }, error = function(e) {
    cat(paste0("ERROR: Failed to write DimPlot PNG: ", e$message, "\n")); flush_now()
    FALSE
  })


  cat("PROGRESS: 100\n"); flush_now()
  cat(paste0("OUTFILE: ", outfile_feature, "\n")); flush_now()
  # Only print OUTFILE2 if the DimPlot was successfully written
  if (dim_plot_success) {
    cat(paste0("OUTFILE2: ", outfile_dim, "\n")); flush_now()
  } else {
    # If DimPlot writing failed, we still report an empty path for the second plot
    cat("OUTFILE2: \n"); flush_now()
  }

  cat("DONE\n"); flush_now()
}