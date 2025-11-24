all_pkgs <- unique(c("BiocManager", "viridis", "ggrepel", "magrittr", "dplyr", "Seurat",
                     "tidyverse", "gridExtra", "plotly", "jsonlite", "FNN"))
repo <- "https://cloud.r-project.org"

silent_exec <- function(expr) {
  out <- file(tempfile(), open = "w")
  msg <- file(tempfile(), open = "w")
  sink(out)
  sink(msg, type = "message")
  res <- tryCatch(eval.parent(substitute(expr)), error = function(e) NULL)
  sink(type = "message")
  sink()
  close(out); close(msg)
  invisible(res)
}

for (pkg in all_pkgs) {
  if (!requireNamespace(pkg, quietly = TRUE)) {
    if (identical(pkg, "BiocManager")) {
      silent_exec(install.packages(pkg, repos = repo, quiet = TRUE))
    } else if (requireNamespace("BiocManager", quietly = TRUE)) {
      silent_exec(BiocManager::install(pkg, ask = FALSE, update = FALSE))
    } else {
      silent_exec(install.packages(pkg, repos = repo, quiet = TRUE))
    }
  }
  suppressPackageStartupMessages(library(pkg, character.only = TRUE, quietly = TRUE))
}

flush_now <- function() {
  tryCatch({
    flush(stdout())
    flush(stderr())
  }, error = function(e) {})
}

get_metadata <- function() {
  timepoints <- unique(as.character(merge$orig.ident))
  timepoints[4] <- "115hpf"
  timepoints <- c("All timepoints", timepoints)
  genes <- rownames(merge)
  list(genes = genes, timepoints = timepoints)
}

cat("PROGRESS: 1\n"); flush_now()

args <- commandArgs(trailingOnly = TRUE)
top.dir <- args[[1]]

# Expression plot colors
plasma_colors <- c("grey","#7B2C7E","#BA4281","#F36875","#FFA974","#FCEED0")
inferno_colors <- c("grey","#FCA50A","#DC513B","#942667","#420A68","#000004")
viridis_colors <- c("grey","#FDE725","#79D151","#2A788E","#414487","#440154")
magma_colors <- c("grey","#FCFDBF","#FE9F6D","#8D2981","#3B0F70","#000004")

