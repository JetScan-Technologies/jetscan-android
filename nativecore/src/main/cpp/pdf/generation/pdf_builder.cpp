#include "pdf_builder.h"
#include <android/log.h>
#include <cmath>

#define TAG "nc::pdf_builder"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

#if __has_include("hpdf.h")
#  include "hpdf.h"
#  define NC_HAS_HARU 1
#elif __has_include("../../third_party/libharu/include/hpdf.h")
#  include "../../third_party/libharu/include/hpdf.h"
#  define NC_HAS_HARU 1
#else
#  define NC_HAS_HARU 0
#endif

namespace nc {

namespace {

static void hpdfErrorHandler(HPDF_STATUS errorNo, HPDF_STATUS detailNo, void*) {
    LOGE("libharu error: 0x%04X detail: 0x%04X", (unsigned)errorNo, (unsigned)detailNo);
}

static void pageSizePts(PdfPageSize size, float& w, float& h) {
    switch (size) {
        case PdfPageSize::A4:     w = 595.0f; h = 842.0f; break;
        case PdfPageSize::LETTER: w = 612.0f; h = 792.0f; break;
        case PdfPageSize::A3:     w = 842.0f; h = 1191.0f; break;
        case PdfPageSize::LEGAL:  w = 612.0f; h = 1008.0f; break;
    }
}

} // anonymous

bool buildPdfFromJpegs(const std::vector<std::string>& jpegPaths,
                       const std::string& outputPath,
                       const PdfBuildOptions& opts) {
#if NC_HAS_HARU
    HPDF_Doc pdf = HPDF_New(hpdfErrorHandler, nullptr);
    if (!pdf) { LOGE("HPDF_New failed"); return false; }

    // Compression mode
    switch (opts.quality) {
        case PdfQuality::VERY_LOW:
        case PdfQuality::LOW:    HPDF_SetCompressionMode(pdf, HPDF_COMP_ALL);   break;
        case PdfQuality::MEDIUM: HPDF_SetCompressionMode(pdf, HPDF_COMP_IMAGE); break;
        case PdfQuality::HIGH:   HPDF_SetCompressionMode(pdf, HPDF_COMP_NONE);  break;
    }

    float pageW, pageH;
    pageSizePts(opts.pageSize, pageW, pageH);
    const float margin = opts.hasMargin ? opts.marginPts : 0.0f;

    for (const auto& jpegPath : jpegPaths) {
        HPDF_Image img = HPDF_LoadJpegImageFromFile(pdf, jpegPath.c_str());
        if (!img) {
            LOGE("Failed to load JPEG: %s", jpegPath.c_str());
            continue;
        }
        HPDF_Page page = HPDF_AddPage(pdf);
        HPDF_Page_SetWidth(page, pageW);
        HPDF_Page_SetHeight(page, pageH);

        const float imgW = static_cast<float>(HPDF_Image_GetWidth(img));
        const float imgH = static_cast<float>(HPDF_Image_GetHeight(img));
        const float drawW = pageW - 2.0f * margin;
        const float drawH = pageH - 2.0f * margin;

        // Scale to fit while preserving aspect ratio
        float scale = std::min(drawW / imgW, drawH / imgH);
        float w = imgW * scale;
        float h = imgH * scale;
        float x = margin + (drawW - w) / 2.0f;
        float y = margin + (drawH - h) / 2.0f;

        HPDF_Page_DrawImage(page, img, x, y, w, h);
        LOGI("Added page from: %s", jpegPath.c_str());
    }

    HPDF_STATUS status = HPDF_SaveToFile(pdf, outputPath.c_str());
    HPDF_Free(pdf);
    if (status != HPDF_OK) {
        LOGE("HPDF_SaveToFile failed: %s", outputPath.c_str());
        return false;
    }
    LOGI("PDF saved: %s", outputPath.c_str());
    return true;
#else
    LOGE("buildPdfFromJpegs: libharu not available");
    return false;
#endif
}

} // namespace nc
