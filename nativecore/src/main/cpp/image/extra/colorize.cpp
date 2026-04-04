#include "colorize.h"
#include <cmath>

namespace nc {

// Jet colormap: blue → cyan → green → yellow → red
static void jetColor(uint8_t v, uint8_t& r, uint8_t& g, uint8_t& b) {
    const float t = v / 255.0f;
    float rv, gv, bv;
    if      (t < 0.125f) { rv = 0.0f;                  gv = 0.0f;                  bv = 0.5f + t * 4.0f;       }
    else if (t < 0.375f) { rv = 0.0f;                  gv = (t - 0.125f) * 4.0f;   bv = 1.0f;                  }
    else if (t < 0.625f) { rv = (t - 0.375f) * 4.0f;   gv = 1.0f;                  bv = 1.0f - (t - 0.375f) * 4.0f; }
    else if (t < 0.875f) { rv = 1.0f;                  gv = 1.0f - (t - 0.625f) * 4.0f; bv = 0.0f;            }
    else                 { rv = 1.0f - (t - 0.875f) * 4.0f; gv = 0.0f;             bv = 0.0f;                  }
    r = static_cast<uint8_t>(rv * 255.0f);
    g = static_cast<uint8_t>(gv * 255.0f);
    b = static_cast<uint8_t>(bv * 255.0f);
}

Image8 applyJetColormap(const Image8& gray) {
    if (gray.empty()) return {};
    // Collapse to 1 channel if needed
    const Image8* src = &gray;
    Image8 tmp;
    if (gray.channels != 1) {
        tmp = Image8(gray.width, gray.height, 1);
        for (int y = 0; y < gray.height; ++y) {
            const uint8_t* s = gray.rowPtr(y);
            uint8_t* d = tmp.rowPtr(y);
            for (int x = 0; x < gray.width; ++x, s += gray.channels, ++d)
                *d = s[0];
        }
        src = &tmp;
    }

    Image8 out(src->width, src->height, 3);
    for (int y = 0; y < src->height; ++y) {
        const uint8_t* s = src->rowPtr(y);
        uint8_t* d = out.rowPtr(y);
        for (int x = 0; x < src->width; ++x, ++s, d += 3)
            jetColor(*s, d[0], d[1], d[2]);
    }
    return out;
}

} // namespace nc
