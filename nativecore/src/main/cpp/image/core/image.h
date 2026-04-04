#pragma once

#include <algorithm>
#include <cassert>
#include <cstdint>
#include <cstring>
#include <vector>

namespace nc {

template <typename T>
struct Image {
    std::vector<T> data;
    int width    = 0;
    int height   = 0;
    int channels = 0;
    int stride   = 0; // elements per row (width * channels)

    Image() = default;

    Image(int w, int h, int ch)
        : width(w), height(h), channels(ch), stride(w * ch) {
        if (w > 0 && h > 0 && ch > 0)
            data.assign(static_cast<size_t>(stride) * h, T{});
    }

    Image(const T* src, int w, int h, int ch, int src_stride_bytes = -1)
        : width(w), height(h), channels(ch), stride(w * ch) {
        if (!src || w <= 0 || h <= 0 || ch <= 0) return;
        const int row_bytes = w * ch * static_cast<int>(sizeof(T));
        const int src_row   = (src_stride_bytes < 0) ? row_bytes : src_stride_bytes;
        data.resize(static_cast<size_t>(stride) * h);
        for (int y = 0; y < h; ++y) {
            const auto* srow = reinterpret_cast<const uint8_t*>(src)
                + static_cast<ptrdiff_t>(y) * src_row;
            std::memcpy(rowPtr(y), srow, static_cast<size_t>(row_bytes));
        }
    }

    bool   empty()     const { return data.empty() || width <= 0 || height <= 0; }
    size_t sizeBytes() const { return data.size() * sizeof(T); }

    T*       rowPtr(int y)       { return data.data() + static_cast<ptrdiff_t>(y) * stride; }
    const T* rowPtr(int y) const { return data.data() + static_cast<ptrdiff_t>(y) * stride; }

    T& at(int x, int y, int c = 0) {
        assert(x >= 0 && x < width && y >= 0 && y < height && c >= 0 && c < channels);
        return data[static_cast<size_t>(y) * stride + x * channels + c];
    }
    const T& at(int x, int y, int c = 0) const {
        assert(x >= 0 && x < width && y >= 0 && y < height && c >= 0 && c < channels);
        return data[static_cast<size_t>(y) * stride + x * channels + c];
    }

    Image clone() const {
        Image out;
        out.width = width; out.height = height;
        out.channels = channels; out.stride = stride;
        out.data = data;
        return out;
    }

    Image roi(int rx, int ry, int rw, int rh) const {
        if (empty()) return {};
        rx = std::clamp(rx, 0, width  - 1);
        ry = std::clamp(ry, 0, height - 1);
        rw = std::min(rw, width  - rx);
        rh = std::min(rh, height - ry);
        if (rw <= 0 || rh <= 0) return {};
        Image out(rw, rh, channels);
        for (int y = 0; y < rh; ++y) {
            const T* src = rowPtr(ry + y) + rx * channels;
            std::copy(src, src + rw * channels, out.rowPtr(y));
        }
        return out;
    }

    Image extractChannel(int ch) const {
        if (empty() || ch < 0 || ch >= channels) return {};
        Image out(width, height, 1);
        for (int y = 0; y < height; ++y) {
            const T* src = rowPtr(y);
            T* dst = out.rowPtr(y);
            for (int x = 0; x < width; ++x)
                dst[x] = src[x * channels + ch];
        }
        return out;
    }
};

using Image8  = Image<uint8_t>;
using Image32 = Image<float>;

struct Point2f { float x = 0, y = 0; };
struct Point2i { int   x = 0, y = 0; };

struct Line {
    float slope      = 0;
    float yIntercept = 0;
};

struct LineSegment {
    float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
    float length() const;
};

struct Quad {
    Point2f tl, tr, br, bl;
    float area() const;
};

} // namespace nc
