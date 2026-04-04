#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

void splitChannels(const Image8& src, std::vector<Image8>& channels);
void mergeChannels(const std::vector<Image8>& channels, Image8& dst);
void extractChannel(const Image8& src, Image8& dst, int channelIdx);
void mixChannels(const Image8& src, Image8& dst, int srcCh, int dstCh);

} // namespace nc