# Cell type plot colors
hpf52_cell_colors <- c(
  "enveloping layer cells" = "#9DC4DE",
  "yolk syncytial nuclei" = "#82B395",
  "deep cells" = "#C44C55"
)
hpf72_cell_colors <- c(
  "enveloping layer cells" = "#592277",
  "primordial germ cells" = "#4A87B7",
  "yolk syncytial nuclei" = "#BBDE92",
  "presumptive ventral ectoderm" = "#85B295",
  "presumptive paraxial mesoderm" = "#9DC5DB",
  "presumptive axial mesoderm" = "#DFB451",
  "mesendoderm" = "#E48651",
  "neutrophils" = "#901C33",
  "presumptive endoderm" = "#C64B57"
)
hpf96_cell_colors <- c(
  "neutrophils" = "#C64B57",
  "ventrolateral mesoderm" = "#4A87B7",
  "endoderm" = "#592277",
  "paraxial mesoderm" = "#85B295",
  "axial mesoderm" = "#9DC5DB",
  "neuromesodermal progenitors" = "#BBDE92",
  "neural ectoderm" = "#DFB451",
  "non neural ectoderm" =  "#E48651",
  "primordial germ cells" = "#901C33",
  "enveloping layer cells" = "#904c33"
)
hpf115_cell_colors <- c(
  "endoderm" = "#F08282",
  "intermediate mesoderm" = "#B42E42",
  "cardiac progenitors" = "#D84051",
  "lateral plate mesoderm" = "#E86854",
  "myeloid cells" = "#F39057", # NI
  "somitic mesoderm" = "#E5A850",
  "myotome" = "#D4C04E",
  "posterior presomitic mesoderm" = "#B5CA69",
  "paraxial mesoderm" = "#96D484",
  "notochord" = "#7DC99E",
  "neuromesodermal progenitors" = "#6EBEB8",
  "non neural ectoderm" = "#64A8C8",
  "neural border ectoderm" = "#5F92D8",
  "epidermis" = "#D19CC6",
  "neural ectoderm" = "#9E9CD1",
  "neural crest" = "#5E4A95",
  "melanocytes" = "#592277"
)
merge_cell_colors <- c(
  "yolk syncytial nuclei" = "#D66B7D",
  "presumptive ventral ectoderm" = "#E89BA5",
  "presumptive paraxial mesoderm" = "#E5A0B8",
  "presumptive axial mesoderm" = "#ED9375",
  "deep cells" = "#E8A587",
  "primordial germ cells" = "#F5C487",
  "enveloping layer cells" = "#E8B86A",
  "axial mesoderm" = "#E8CF7A",
  "mesendoderm" = "#E5D98A",
  "presumptive endoderm" = "#D9E088",
  "endoderm" = "#C8D87A",
  "ventrolateral mesoderm" = "#B0D980",
  "myeloid cells" = "#96D990",
  "neutrophils" = "#7FD9A8",
  "intermediate mesoderm" = "#73CCBB",
  "cardiac progenitors" = "#7AD6D6",
  "lateral plate mesoderm" = "#8FD9E8",
  "myotome" = "#93CCE8",
  "somitic mesoderm" = "#87B8DD",
  "posterior presomitic mesoderm" = "#8AA8D9",
  "paraxial mesoderm" = "#9BA0D9",
  "notochord" = "#B0B5E5",
  "melanocytes" = "#A896DD",
  "neuromesodermal progenitors" = "#B890D9",
  "neural crest" = "#C99BD9",
  "neural ectoderm" = "#D9A8DD",
  "neural border ectoderm" = "#E087BD",
  "non neural ectoderm" = "#E597B5",
  "epidermis" = "#DB8BA0"
)
by_time_cell_colors <- c(
  "52hpf" = "#D66B7D",
  "72hpf" = "#E8A587",
  "96hpf" =  "#96D990",
  "115hpf" = "#87B8DD"
)
time_to_stage_map <- c(
  "52hpf" = "dispersed",
  "72hpf" = "incipient aggregate",
  "96hpf" = "tailbud",
  "115hpf" = "8-somite",
  "all" = ""
)

cat("PROGRESS: 10\n"); flush_now()

pip.list <- readRDS(paste0(top.dir, "/pip.list.annotated.rds"))

cat("PROGRESS: 50\n"); flush_now()

merge <- readRDS(paste0(top.dir, "/merge.annotated.rds"))

cat("PROGRESS: 90\n"); flush_now()

pip.list[[4]]$orig.ident <- "115hpf"
names(pip.list) <- c("52hpf", "72hpf", "96hpf", "115hpf")
con <- file("stdin", open = "r")

