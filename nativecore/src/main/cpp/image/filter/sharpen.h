#pragma once
#include "../core/image.h"

namespace nc {

// Cross-shaped Laplacian: [0,-1,0; -1,5,-1; 0,-1,0]
void sharpenCross(const Image8& src, Image8& dst);
Image8 sharpenCross(const Image8& src);

// Full-mask: [-1,-1,-1; -1,9,-1; -1,-1,-1]
void sharpenFull(const Image8& src, Image8& dst);
Image8 sharpenFull(const Image8& src);

} // namespace nc
