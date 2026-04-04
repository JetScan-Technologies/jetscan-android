#include "structuring_element.h"
#include <cmath>
#include <stdexcept>
#include <string>

namespace nc {

namespace {

void validateKsize(int ksize) {
    if (ksize < 1 || ksize % 2 == 0)
        throw std::invalid_argument(
            "structuring element ksize must be odd and >= 1, got " + std::to_string(ksize));
}

} // anonymous namespace

StructuringElement makeRect(int ksize) {
    validateKsize(ksize);
    StructuringElement se;
    se.ksize = ksize;
    se.mask.assign(static_cast<size_t>(ksize) * ksize, true);
    return se;
}

StructuringElement makeEllipse(int ksize) {
    validateKsize(ksize);
    StructuringElement se;
    se.ksize = ksize;
    se.mask.resize(static_cast<size_t>(ksize) * ksize, false);
    const float cx = (ksize - 1) / 2.0f;
    const float cy = cx;
    const float rx = cx + 0.5f;
    const float ry = cy + 0.5f;
    for (int r = 0; r < ksize; ++r) {
        for (int c = 0; c < ksize; ++c) {
            float dx = (c - cx) / rx;
            float dy = (r - cy) / ry;
            if (dx * dx + dy * dy <= 1.0f)
                se.mask[r * ksize + c] = true;
        }
    }
    return se;
}

StructuringElement makeCross(int ksize) {
    validateKsize(ksize);
    StructuringElement se;
    se.ksize = ksize;
    se.mask.assign(static_cast<size_t>(ksize) * ksize, false);
    const int mid = ksize / 2;
    for (int i = 0; i < ksize; ++i) {
        se.mask[mid * ksize + i] = true;
        se.mask[i * ksize + mid] = true;
    }
    return se;
}

StructuringElement makeStructuringElement(MorphShape shape, int ksize) {
    switch (shape) {
        case MorphShape::RECT:    return makeRect(ksize);
        case MorphShape::ELLIPSE: return makeEllipse(ksize);
        case MorphShape::CROSS:   return makeCross(ksize);
    }
    return makeRect(ksize);
}

} // namespace nc
