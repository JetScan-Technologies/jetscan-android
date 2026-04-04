#include "median_blur.h"
#include <algorithm>
#include <cstring>
#include <android/log.h>

#define TAG "nc::median_blur"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

namespace {

// Scan a 256-bin histogram to find the value where the cumulative count
// first exceeds `threshold` (i.e. the lower median of the window).
static inline uint8_t findMedian(const int hist[256], int threshold) {
    int count = 0;
    for (int i = 0; i < 256; ++i) {
        count += hist[i];
        if (count > threshold) return static_cast<uint8_t>(i);
    }
    return 255;
}

// Histogram-based O(1)-amortized median filter for a single channel.
//
// Uses per-column histograms (Huang et al.) so that sliding the window
// both horizontally and vertically only requires O(k) histogram updates
// per pixel instead of rebuilding an O(k²) window.  Finding the median
// inside the 256-bin histogram is O(256) = O(1) for uint8_t data, giving
// overall O(k + 256) work per pixel instead of the naive O(k² log k²).
static void medianBlurChannel(const Image8& src, Image8& dst, int c, int ksize) {
    const int half = ksize / 2;
    const int w = src.width;
    const int h = src.height;
    const int ch = src.channels;
    const int windowArea = ksize * ksize;
    const int medianThreshold = windowArea / 2; // for 0-based nth_element semantics

    // --- Per-column histograms ------------------------------------------
    // colHist[x][v] = count of value v in column x across the current
    // vertical window of ksize rows.
    // Allocate as a flat array: colHist[x * 256 + v].
    std::vector<int> colHistBuf(static_cast<size_t>(w) * 256, 0);
    auto colHist = [&](int x) -> int* {
        return colHistBuf.data() + static_cast<size_t>(x) * 256;
    };

    // Initialize column histograms for the first row window (y = 0).
    // The vertical window covers rows clamp(-half..+half, 0, h-1).
    for (int x = 0; x < w; ++x) {
        int* ch_ptr = colHist(x);
        for (int ky = -half; ky <= half; ++ky) {
            int sy = std::clamp(ky, 0, h - 1);
            uint8_t val = src.rowPtr(sy)[x * ch + c];
            ch_ptr[val]++;
        }
    }

    // --- Process rows ---------------------------------------------------
    for (int y = 0; y < h; ++y) {
        uint8_t* drow = dst.rowPtr(y);

        // Update column histograms when moving down (skip for y == 0).
        if (y > 0) {
            int removeRow = std::clamp(y - half - 1, 0, h - 1);
            int addRow    = std::clamp(y + half,     0, h - 1);
            // Only update if the clamped rows actually changed.
            // When y - half - 1 < 0 and y - half - 2 < 0 both clamp to 0,
            // the old row we'd "remove" is the same as what was already
            // removed, BUT because we're re-entering border territory we
            // must still do it to keep counts correct.  The simpler approach
            // is to always subtract the old row and add the new row.
            for (int x = 0; x < w; ++x) {
                int* ch_ptr = colHist(x);
                uint8_t oldVal = src.rowPtr(removeRow)[x * ch + c];
                uint8_t newVal = src.rowPtr(addRow)[x * ch + c];
                ch_ptr[oldVal]--;
                ch_ptr[newVal]++;
            }
        }

        // Build the window histogram for x = 0 by summing column histograms
        // in the horizontal range clamp(-half..+half, 0, w-1).
        int hist[256];
        std::memset(hist, 0, sizeof(hist));

        for (int kx = -half; kx <= half; ++kx) {
            int sx = std::clamp(kx, 0, w - 1);
            const int* ch_ptr = colHist(sx);
            for (int v = 0; v < 256; ++v)
                hist[v] += ch_ptr[v];
        }

        drow[0 * ch + c] = findMedian(hist, medianThreshold);

        // Slide the window histogram to the right for x = 1 .. w-1.
        for (int x = 1; x < w; ++x) {
            int removeCol = std::clamp(x - half - 1, 0, w - 1);
            int addCol    = std::clamp(x + half,     0, w - 1);

            const int* remHist = colHist(removeCol);
            const int* addHist = colHist(addCol);
            for (int v = 0; v < 256; ++v) {
                hist[v] += addHist[v] - remHist[v];
            }

            drow[x * ch + c] = findMedian(hist, medianThreshold);
        }
    }
}

} // anonymous

void medianBlur(const Image8& src, Image8& dst, int ksize) {
    if (src.empty()) return;
    if (ksize < 3) ksize = 3;
    if (ksize % 2 == 0) {
        LOGE("medianBlur: ksize must be odd, got %d, using %d", ksize, ksize + 1);
        ksize++;
    }
    dst = Image8(src.width, src.height, src.channels);
    for (int c = 0; c < src.channels; ++c)
        medianBlurChannel(src, dst, c, ksize);
}

Image8 medianBlur(const Image8& src, int ksize) {
    Image8 dst;
    medianBlur(src, dst, ksize);
    return dst;
}

} // namespace nc
