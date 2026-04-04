#pragma once
#include <cstdint>

// ─── NEON intrinsic wrappers ──────────────────────────────────────────────────
// Only included on ARM targets. All other targets use scalar fallbacks.

#if defined(__ARM_NEON) || defined(__ARM_NEON__)
#  include <arm_neon.h>
#  define NC_HAS_NEON 1
#else
#  define NC_HAS_NEON 0
#endif

namespace nc {
namespace simd {

// Saturated uint8 clamp to [0, 255]
inline uint8_t clamp_u8(int32_t v) {
    return static_cast<uint8_t>(v < 0 ? 0 : (v > 255 ? 255 : v));
}

// Saturated float → uint8
inline uint8_t clamp_f32(float v) {
    return static_cast<uint8_t>(v < 0.f ? 0 : (v > 255.f ? 255 : static_cast<int>(v + 0.5f)));
}

// Process 16 bytes with NEON bitwise NOT, scalar fallback otherwise
inline void bitwiseNot16(const uint8_t* src, uint8_t* dst) {
#if NC_HAS_NEON
    vst1q_u8(dst, vmvnq_u8(vld1q_u8(src)));
#else
    for (int i = 0; i < 16; ++i) dst[i] = ~src[i];
#endif
}

// Process 16 bytes with NEON bitwise AND
inline void bitwiseAnd16(const uint8_t* a, const uint8_t* b, uint8_t* dst) {
#if NC_HAS_NEON
    vst1q_u8(dst, vandq_u8(vld1q_u8(a), vld1q_u8(b)));
#else
    for (int i = 0; i < 16; ++i) dst[i] = a[i] & b[i];
#endif
}

// Process 16 bytes with NEON min
inline void min16(const uint8_t* a, const uint8_t* b, uint8_t* dst) {
#if NC_HAS_NEON
    vst1q_u8(dst, vminq_u8(vld1q_u8(a), vld1q_u8(b)));
#else
    for (int i = 0; i < 16; ++i) dst[i] = a[i] < b[i] ? a[i] : b[i];
#endif
}

// Process 16 bytes with NEON max
inline void max16(const uint8_t* a, const uint8_t* b, uint8_t* dst) {
#if NC_HAS_NEON
    vst1q_u8(dst, vmaxq_u8(vld1q_u8(a), vld1q_u8(b)));
#else
    for (int i = 0; i < 16; ++i) dst[i] = a[i] > b[i] ? a[i] : b[i];
#endif
}

// Absolute difference of 16 bytes
inline void absDiff16(const uint8_t* a, const uint8_t* b, uint8_t* dst) {
#if NC_HAS_NEON
    uint8x16_t va = vld1q_u8(a), vb = vld1q_u8(b);
    vst1q_u8(dst, vabdq_u8(va, vb));
#else
    for (int i = 0; i < 16; ++i) {
        int d = static_cast<int>(a[i]) - static_cast<int>(b[i]);
        dst[i] = static_cast<uint8_t>(d < 0 ? -d : d);
    }
#endif
}

} // namespace simd
} // namespace nc
