#pragma once
#include <string>
#include <vector>

namespace nc {

// Merge multiple PDFs into one using PoDoFo
bool mergePdfs(const std::vector<std::string>& inputPaths, const std::string& outputPath);

// Split a PDF into individual pages, one file per page
// Output files: outputDir/page_001.pdf, page_002.pdf, ...
// Returns paths of created files
std::vector<std::string> splitPdf(const std::string& inputPath,
                                   const std::string& outputDir);

// Extract specific pages [fromPage, toPage] (1-indexed, inclusive)
bool extractPages(const std::string& inputPath, const std::string& outputPath,
                  int fromPage, int toPage);

} // namespace nc
