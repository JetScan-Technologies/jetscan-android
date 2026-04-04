#include "homography.h"
#include <cmath>
#include <cstring>

namespace nc {

#if __has_include("../../third_party/eigen/Eigen/Dense")

Mat3f computeHomography(const Point2f src[4], const Point2f dst[4]) {
    // DLT: build 8×9 matrix A
    Eigen::Matrix<float, 8, 9> A;
    A.setZero();
    for (int i = 0; i < 4; ++i) {
        float sx = src[i].x, sy = src[i].y;
        float dx = dst[i].x, dy = dst[i].y;
        A.row(2*i)   << -sx, -sy, -1,  0,   0,   0, dx*sx, dx*sy, dx;
        A.row(2*i+1) <<  0,   0,   0, -sx, -sy, -1, dy*sx, dy*sy, dy;
    }
    Eigen::JacobiSVD<Eigen::Matrix<float,8,9>> svd(A, Eigen::ComputeFullV);
    Eigen::Matrix<float,9,1> h = svd.matrixV().col(8);
    Eigen::Matrix3f H;
    H << h(0), h(1), h(2),
         h(3), h(4), h(5),
         h(6), h(7), h(8);
    return H / H(2, 2);
}

Mat3f computeHomography(const std::vector<Point2f>& src, const std::vector<Point2f>& dst) {
    return computeHomography(src.data(), dst.data());
}

Mat3f invert3x3(const Mat3f& H) {
    return H.inverse();
}

Point2f applyHomography(const Mat3f& H, const Point2f& p) {
    Eigen::Vector3f v(p.x, p.y, 1.0f);
    Eigen::Vector3f r = H * v;
    return {r(0) / r(2), r(1) / r(2)};
}

#else

// Scalar fallback using Gaussian elimination
static float det3(const float m[9]) {
    return m[0]*(m[4]*m[8]-m[5]*m[7])
          -m[1]*(m[3]*m[8]-m[5]*m[6])
          +m[2]*(m[3]*m[7]-m[4]*m[6]);
}

Mat3f invert3x3(const Mat3f& H) {
    const float* m = H.m;
    float d = det3(m);
    Mat3f inv;
    if (std::abs(d) < 1e-8f) return inv;
    float id = 1.0f / d;
    inv.m[0] = (m[4]*m[8]-m[5]*m[7])*id;
    inv.m[1] =-(m[1]*m[8]-m[2]*m[7])*id;
    inv.m[2] = (m[1]*m[5]-m[2]*m[4])*id;
    inv.m[3] =-(m[3]*m[8]-m[5]*m[6])*id;
    inv.m[4] = (m[0]*m[8]-m[2]*m[6])*id;
    inv.m[5] =-(m[0]*m[5]-m[2]*m[3])*id;
    inv.m[6] = (m[3]*m[7]-m[4]*m[6])*id;
    inv.m[7] =-(m[0]*m[7]-m[1]*m[6])*id;
    inv.m[8] = (m[0]*m[4]-m[1]*m[3])*id;
    return inv;
}

// Simple DLT without SVD (works for 4-point exact correspondences)
Mat3f computeHomography(const Point2f src[4], const Point2f dst[4]) {
    // Build 8×8 + rhs system from pairs
    // For 4 exact correspondences, solve directly
    // This is simplified — for production we'd use Eigen SVD
    // Using Gaussian elimination on 8x8 linear system
    float A[8][9];
    for (int i = 0; i < 4; ++i) {
        float sx = src[i].x, sy = src[i].y;
        float dx = dst[i].x, dy = dst[i].y;
        A[2*i][0]=-sx; A[2*i][1]=-sy; A[2*i][2]=-1;
        A[2*i][3]=0;  A[2*i][4]=0;  A[2*i][5]=0;
        A[2*i][6]=dx*sx; A[2*i][7]=dx*sy; A[2*i][8]=dx;
        A[2*i+1][0]=0; A[2*i+1][1]=0; A[2*i+1][2]=0;
        A[2*i+1][3]=-sx; A[2*i+1][4]=-sy; A[2*i+1][5]=-1;
        A[2*i+1][6]=dy*sx; A[2*i+1][7]=dy*sy; A[2*i+1][8]=dy;
    }
    // Gaussian elimination (8 equations, fix h[8]=1)
    float b[8];
    float AA[8][8];
    for (int i = 0; i < 8; ++i) {
        b[i] = -A[i][8];
        for (int j = 0; j < 8; ++j) AA[i][j] = A[i][j];
    }
    for (int col = 0; col < 8; ++col) {
        // Pivot
        int pivot = col;
        for (int r = col+1; r < 8; ++r)
            if (std::abs(AA[r][col]) > std::abs(AA[pivot][col])) pivot = r;
        if (pivot != col) {
            std::swap(b[col], b[pivot]);
            for (int j = 0; j < 8; ++j) std::swap(AA[col][j], AA[pivot][j]);
        }
        if (std::abs(AA[col][col]) < 1e-10f) continue;
        float inv = 1.0f / AA[col][col];
        for (int r = col+1; r < 8; ++r) {
            float f = AA[r][col] * inv;
            b[r] -= f * b[col];
            for (int j = col; j < 8; ++j) AA[r][j] -= f * AA[col][j];
        }
    }
    float h[9];
    h[8] = 1.0f;
    for (int i = 7; i >= 0; --i) {
        float s = b[i];
        for (int j = i+1; j < 8; ++j) s -= AA[i][j] * h[j];
        h[i] = (std::abs(AA[i][i]) > 1e-10f) ? s / AA[i][i] : 0.0f;
    }
    Mat3f H;
    std::memcpy(H.m, h, sizeof(h));
    return H;
}

Mat3f computeHomography(const std::vector<Point2f>& src, const std::vector<Point2f>& dst) {
    return computeHomography(src.data(), dst.data());
}

Point2f applyHomography(const Mat3f& H, const Point2f& p) {
    float w = H(2,0)*p.x + H(2,1)*p.y + H(2,2);
    float x = H(0,0)*p.x + H(0,1)*p.y + H(0,2);
    float y = H(1,0)*p.x + H(1,1)*p.y + H(1,2);
    return {x/w, y/w};
}

#endif

} // namespace nc
