#pragma once
#include "../core/image.h"

namespace nc {

void bitwiseNot(const Image8& src, Image8& dst);
void bitwiseAnd(const Image8& a, const Image8& b, Image8& dst);
void bitwiseOr (const Image8& a, const Image8& b, Image8& dst);
void bitwiseXor(const Image8& a, const Image8& b, Image8& dst);
void absDiff   (const Image8& a, const Image8& b, Image8& dst);

// dst = alpha*src + beta (saturated to uint8)
void linearTransform(const Image8& src, Image8& dst, double alpha, double beta);

// dst = w1*a + w2*b + gamma
void addWeighted(const Image8& a, double w1, const Image8& b, double w2,
                 double gamma, Image8& dst);

void multiplyScalar(const Image32& src, Image32& dst, double scalar);
void minClamp      (const Image32& src, Image32& dst, float maxVal);
void divide        (const Image8& a,   const Image8& b, Image8& dst, double scale);

Image8 bitwiseNot(const Image8& src);
Image8 absDiff   (const Image8& a, const Image8& b);

} // namespace nc
