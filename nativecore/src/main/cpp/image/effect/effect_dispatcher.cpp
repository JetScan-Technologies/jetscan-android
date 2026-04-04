#include "effect_dispatcher.h"
#include "../color/grayscale.h"
#include "../color/rgb_hsv.h"
#include "../pixel_ops/pixel_ops.h"
#include "../normalize/normalizer.h"
#include "../filter/gaussian_blur.h"
#include "../filter/median_blur.h"
#include "../filter/bilateral_filter.h"
#include "../filter/sharpen.h"
#include "../morphology/dilation.h"
#include "../morphology/morph_ops.h"
#include "../morphology/structuring_element.h"
#include "../threshold/adaptive_threshold.h"
#include <algorithm>
#include <cstring>

namespace nc {

// ── ORIGINAL ─────────────────────────────────────────────────────────────
static Image8 effectOriginal(const Image8& src) { return src.clone(); }

// ── VIBRANT ───────────────────────────────────────────────────────────────
// Port of ImageFilters.toVibrant(scaleFactor=1.8):
// RGB→HSV → S *= 1.8 → normalize(S, 0,255) → HSV→RGB
static Image8 effectVibrant(const Image8& src) {
    Image32 hsv = rgbToHsv(src);
    if (hsv.empty()) return src.clone();
    const int w = hsv.width, h = hsv.height;

    // Pass 1: Scale S by 1.8 and find min/max in same pass
    float sMin = 1.0f, sMax = 0.0f;
    for (int y = 0; y < h; ++y) {
        float* hr = hsv.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float s = std::clamp(hr[x*3+1] * 1.8f, 0.0f, 1.0f);
            hr[x*3+1] = s;
            if (s < sMin) sMin = s;
            if (s > sMax) sMax = s;
        }
    }

    // Pass 2: Normalize S in-place (avoid separate Image32 allocation)
    if (sMax > sMin) {
        const float scale = 1.0f / (sMax - sMin);
        for (int y = 0; y < h; ++y) {
            float* hr = hsv.rowPtr(y);
            for (int x = 0; x < w; ++x)
                hr[x*3+1] = (hr[x*3+1] - sMin) * scale;
        }
    }

    Image8 rgb = hsvToRgb(hsv);
    // If src was RGBA, add back alpha channel
    if (src.channels == 4) {
        Image8 rgba(w, h, 4);
        for (int y = 0; y < h; ++y) {
            const uint8_t* r = rgb.rowPtr(y);
            const uint8_t* s = src.rowPtr(y);
            uint8_t* d = rgba.rowPtr(y);
            for (int x = 0; x < w; ++x) {
                d[x*4+0] = r[x*3+0];
                d[x*4+1] = r[x*3+1];
                d[x*4+2] = r[x*3+2];
                d[x*4+3] = s[x*4+3];
            }
        }
        return rgba;
    }
    return rgb;
}

// ── NO_SHADOW ─────────────────────────────────────────────────────────────
// Port of ImageFilters.noShadow():
// RGB→HSV → extract V channel (ch 2)
// dilate(V, ones_7x7) → medianBlur(21) = bg_model
// absdiff(V, bg_model) → bitwise_not(diff) → normalize(0,255) = norm_V
// merge [H, S, norm_V] → HSV→RGB
static Image8 effectNoShadow(const Image8& src) {
    Image32 hsv = rgbToHsv(src);
    if (hsv.empty()) return src.clone();
    const int w = hsv.width, h = hsv.height;

    // Extract V channel as uint8
    Image8 V8(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* hr = hsv.rowPtr(y);
        uint8_t* vr = V8.rowPtr(y);
        for (int x = 0; x < w; ++x)
            vr[x] = static_cast<uint8_t>(std::clamp(hr[x*3+2] * 255.0f, 0.0f, 255.0f));
    }

    // dilate(V, ones_7x7) then medianBlur(21)
    Image8 dilated = dilate(V8, makeRect(7));
    Image8 bgModel = medianBlur(dilated, 21);

    // absdiff → bitwise_not → normalize
    Image8 diff = absDiff(V8, bgModel);
    Image8 diffInv = bitwiseNot(diff);
    Image8 normV;
    normalizeMinMax(diffInv, normV, 0, 255);

    // Put norm_V back into HSV and convert
    for (int y = 0; y < h; ++y) {
        float* hr = hsv.rowPtr(y);
        const uint8_t* vr = normV.rowPtr(y);
        for (int x = 0; x < w; ++x)
            hr[x*3+2] = vr[x] / 255.0f;
    }
    return hsvToRgb(hsv);
}

