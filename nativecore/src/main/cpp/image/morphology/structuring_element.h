#pragma once
#include <stdexcept>
#include <vector>

namespace nc {

enum class MorphShape { RECT, ELLIPSE, CROSS };

struct StructuringElement {
    std::vector<bool> mask;
    int ksize = 0;

    bool isValid() const {
        return ksize > 0 && (ksize % 2 == 1)
            && mask.size() == static_cast<size_t>(ksize) * ksize;
    }

    bool get(int row, int col) const {
        if (row < 0 || row >= ksize || col < 0 || col >= ksize)
            return false;
        return mask[row * ksize + col];
    }
};

/// Create a structuring element of the given shape and kernel size.
/// ksize must be odd and >= 1; throws std::invalid_argument otherwise.
StructuringElement makeStructuringElement(MorphShape shape, int ksize);
StructuringElement makeRect(int ksize);
StructuringElement makeEllipse(int ksize);
StructuringElement makeCross(int ksize);

} // namespace nc
