# Thin Windows wrapper: pin Seurat 4.4.0, then run shared worker core.
args_full <- commandArgs(trailingOnly = FALSE)
script_arg <- grep("^--file=", args_full, value = TRUE)
script_path <- if (length(script_arg) > 0) sub("^--file=", "", script_arg[[1]]) else "plot_worker_windows_wrapper.R"
core_path <- file.path(dirname(normalizePath(script_path)), "plot_worker_core.R")
source(core_path, local = TRUE)

install_seurat <- function(app_lib, repo, silent_exec) {
  if (!requireNamespace("Seurat", quietly = TRUE) || packageVersion("Seurat")$major != 4) {
    silent_exec(remotes::install_version(
      "Seurat",
      version = "4.4.0",
      repos = repo,
      lib = app_lib,
      upgrade = "never",
      quiet = TRUE
    ))
  }
}

run_plot_worker(install_seurat)

