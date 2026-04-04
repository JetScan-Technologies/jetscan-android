package io.github.dracula101.nativecore

/**
 * JNI-backed PDF manager (replaces iTextG + PdfManagerImpl).
 * Mirrors the interface expected by the app's DI bindings.
 */
class NativePdfManager {

    init {
        JetScanNative // trigger library load
    }

    enum class PageSize { A4, LETTER, A3, LEGAL }
    enum class Quality  { VERY_LOW, LOW, MEDIUM, HIGH }

    /**
     * Build a PDF from JPEG file paths.
     * @param jpegPaths  list of absolute JPEG file paths
     * @param outputPath destination PDF path
     * @param pageSize   page size enum
     * @param quality    compression quality
     * @param hasMargin  whether to add 20pt margins
     */
    fun buildPdf(
        jpegPaths:  List<String>,
        outputPath: String,
        pageSize:   PageSize = PageSize.A4,
        quality:    Quality  = Quality.MEDIUM,
        hasMargin:  Boolean  = false
    ): Boolean = nativeBuildPdf(
        jpegPaths.toTypedArray(), outputPath,
        pageSize.ordinal, quality.ordinal, hasMargin
    )

    fun encryptPdf(input: String, output: String, userPw: String, ownerPw: String): Boolean =
        nativeEncryptPdf(input, output, userPw, ownerPw)

    fun decryptPdf(input: String, output: String, password: String): Boolean =
        nativeDecryptPdf(input, output, password)

    fun pdfHasPassword(path: String): Boolean =
        nativePdfHasPassword(path)

    fun pdfCheckPassword(path: String, password: String): Boolean =
        nativePdfCheckPassword(path, password)

    fun mergePdfs(paths: List<String>, output: String): Boolean =
        nativeMergePdfs(paths.toTypedArray(), output)

    fun splitPdf(input: String, outputDir: String): List<String> =
        nativeSplitPdf(input, outputDir).toList()

    companion object {
        @JvmStatic private external fun nativeBuildPdf(
            jpegPaths: Array<String>, outputPath: String,
            pageSizeOrdinal: Int, qualityOrdinal: Int, hasMargin: Boolean
        ): Boolean
        @JvmStatic private external fun nativeEncryptPdf(
            input: String, output: String, userPw: String, ownerPw: String
        ): Boolean
        @JvmStatic private external fun nativeDecryptPdf(
            input: String, output: String, password: String
        ): Boolean
        @JvmStatic private external fun nativePdfHasPassword(path: String): Boolean
        @JvmStatic private external fun nativePdfCheckPassword(path: String, password: String): Boolean
        @JvmStatic private external fun nativeMergePdfs(paths: Array<String>, output: String): Boolean
        @JvmStatic private external fun nativeSplitPdf(input: String, outputDir: String): Array<String>
    }
}
