#pragma once
#include <string>
#include <vector>

namespace nc {

enum class PdfPageSize { A4, LETTER, A3, LEGAL };
enum class PdfQuality  { VERY_LOW, LOW, MEDIUM, HIGH };

struct PdfBuildOptions {
    PdfPageSize pageSize  = PdfPageSize::A4;
    PdfQuality  quality   = PdfQuality::MEDIUM;
    bool        hasMargin = false;
    float       marginPts = 20.0f; // points (1 pt = 1/72 inch)
};

// Build a PDF from a list of JPEG file paths using libharu.
// Returns true on success.
bool buildPdfFromJpegs(const std::vector<std::string>& jpegPaths,
                       const std::string& outputPath,
                       const PdfBuildOptions& opts);

} // namespace nc
