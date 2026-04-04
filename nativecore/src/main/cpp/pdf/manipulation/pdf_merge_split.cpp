
#include <podofo/podofo.h>
#include <android/log.h>
#include <cstdio>
#include "pdf_merge_split.h"

#define TAG "nc::pdf_merge_split"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

namespace nc {

bool mergePdfs(const std::vector<std::string>& inputPaths, const std::string& outputPath) {
    if (inputPaths.empty()) return false;
    try {
        PoDoFo::PdfMemDocument merged;
        merged.Load(inputPaths[0]);
        for (size_t i = 1; i < inputPaths.size(); ++i) {
            PoDoFo::PdfMemDocument other;
            other.Load(inputPaths[i]);
            merged.GetPages().AppendDocumentPages(other);
        }
        merged.Save(outputPath);
        LOGI("Merged %zu PDFs → %s", inputPaths.size(), outputPath.c_str());
        return true;
    } catch (const PoDoFo::PdfError& e) {
        LOGE("mergePdfs error: %s", e.what());
        return false;
    }
}

std::vector<std::string> splitPdf(const std::string& inputPath,
                                   const std::string& outputDir) {
    std::vector<std::string> result;
    try {
        PoDoFo::PdfMemDocument src;
        src.Load(inputPath);
        const int n = src.GetPages().GetCount();
        for (int i = 0; i < n; ++i) {
            char buf[1024];
            snprintf(buf, sizeof(buf), "%s/page_%03d.pdf", outputDir.c_str(), i + 1);
            PoDoFo::PdfMemDocument page;
            page.Load(inputPath);
            // Remove all other pages
            for (int j = n - 1; j >= 0; --j)
                if (j != i) page.GetPages().RemovePageAt(j);
            page.Save(buf);
            result.emplace_back(buf);
        }
        LOGI("Split %d pages from %s", n, inputPath.c_str());
    } catch (const PoDoFo::PdfError& e) {
        LOGE("splitPdf error: %s", e.what());
    }
    return result;
}

bool extractPages(const std::string& inputPath, const std::string& outputPath,
                  int fromPage, int toPage) {
    try {
        PoDoFo::PdfMemDocument src;
        src.Load(inputPath);
        const int n = src.GetPages().GetCount();
        // Convert 1-indexed to 0-indexed
        int from0 = fromPage - 1;
        int to0   = toPage - 1;
        if (from0 < 0) from0 = 0;
        if (to0 >= n)  to0   = n - 1;
        // Delete pages outside [from0, to0]
        for (int i = n - 1; i > to0; --i)   src.GetPages().RemovePageAt(i);
        for (int i = from0 - 1; i >= 0; --i) src.GetPages().RemovePageAt(i);
        src.Save(outputPath);
        return true;
    } catch (const PoDoFo::PdfError& e) {
        LOGE("extractPages error: %s", e.what());
        return false;
    }
}

} // namespace nc
