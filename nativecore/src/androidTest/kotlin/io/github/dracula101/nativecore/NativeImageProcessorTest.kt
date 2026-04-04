package io.github.dracula101.nativecore

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [NativeImageProcessor].
 * Exercises every JNI entry point using real test images from assets/.
 * Must run on an Android device/emulator (requires native .so loading).
 */
@RunWith(AndroidJUnit4::class)
class NativeImageProcessorTest {

    private lateinit var processor: NativeImageProcessor

    @Before
    fun setUp() {
        processor = NativeImageProcessor()
        val ok = processor.initialize()
        assertTrue("Native library must initialize successfully", ok)
    }

    // ── Asset Loaders ───────────────────────────────────────────────────────

    private fun loadAssetBitmap(name: String): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open(name)
        val bmp = BitmapFactory.decodeStream(stream)
        stream.close()
        assertNotNull("Failed to load asset: $name", bmp)
        return bmp.copy(Bitmap.Config.ARGB_8888, false)
    }

    /** Generates a synthetic NV21 byte array. */
    private fun syntheticNv21(w: Int, h: Int): ByteArray {
        val ySize = w * h
        val uvSize = w * (h / 2)
        val nv21 = ByteArray(ySize + uvSize)
        for (i in 0 until ySize) nv21[i] = 128.toByte()
        for (i in ySize until ySize + uvSize) nv21[i] = 128.toByte()
        return nv21
    }

    /** Creates a simple programmatic bitmap for edge cases. */
    private fun solidBitmap(w: Int = 200, h: Int = 200, color: Int = Color.RED): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(color)
        return bmp
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  1. Initialize
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun initialize_returnsTrue() {
        val fresh = NativeImageProcessor()
        assertTrue(fresh.initialize())
    }

    @Test
    fun initialize_canBeCalledMultipleTimes() {
        assertTrue(processor.initialize())
        assertTrue(processor.initialize())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  2. Detect Document (Path A — Canny + contours)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun detectDocument_withDocumentImage_doesNotCrash() {
        val bmp = loadAssetBitmap("test_document.jpg")
        val coords = processor.detectDocument(bmp)
        // White rect on dark background — may or may not detect
    }

    @Test
    fun detectDocument_withSolidRed_returnsNullOrValid() {
        val bmp = loadAssetBitmap("test_solid_red.jpg")
        processor.detectDocument(bmp) // should not crash
    }

    @Test
    fun detectDocument_withGradient_doesNotCrash() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        processor.detectDocument(bmp)
    }

    @Test
    fun detectDocument_withTinyImage_doesNotCrash() {
        val bmp = loadAssetBitmap("test_tiny.jpg")
        processor.detectDocument(bmp)
    }

    @Test
    fun detectDocument_withLandscapeImage_doesNotCrash() {
        val bmp = loadAssetBitmap("test_landscape.jpg")
        processor.detectDocument(bmp)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  3. Detect Single Document (Path B — LAB + HoughLinesP)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun detectSingleDocument_withDocumentImage_doesNotCrash() {
        val bmp = loadAssetBitmap("test_document.jpg")
        processor.detectSingleDocument(bmp)
    }

    @Test
    fun detectSingleDocument_withLinesImage_doesNotCrash() {
        val bmp = loadAssetBitmap("test_lines.jpg")
        processor.detectSingleDocument(bmp)
    }

    @Test
    fun detectSingleDocument_withSmallBitmap_doesNotCrash() {
        val bmp = loadAssetBitmap("test_tiny.jpg")
        processor.detectSingleDocument(bmp)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  4. Get Lines
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun getLines_withLinesImage_returnsNonNullList() {
        val bmp = loadAssetBitmap("test_lines.jpg")
        val lines = processor.getLines(bmp)
        assertNotNull(lines)
    }

    @Test
    fun getLines_withSolidImage_returnsEmptyOrSmallList() {
        val bmp = loadAssetBitmap("test_solid_red.jpg")
        val lines = processor.getLines(bmp)
        assertNotNull(lines)
    }

    @Test
    fun getLines_withGradient_doesNotCrash() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val lines = processor.getLines(bmp)
        assertNotNull(lines)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  5. Crop Document
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun cropDocument_withValidCorners_returnsBitmap() {
        val bmp = loadAssetBitmap("test_document.jpg") // 640x480
        val coords = ImageCropCoords(
            topLeft     = FloatPair(100f, 80f),
            topRight    = FloatPair(540f, 80f),
            bottomLeft  = FloatPair(100f, 400f),
            bottomRight = FloatPair(540f, 400f)
        )
        val cropped = processor.cropDocument(bmp, coords)
        assertNotNull("cropDocument must return a bitmap", cropped)
        assertTrue("Cropped width must be positive", cropped.width > 0)
        assertTrue("Cropped height must be positive", cropped.height > 0)
    }

    @Test
    fun cropDocument_withTrapezoidCorners_returnsBitmap() {
        val bmp = loadAssetBitmap("test_document.jpg")
        // Simulate perspective distortion
        val coords = ImageCropCoords(
            topLeft     = FloatPair(120f, 90f),
            topRight    = FloatPair(520f, 70f),
            bottomLeft  = FloatPair(80f, 410f),
            bottomRight = FloatPair(560f, 390f)
        )
        val cropped = processor.cropDocument(bmp, coords)
        assertNotNull(cropped)
        assertTrue(cropped.width > 0)
        assertTrue(cropped.height > 0)
    }

    @Test
    fun cropDocument_withFullImageCorners_returnsValidBitmap() {
        val bmp = loadAssetBitmap("test_gradient.jpg") // 200x200
        val w = bmp.width.toFloat()
        val h = bmp.height.toFloat()
        val coords = ImageCropCoords(
            topLeft     = FloatPair(0f, 0f),
            topRight    = FloatPair(w, 0f),
            bottomLeft  = FloatPair(0f, h),
            bottomRight = FloatPair(w, h)
        )
        val cropped = processor.cropDocument(bmp, coords)
        assertNotNull(cropped)
    }

    @Test
    fun cropDocument_withPortraitImage_returnsBitmap() {
        val bmp = loadAssetBitmap("test_portrait.jpg") // 200x400
        val coords = ImageCropCoords(
            topLeft     = FloatPair(20f, 40f),
            topRight    = FloatPair(180f, 40f),
            bottomLeft  = FloatPair(20f, 360f),
            bottomRight = FloatPair(180f, 360f)
        )
        val cropped = processor.cropDocument(bmp, coords)
        assertNotNull(cropped)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  6. Rotate Document
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun rotateDocument_by0Degrees_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val rotated = processor.rotateDocument(bmp, 0f)
        assertNotNull(rotated)
        assertEquals(bmp.width, rotated.width)
        assertEquals(bmp.height, rotated.height)
    }

    @Test
    fun rotateDocument_by90Degrees_returnsValidBitmap() {
        val bmp = loadAssetBitmap("test_landscape.jpg")
        val rotated = processor.rotateDocument(bmp, 90f)
        assertNotNull(rotated)
        assertTrue(rotated.width > 0)
        assertTrue(rotated.height > 0)
    }

    @Test
    fun rotateDocument_by180Degrees_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_solid_red.jpg")
        val rotated = processor.rotateDocument(bmp, 180f)
        assertNotNull(rotated)
        assertEquals(bmp.width, rotated.width)
        assertEquals(bmp.height, rotated.height)
    }

    @Test
    fun rotateDocument_byNegativeDegrees_doesNotCrash() {
        val bmp = loadAssetBitmap("test_portrait.jpg")
        val rotated = processor.rotateDocument(bmp, -45f)
        assertNotNull(rotated)
    }

    @Test
    fun rotateDocument_by360Degrees_returnsValidBitmap() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val rotated = processor.rotateDocument(bmp, 360f)
        assertNotNull(rotated)
        assertEquals(bmp.width, rotated.width)
        assertEquals(bmp.height, rotated.height)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  7. Apply Filter (single) — each of the 7 ImageFilter variants
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun applyFilter_original_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.ORIGINAL)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_vibrant_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.VIBRANT)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_noShadow_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.NO_SHADOW)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_auto_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.AUTO)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_colorBump_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.COLOR_BUMP)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_grayscale_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.GRAYSCALE)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_bw_returnsSameDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyFilter(bmp, ImageFilter.B_W)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyFilter_allFilters_withDocumentImage() {
        val bmp = loadAssetBitmap("test_document.jpg")
        for (filter in ImageFilter.values()) {
            val result = processor.applyFilter(bmp, filter)
            assertNotNull("Filter ${filter.name} must produce a non-null bitmap", result)
            assertEquals("Filter ${filter.name} width", bmp.width, result.width)
            assertEquals("Filter ${filter.name} height", bmp.height, result.height)
        }
    }

    @Test
    fun applyFilter_allFilters_withPortraitImage() {
        val bmp = loadAssetBitmap("test_portrait.jpg")
        for (filter in ImageFilter.values()) {
            val result = processor.applyFilter(bmp, filter)
            assertNotNull("Filter ${filter.name} on portrait must not be null", result)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  8. Apply Filters (batch — all 7 at once)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun applyFilters_returns7Bitmaps() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val results = processor.applyFilters(bmp)
        assertNotNull(results)
        assertEquals(
            "applyFilters must return one bitmap per ImageFilter",
            ImageFilter.values().size,
            results.size
        )
    }

    @Test
    fun applyFilters_allResultsHaveMatchingDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val results = processor.applyFilters(bmp)
        for ((i, result) in results.withIndex()) {
            assertNotNull("Filter result [$i] must not be null", result)
            assertEquals("Filter result [$i] width", bmp.width, result.width)
            assertEquals("Filter result [$i] height", bmp.height, result.height)
        }
    }

    @Test
    fun applyFilters_withLargerImage() {
        val bmp = loadAssetBitmap("test_document.jpg") // 640x480
        val results = processor.applyFilters(bmp)
        assertEquals(ImageFilter.values().size, results.size)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  9. Apply Color Adjustment
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun applyColorAdjustment_neutral_preservesDimensions() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, 0f, 1f, 1f)
        assertNotNull(result)
        assertEquals(bmp.width, result.width)
        assertEquals(bmp.height, result.height)
    }

    @Test
    fun applyColorAdjustment_increaseBrightness() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, 80f, 1f, 1f)
        assertNotNull(result)
    }

    @Test
    fun applyColorAdjustment_decreaseBrightness() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, -80f, 1f, 1f)
        assertNotNull(result)
    }

    @Test
    fun applyColorAdjustment_highContrast() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, 0f, 3f, 1f)
        assertNotNull(result)
    }

    @Test
    fun applyColorAdjustment_highSaturation() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, 0f, 1f, 3f)
        assertNotNull(result)
    }

    @Test
    fun applyColorAdjustment_desaturate() {
        val bmp = loadAssetBitmap("test_gradient.jpg")
        val result = processor.applyColorAdjustment(bmp, 0f, 1f, 0f)
        assertNotNull(result)
    }

    @Test
    fun applyColorAdjustment_extremeValues_doesNotCrash() {
        val bmp = loadAssetBitmap("test_solid_red.jpg")
        processor.applyColorAdjustment(bmp, 255f, 1f, 1f)
        processor.applyColorAdjustment(bmp, -255f, 1f, 1f)
        processor.applyColorAdjustment(bmp, 0f, 10f, 1f)
        processor.applyColorAdjustment(bmp, 0f, 0f, 1f)
        processor.applyColorAdjustment(bmp, 0f, 1f, 5f)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  10. Convert YUV Frame
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun convertYuvFrame_validNv21_returnsRgbaBitmap() {
        val w = 64
        val h = 64
        val nv21 = syntheticNv21(w, h)
        val result = processor.convertYuvFrame(nv21, w, h)
        assertNotNull(result)
        assertEquals(w, result.width)
        assertEquals(h, result.height)
        assertEquals(Bitmap.Config.ARGB_8888, result.config)
    }

    @Test
    fun convertYuvFrame_midGrayNv21_producesGrayPixels() {
        val w = 4
        val h = 4
        val nv21 = syntheticNv21(w, h)
        val result = processor.convertYuvFrame(nv21, w, h)
        assertNotNull(result)

        val pixel = result.getPixel(w / 2, h / 2)
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        assertTrue("Red ≈ 128, got $r", r in 100..160)
        assertTrue("Green ≈ 128, got $g", g in 100..160)
        assertTrue("Blue ≈ 128, got $b", b in 100..160)
    }

    @Test
    fun convertYuvFrame_variousSizes_doesNotCrash() {
        val sizes = listOf(2 to 2, 16 to 16, 32 to 24, 64 to 48, 128 to 96)
        for ((w, h) in sizes) {
            val nv21 = syntheticNv21(w, h)
            val result = processor.convertYuvFrame(nv21, w, h)
            assertNotNull("convertYuvFrame must handle ${w}x${h}", result)
            assertEquals(w, result.width)
            assertEquals(h, result.height)
        }
    }

    @Test
    fun convertYuvFrame_largeFrame_doesNotCrash() {
        val w = 640
        val h = 480
        val nv21 = syntheticNv21(w, h)
        val result = processor.convertYuvFrame(nv21, w, h)
        assertNotNull(result)
        assertEquals(w, result.width)
        assertEquals(h, result.height)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  11. Edge Cases & Robustness
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun applyFilter_withRgb565_convertsAndSucceeds() {
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        bmp.eraseColor(Color.GREEN)
        val result = processor.applyFilter(bmp, ImageFilter.GRAYSCALE)
        assertNotNull("Should handle RGB_565 input via ensureRgba", result)
    }

    @Test
    fun applyFilter_1x1Bitmap_doesNotCrash() {
        val bmp = solidBitmap(w = 1, h = 1)
        for (filter in ImageFilter.values()) {
            val result = processor.applyFilter(bmp, filter)
            assertNotNull("Filter ${filter.name} must handle 1x1", result)
        }
    }

    @Test
    fun applyFilter_tinyAsset_doesNotCrash() {
        val bmp = loadAssetBitmap("test_tiny.jpg") // 4x4
        for (filter in ImageFilter.values()) {
            processor.applyFilter(bmp, filter)
        }
    }

    @Test
    fun cropDocument_thenApplyFilter_chainsCorrectly() {
        val bmp = loadAssetBitmap("test_document.jpg")
        val coords = ImageCropCoords(
            topLeft     = FloatPair(100f, 80f),
            topRight    = FloatPair(540f, 80f),
            bottomLeft  = FloatPair(100f, 400f),
            bottomRight = FloatPair(540f, 400f)
        )
        val cropped = processor.cropDocument(bmp, coords)
        assertNotNull(cropped)

        val filtered = processor.applyFilter(cropped, ImageFilter.GRAYSCALE)
        assertNotNull(filtered)
        assertEquals(cropped.width, filtered.width)
        assertEquals(cropped.height, filtered.height)
    }

    @Test
    fun rotateThenFilter_chainsCorrectly() {
        val bmp = loadAssetBitmap("test_landscape.jpg")
        val rotated = processor.rotateDocument(bmp, 90f)
        assertNotNull(rotated)

        val filtered = processor.applyFilter(rotated, ImageFilter.VIBRANT)
        assertNotNull(filtered)
    }
}
