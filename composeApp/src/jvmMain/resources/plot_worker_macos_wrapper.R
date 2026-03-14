# Thin macOS wrapper: install Seurat from CRAN, then run shared worker core.
args_full <- commandArgs(trailingOnly = FALSE)
script_arg <- grep("^--file=", args_full, value = TRUE)
script_path <- if (length(script_arg) > 0) sub("^--file=", "", script_arg[[1]]) else "plot_worker_macos_wrapper.R"
core_path <- file.path(dirname(normalizePath(script_path)), "plot_worker_core.R")
source(core_path, local = TRUE)

install_seurat <- function(app_lib, repo, silent_exec) {
  if (!requireNamespace("Seurat", quietly = TRUE)) {
    silent_exec(install.packages("Seurat", repos = repo, lib = app_lib, quiet = TRUE))
  }
}

tryCatch(
  run_plot_worker(install_seurat),
  error = function(e) {
    cat(paste0("ERROR: ", conditionMessage(e), "\n"))
    flush(stdout())
    quit(status = 1)
  }
)