// ── AUTO ──────────────────────────────────────────────────────────────────
// Port of ImageFilters.toAutoFilter()
static Image8 effectAuto(const Image8& src) {
    // contrast × 2
    Image8 contrast;
    linearTransform(src, contrast, 2.0, 0.0);
    // color bump × 1.5
    Image8 bumped;
    linearTransform(contrast, bumped, 1.5, 0.0);
    // grayscale
    Image8 gray = toGrayscale(bumped);
    // GaussianBlur(15)
    Image8 blurred = gaussianBlur(gray, 15, 0.0f);
    // divide(gray, blurred, corrected, 255.0)
    Image8 corrected;
    divide(gray, blurred, corrected, 255.0);
    // adaptiveThreshGaussian(blockSize=15, C=10)
    Image8 thresholded = adaptiveThreshGaussian(corrected, 15, 10.0);
    // bilateralFilter(d=9, sigmaColor=75, sigmaSpace=75)
    Image8 bilateral = bilateralFilter(thresholded, 9, 75.0f, 75.0f);
    // morphClose(rect_1x1) — no-op with ksize=1 so just copy
    Image8 morphed = morphClose(bilateral, makeRect(1));
    // sharpenFull
    return sharpenFull(morphed);
}

// ── COLOR_BUMP ─────────────────────────────────────────────────────────────
static Image8 effectColorBump(const Image8& src) {
    Image8 dst;
    linearTransform(src, dst, 1.5, 0.0);
    return dst;
}

// ── GRAYSCALE ─────────────────────────────────────────────────────────────
static Image8 effectGrayscale(const Image8& src) {
    return toGrayscale(src);
}

// ── B_W ───────────────────────────────────────────────────────────────────
// Port of ImageFilters.toBlackAndWhite():
// grayscale → medianBlur(5) → adaptiveThreshGaussian(11, 3)
static Image8 effectBW(const Image8& src) {
    Image8 gray = toGrayscale(src);
    Image8 blurred = medianBlur(gray, 5);
    return adaptiveThreshGaussian(blurred, 11, 3.0);
}

// ── Dispatcher ────────────────────────────────────────────────────────────
Image8 applyEffect(const Image8& src, ImageFilter filter) {
    switch (filter) {
        case ImageFilter::ORIGINAL:   return effectOriginal(src);
        case ImageFilter::VIBRANT:    return effectVibrant(src);
        case ImageFilter::NO_SHADOW:  return effectNoShadow(src);
        case ImageFilter::AUTO:       return effectAuto(src);
        case ImageFilter::COLOR_BUMP: return effectColorBump(src);
        case ImageFilter::GRAYSCALE:  return effectGrayscale(src);
        case ImageFilter::B_W:        return effectBW(src);
    }
    return src.clone();
}

// ── Color Adjustment ─────────────────────────────────────────────────────
// Port of Effects.colorAdjustment(brightness, contrast, saturation):
// linearTransform(contrast, brightness) → HSV → S *= saturation → clamp → RGB
Image8 colorAdjust(const Image8& src, float brightness, float contrast, float saturation) {
    // Step 1: linear transform
    Image8 adjusted;
    linearTransform(src, adjusted, static_cast<double>(contrast),
                    static_cast<double>(brightness));

    // Step 2: scale saturation via HSV
    Image32 hsv = rgbToHsv(adjusted);
    if (hsv.empty()) return adjusted;
    const int w = hsv.width, h = hsv.height;
    for (int y = 0; y < h; ++y) {
        float* row = hsv.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float s = row[x*3+1] * saturation;
            row[x*3+1] = s > 1.0f ? 1.0f : s; // min(S, 1.0)
        }
    }
    return hsvToRgb(hsv);
}

} // namespace nc
