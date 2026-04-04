#pragma once
#include "../core/image.h"
#include <vector>

// Homography via Eigen SVD (DLT method)
#if __has_include("../../third_party/eigen/Eigen/Dense")
#  include "../../third_party/eigen/Eigen/Dense"
   using Mat3f = Eigen::Matrix3f;
#else
   // Fallback: plain 3x3 float array stored row-major
   struct Mat3f {
       float m[9] = {};
       float& operator()(int r, int c) { return m[r*3+c]; }
       float  operator()(int r, int c) const { return m[r*3+c]; }
   };
#endif

namespace nc {

// Compute 3×3 homography from 4 point correspondences: src → dst
// Uses Eigen JacobiSVD for null-space extraction.
Mat3f computeHomography(const Point2f src[4], const Point2f dst[4]);
Mat3f computeHomography(const std::vector<Point2f>& src, const std::vector<Point2f>& dst);

// Invert a 3×3 matrix
Mat3f invert3x3(const Mat3f& H);

// Apply H to a 2D point (homogeneous division)
Point2f applyHomography(const Mat3f& H, const Point2f& p);

} // namespace nc
