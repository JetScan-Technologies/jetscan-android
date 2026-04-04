#pragma once
#include "../core/image.h"
#include "../geometry/quad_utils.h"

namespace nc {

// Crop a document from src using the 4 corner points.
// Computes perspective transform and warps to flat rectangle.
// Exact port of OpenCvManagerImpl.cropDocument() + Operations.crop()
Image8 cropDocument(const Image8& src, const Quad& corners);

} // namespace nc
