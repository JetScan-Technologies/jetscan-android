#include "channel_ops.h"
#include <android/log.h>

#define TAG "nc::channel_ops"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

void splitChannels(const Image8& src, std::vector<Image8>& channels) {
    channels.clear();
    if (src.empty()) {
        LOGE("splitChannels: empty source image");
        return;
    }
    for (int c = 0; c < src.channels; ++c) {
        Image8 ch(src.width, src.height, 1);
        for (int y = 0; y < src.height; ++y) {
            const uint8_t* s = src.rowPtr(y) + c;
            uint8_t*       d = ch.rowPtr(y);
            for (int x = 0; x < src.width; ++x, s += src.channels, ++d)
                *d = *s;
        }
        channels.push_back(std::move(ch));
    }
}

void mergeChannels(const std::vector<Image8>& channels, Image8& dst) {
    if (channels.empty()) {
        LOGE("mergeChannels: empty channel list");
        return;
    }
    const int w = channels[0].width, h = channels[0].height;
    if (w <= 0 || h <= 0) {
        LOGE("mergeChannels: invalid channel dimensions");
        return;
    }
    const int nch = static_cast<int>(channels.size());
    dst = Image8(w, h, nch);
    for (int c = 0; c < nch; ++c) {
        if (channels[c].width != w || channels[c].height != h) {
            LOGE("mergeChannels: channel %d dimension mismatch", c);
            return;
        }
        for (int y = 0; y < h; ++y) {
            const uint8_t* s = channels[c].rowPtr(y);
            uint8_t*       d = dst.rowPtr(y) + c;
            for (int x = 0; x < w; ++x, ++s, d += nch)
                *d = *s;
        }
    }
}

void extractChannel(const Image8& src, Image8& dst, int channelIdx) {
    if (src.empty()) {
        LOGE("extractChannel: empty source image");
        return;
    }
    if (channelIdx < 0 || channelIdx >= src.channels) {
        LOGE("extractChannel: invalid channel %d for %d-ch image", channelIdx, src.channels);
        return;
    }
    dst = Image8(src.width, src.height, 1);
    for (int y = 0; y < src.height; ++y) {
        const uint8_t* s = src.rowPtr(y) + channelIdx;
        uint8_t*       d = dst.rowPtr(y);
        for (int x = 0; x < src.width; ++x, s += src.channels, ++d)
            *d = *s;
    }
}

void mixChannels(const Image8& src, Image8& dst, int srcCh, int dstCh) {
    if (src.empty() || dst.empty()) {
        LOGE("mixChannels: empty source or destination image");
        return;
    }
    if (srcCh < 0 || srcCh >= src.channels || dstCh < 0 || dstCh >= dst.channels) {
        LOGE("mixChannels: invalid channel indices (src=%d/%d, dst=%d/%d)",
             srcCh, src.channels, dstCh, dst.channels);
        return;
    }
    if (src.width != dst.width || src.height != dst.height) {
        LOGE("mixChannels: dimension mismatch");
        return;
    }
    for (int y = 0; y < src.height; ++y) {
        const uint8_t* s = src.rowPtr(y) + srcCh;
        uint8_t*       d = dst.rowPtr(y) + dstCh;
        for (int x = 0; x < src.width; ++x, s += src.channels, d += dst.channels)
            *d = *s;
    }
}

} // namespace nc
