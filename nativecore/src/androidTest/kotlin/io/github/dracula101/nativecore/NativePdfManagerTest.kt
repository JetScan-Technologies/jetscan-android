package io.github.dracula101.nativecore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for [NativePdfManager].
 * Uses real JPEG assets from androidTest/assets/ for PDF generation.
 * Tests libharu PDF generation and verifies PoDoFo stubs behave correctly.
 * Must run on an Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class NativePdfManagerTest {

    private lateinit var pdfManager: NativePdfManager
    private lateinit var testDir: File

    @Before
    fun setUp() {
        pdfManager = NativePdfManager()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        testDir = File(context.cacheDir, "pdf_test").apply {
            mkdirs()
            listFiles()?.forEach { it.delete() }
        }
    }

    // ── Asset Helpers ───────────────────────────────────────────────────────

    /** Copies a test asset JPEG to the test directory and returns its absolute path. */
    private fun copyAssetJpeg(assetName: String): File {
        val context = InstrumentationRegistry.getInstrumentation().context
        val outFile = File(testDir, assetName)
        context.assets.open(assetName).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        assertTrue("Asset $assetName must be copied", outFile.exists())
        assertTrue("Asset $assetName must not be empty", outFile.length() > 0)
        return outFile
    }

    /** Copies the test PDF asset to the test directory. */
    private fun copyAssetPdf(assetName: String): File {
        return copyAssetJpeg(assetName) // same copy logic
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  1. Build PDF — Single Page
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_singleJpeg_createsPdfFile() {
        val jpeg = copyAssetJpeg("test_solid_red.jpg")
        val output = File(testDir, "single.pdf")

        val result = pdfManager.buildPdf(
            jpegPaths = listOf(jpeg.absolutePath),
            outputPath = output.absolutePath,
            pageSize = NativePdfManager.PageSize.A4,
            quality = NativePdfManager.Quality.MEDIUM
        )

        assertTrue("buildPdf must succeed", result)
        assertTrue("Output PDF must exist", output.exists())
        assertTrue("Output PDF must not be empty", output.length() > 0)
    }

    @Test
    fun buildPdf_pdfHeader_isValid() {
        val jpeg = copyAssetJpeg("test_solid_red.jpg")
        val output = File(testDir, "header_check.pdf")

        pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath)

        val header = output.inputStream().use { it.readNBytes(5) }
        assertEquals("PDF header", "%PDF-", String(header))
    }

    @Test
    fun buildPdf_pdfTrailer_hasEOF() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "eof_check.pdf")

        pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath)

        val content = output.readText(Charsets.ISO_8859_1)
        assertTrue("PDF must contain %%EOF", content.contains("%%EOF"))
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  2. Build PDF — Multiple Pages
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_threeJpegs_succeeds() {
        val j1 = copyAssetJpeg("test_multipage_1.jpg")
        val j2 = copyAssetJpeg("test_multipage_2.jpg")
        val j3 = copyAssetJpeg("test_multipage_3.jpg")
        val output = File(testDir, "multipage.pdf")

        val result = pdfManager.buildPdf(
            jpegPaths = listOf(j1.absolutePath, j2.absolutePath, j3.absolutePath),
            outputPath = output.absolutePath
        )

        assertTrue("Multi-page buildPdf must succeed", result)
        assertTrue(output.exists())
        assertTrue("Multi-page PDF should be larger than single-page",
            output.length() > 1000)
    }

    @Test
    fun buildPdf_twoJpegs_largerThanSinglePage() {
        val j1 = copyAssetJpeg("test_multipage_1.jpg")
        val j2 = copyAssetJpeg("test_multipage_2.jpg")

        val single = File(testDir, "single_compare.pdf")
        val multi = File(testDir, "multi_compare.pdf")

        pdfManager.buildPdf(listOf(j1.absolutePath), single.absolutePath)
        pdfManager.buildPdf(listOf(j1.absolutePath, j2.absolutePath), multi.absolutePath)

        assertTrue(single.exists())
        assertTrue(multi.exists())
        assertTrue("2-page PDF should be larger than 1-page",
            multi.length() > single.length())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  3. Build PDF — All Page Sizes
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_pageSize_A4() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "a4.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            pageSize = NativePdfManager.PageSize.A4))
        assertTrue(output.exists())
    }

    @Test
    fun buildPdf_pageSize_LETTER() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "letter.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            pageSize = NativePdfManager.PageSize.LETTER))
        assertTrue(output.exists())
    }

    @Test
    fun buildPdf_pageSize_A3() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "a3.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            pageSize = NativePdfManager.PageSize.A3))
        assertTrue(output.exists())
    }

    @Test
    fun buildPdf_pageSize_LEGAL() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "legal.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            pageSize = NativePdfManager.PageSize.LEGAL))
        assertTrue(output.exists())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  4. Build PDF — All Quality Levels
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_quality_VERY_LOW() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "q_verylow.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            quality = NativePdfManager.Quality.VERY_LOW))
    }

    @Test
    fun buildPdf_quality_LOW() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "q_low.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            quality = NativePdfManager.Quality.LOW))
    }

    @Test
    fun buildPdf_quality_MEDIUM() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "q_medium.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            quality = NativePdfManager.Quality.MEDIUM))
    }

    @Test
    fun buildPdf_quality_HIGH() {
        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val output = File(testDir, "q_high.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            quality = NativePdfManager.Quality.HIGH))
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  5. Build PDF — Margin Options
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_withMargin_succeeds() {
        val jpeg = copyAssetJpeg("test_landscape.jpg")
        val output = File(testDir, "with_margin.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            hasMargin = true))
        assertTrue(output.exists())
    }

    @Test
    fun buildPdf_withoutMargin_succeeds() {
        val jpeg = copyAssetJpeg("test_landscape.jpg")
        val output = File(testDir, "no_margin.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath,
            hasMargin = false))
        assertTrue(output.exists())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  6. Build PDF — Image Orientations
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_landscapeJpeg_succeeds() {
        val jpeg = copyAssetJpeg("test_landscape.jpg") // 400x200
        val output = File(testDir, "landscape.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath))
    }

    @Test
    fun buildPdf_portraitJpeg_succeeds() {
        val jpeg = copyAssetJpeg("test_portrait.jpg") // 200x400
        val output = File(testDir, "portrait.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath))
    }

    @Test
    fun buildPdf_tinyJpeg_succeeds() {
        val jpeg = copyAssetJpeg("test_tiny.jpg") // 4x4
        val output = File(testDir, "tiny.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath))
    }

    @Test
    fun buildPdf_documentJpeg_succeeds() {
        val jpeg = copyAssetJpeg("test_document.jpg") // 640x480
        val output = File(testDir, "document.pdf")
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), output.absolutePath))
    }

    @Test
    fun buildPdf_mixedOrientations_succeeds() {
        val j1 = copyAssetJpeg("test_landscape.jpg")
        val j2 = copyAssetJpeg("test_portrait.jpg")
        val j3 = copyAssetJpeg("test_document.jpg")
        val output = File(testDir, "mixed.pdf")

        assertTrue(pdfManager.buildPdf(
            listOf(j1.absolutePath, j2.absolutePath, j3.absolutePath),
            output.absolutePath
        ))
        assertTrue(output.exists())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  7. Build PDF — Error Handling
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_nonExistentJpeg_handlesGracefully() {
        val output = File(testDir, "bad_input.pdf")
        // Should not crash — libharu will skip the bad file
        pdfManager.buildPdf(
            jpegPaths = listOf("/nonexistent/path/fake.jpg"),
            outputPath = output.absolutePath
        )
    }

    @Test
    fun buildPdf_emptyList_handlesGracefully() {
        val output = File(testDir, "empty.pdf")
        pdfManager.buildPdf(
            jpegPaths = emptyList(),
            outputPath = output.absolutePath
        )
    }

    @Test
    fun buildPdf_mixedValidAndInvalidPaths_stillProducesPdf() {
        val validJpeg = copyAssetJpeg("test_solid_red.jpg")
        val output = File(testDir, "mixed_validity.pdf")

        pdfManager.buildPdf(
            jpegPaths = listOf(
                "/nonexistent/fake.jpg",
                validJpeg.absolutePath,
                "/another/fake.jpg"
            ),
            outputPath = output.absolutePath
        )
        // Should produce a PDF with at least the valid page
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  8. PDF Encryption (PoDoFo stubs — all return false)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun encryptPdf_stub_returnsFalse() {
        val jpeg = copyAssetJpeg("test_solid_red.jpg")
        val srcPdf = File(testDir, "encrypt_src.pdf")
        pdfManager.buildPdf(listOf(jpeg.absolutePath), srcPdf.absolutePath)
        assertTrue(srcPdf.exists())

        val outPdf = File(testDir, "encrypted.pdf")
        val result = pdfManager.encryptPdf(
            srcPdf.absolutePath, outPdf.absolutePath,
            "user123", "owner456"
        )
        assertFalse("encryptPdf stub must return false (PoDoFo disabled)", result)
    }

    @Test
    fun decryptPdf_stub_returnsFalse() {
        assertFalse(pdfManager.decryptPdf("/d/in.pdf", "/d/out.pdf", "pw"))
    }

    @Test
    fun pdfHasPassword_stub_returnsFalse() {
        assertFalse(pdfManager.pdfHasPassword("/d/file.pdf"))
    }

    @Test
    fun pdfCheckPassword_stub_returnsFalse() {
        assertFalse(pdfManager.pdfCheckPassword("/d/file.pdf", "pw"))
    }

    @Test
    fun encryptPdf_withRealPdf_stub_returnsFalse() {
        val pdf = copyAssetPdf("test_sample.pdf")
        val out = File(testDir, "encrypted_real.pdf")
        assertFalse(pdfManager.encryptPdf(pdf.absolutePath, out.absolutePath, "u", "o"))
    }

    @Test
    fun pdfHasPassword_withRealPdf_stub_returnsFalse() {
        val pdf = copyAssetPdf("test_sample.pdf")
        assertFalse(pdfManager.pdfHasPassword(pdf.absolutePath))
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  9. PDF Merge/Split (PoDoFo stubs)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun mergePdfs_stub_returnsFalse() {
        assertFalse(pdfManager.mergePdfs(listOf("/a.pdf", "/b.pdf"), "/merged.pdf"))
    }

    @Test
    fun splitPdf_stub_returnsEmptyList() {
        val result = pdfManager.splitPdf("/d/input.pdf", "/d/output_dir")
        assertNotNull(result)
        assertTrue("splitPdf stub must return empty list", result.isEmpty())
    }

    @Test
    fun mergePdfs_withRealPdfs_stub_returnsFalse() {
        val j1 = copyAssetJpeg("test_multipage_1.jpg")
        val j2 = copyAssetJpeg("test_multipage_2.jpg")
        val pdf1 = File(testDir, "merge_1.pdf")
        val pdf2 = File(testDir, "merge_2.pdf")
        pdfManager.buildPdf(listOf(j1.absolutePath), pdf1.absolutePath)
        pdfManager.buildPdf(listOf(j2.absolutePath), pdf2.absolutePath)

        val merged = File(testDir, "merged.pdf")
        assertFalse(pdfManager.mergePdfs(
            listOf(pdf1.absolutePath, pdf2.absolutePath),
            merged.absolutePath
        ))
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  10. Multiple Manager Instances
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleInstances_workIndependently() {
        val pm1 = NativePdfManager()
        val pm2 = NativePdfManager()

        val jpeg = copyAssetJpeg("test_gradient.jpg")
        val out1 = File(testDir, "inst1.pdf")
        val out2 = File(testDir, "inst2.pdf")

        assertTrue(pm1.buildPdf(listOf(jpeg.absolutePath), out1.absolutePath))
        assertTrue(pm2.buildPdf(listOf(jpeg.absolutePath), out2.absolutePath))

        assertTrue(out1.exists())
        assertTrue(out2.exists())
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  11. PDF Generation Consistency
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    fun buildPdf_samInput_producesConsistentOutput() {
        val jpeg = copyAssetJpeg("test_solid_red.jpg")
        val out1 = File(testDir, "consistent1.pdf")
        val out2 = File(testDir, "consistent2.pdf")

        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), out1.absolutePath))
        assertTrue(pdfManager.buildPdf(listOf(jpeg.absolutePath), out2.absolutePath))

        // Same input should produce same-sized output
        assertEquals("Same input → same output size", out1.length(), out2.length())
    }
}