repeat {

  line <- tryCatch(readLines(con, n = 1, warn = FALSE), error = function(e) character())

  if (length(line) == 0) {
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

  if (!is.null(req$action) && identical(req$action, "metadata")) {
    cat("PROGRESS: 95\n"); flush_now()
    meta <- get_metadata()
    cat(paste0("METADATA: ", jsonlite::toJSON(meta, auto_unbox = TRUE, pretty = FALSE), "\n"))
    flush_now()
    next
  }

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

  color_by_ctype <- length(colorCType) == 1L && !is.na(colorCType) && colorCType == 0L

  cat("PROGRESS: 40\n"); flush_now()

  if (identical(timepoint, "All timepoints")) {
    obj <- merge
    timepoint_label <- "all"
  } else {
    obj <- pip.list[[timepoint]]
    timepoint_label <- unique(obj$orig.ident)
  }

  if (identical(timepoint_label, "all") && color_by_ctype) {
    Idents(obj) <- obj$cell.type
  } else if (identical(timepoint_label, "all")) {
    Idents(obj) <- obj$timepoint
  }

  emb <- Embeddings(obj, "umap") %>% as.data.frame()
  emb$cluster <- Idents(obj)
  coords_cols <- if (identical(timepoint_label, "115hpf")) c("UMAP_1", "UMAP_2") else c("umap_1", "umap_2")
  anchors <- emb %>%
    group_by(cluster) %>%
    group_modify(function(df, key){
      k <- min(25, nrow(df) - 1)
      dbar <- rowMeans(FNN::knn.dist(as.matrix(df[coords_cols]), k = k))
      df[which.min(dbar), , drop = FALSE]
    }) %>% ungroup()

  safe <- function(x) gsub("[^A-Za-z0-9._-]", "_", x)
  stem <- paste0(safe(gene), "_", safe(timepoint))

  out.dir <- tempdir()
  if (!dir.exists(out.dir)) {
    dir.create(out.dir, recursive = TRUE, showWarnings = FALSE)
  }

  outfile_feature <- tempfile(pattern = paste0(stem, "_feature_"), tmpdir = out.dir, fileext = ".png")
  outfile_dim <- tempfile(pattern = paste0(stem, "_dim_"),     tmpdir = out.dir, fileext = ".png")

  cat("PROGRESS: 65\n"); flush_now()

  lim_square <- c(-20, 20)

  title_expr <- if (identical(timepoint_label, "all")) {
    bquote(bold(italic(.(gene))))
  } else {
    bquote(bold(italic(.(gene)) ~ "-" ~ .(time_to_stage_map[timepoint_label]) ~ "(" * .(timepoint_label) * ")"))
  }

  colors_expr <- switch(
    colorExpr,
    "viridis" = viridis_colors,
    "magma" = magma_colors,
    "inferno" = inferno_colors,
    "plasma" = plasma_colors,
    viridis_colors
  )

  colors_dim <- switch(
    timepoint,
    "52hpf" = hpf52_cell_colors,
    "72hpf" = hpf72_cell_colors,
    "96hpf" = hpf96_cell_colors,
    "115hpf" = hpf115_cell_colors,
    merge_cell_colors
  )

  aes_coords_dim <- if (identical(timepoint_label, "115hpf")) {
    aes(x = UMAP_1, y = UMAP_2, label = cluster)
  } else {
    aes(x = umap_1, y = umap_2, label = cluster)
  }

  labels_repelled <- geom_text_repel(
    data = anchors,
    aes_coords_dim,
    seed = 42,
    size = 4,
    color = "black",
    segment.color = "black",
    segment.size = 0.6,
    min.segment.length = 0.2,
    box.padding = 1.5,
    point.padding = 1,
    force = 5,
    force_pull = 1,
    max.overlaps = Inf
  )

  cat("PROGRESS: 66\n"); flush_now()

  p_feature <- FeaturePlot(obj, features = gene, reduction = "umap", order = TRUE, label = FALSE, cols = colors_expr) +
    ggtitle(title_expr) +
    #coord_fixed(ratio = 1, xlim = lim_square, ylim = lim_square, expand = FALSE) +
    #scale_color_gradientn(colours = colors_expr) +
    #coord_cartesian(clip = "off") +
    theme(plot.title = element_text(hjust = 0.5)) +
    NoAxes()

  cat("PROGRESS: 67\n"); flush_now()


  if (identical(timepoint_label, "all") && !color_by_ctype) {
    p_dim <- DimPlot(obj, reduction = "umap", group.by = "timepoint", label = labelsDim) +
      scale_color_manual(values = by_time_cell_colors)
  } else {
    p_dim <- DimPlot(obj, reduction = "umap", label = FALSE) +
      scale_color_manual(values = colors_dim)
    if (labelsDim) {
      p_dim <- p_dim + labels_repelled
    }
  }

  cat("PROGRESS: 68\n"); flush_now()

  p_dim <- p_dim + ggtitle(paste0("Cell types - ", time_to_stage_map[timepoint_label], " (", timepoint_label, ")")) +
    #coord_fixed(ratio = 1, xlim = lim_square, ylim = lim_square, expand = FALSE) +
    #coord_cartesian(clip = "off") +
    theme(plot.title = element_text(hjust = 0.5)) +
    NoAxes() +
    NoLegend()

  if (labelsExpr) {
    p_feature <- p_feature + labels_repelled
  }

  cat("PROGRESS: 85\n"); flush_now()

  png(filename = outfile_feature, width = 1000, height = 1000, res = dpiExpr)
  print(p_feature)
  dev.off()

  png(filename = outfile_dim, width = 1000, height = 1000, res = dpiCType)
  print(p_dim)
  dev.off()

  cat("PROGRESS: 100\n"); flush_now()

  cat(paste0("OUTFILE: ", outfile_feature, "\n")); flush_now()
  cat(paste0("OUTFILE2: ", outfile_dim, "\n")); flush_now()

  cat("DONE\n"); flush_now()
}