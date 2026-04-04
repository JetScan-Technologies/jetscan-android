#include "podofo/podofo.h"
#include "pdf_crypto.h"
#include <android/log.h>

#define TAG "nc::pdf_crypto"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

namespace nc {

bool encryptPdf(const std::string& inputPath, const std::string& outputPath,
                const std::string& userPassword, const std::string& ownerPassword) {
    try {
        PoDoFo::PdfMemDocument doc;
        doc.Load(inputPath);
        // AES-128 encryption with print + copy permissions
        doc.SetEncrypted(userPassword, ownerPassword,
                         PoDoFo::PdfPermissions::Print |
                         PoDoFo::PdfPermissions::Copy,
                         PoDoFo::PdfEncryptionAlgorithm::AESV2);
        doc.Save(outputPath);
        LOGI("PDF encrypted: %s", outputPath.c_str());
        return true;
    } catch (const PoDoFo::PdfError& e) {
        LOGE("encryptPdf error: %s", e.what());
        return false;
    }
}

bool decryptPdf(const std::string& inputPath, const std::string& outputPath,
                const std::string& password) {
    try {
        PoDoFo::PdfMemDocument doc;
        doc.Load(inputPath, password);
        // Remove encryption by clearing the encrypt object
        doc.SetEncrypt(nullptr);
        doc.Save(outputPath);
        LOGI("PDF decrypted: %s", outputPath.c_str());
        return true;
    } catch (const PoDoFo::PdfError& e) {
        LOGE("decryptPdf error: %s", e.what());
        return false;
    }
}

bool pdfHasPassword(const std::string& path) {
    try {
        PoDoFo::PdfMemDocument doc;
        doc.Load(path);
        return doc.GetEncrypt() != nullptr;
    } catch (const PoDoFo::PdfError& e) {
        // If error code is PdfErrorCode::InvalidPassword, it has password
        if (e.GetCode() == PoDoFo::PdfErrorCode::InvalidPassword) return true;
        LOGE("pdfHasPassword error: %s", e.what());
        return false;
    }
}

bool pdfCheckPassword(const std::string& path, const std::string& password) {
    try {
        PoDoFo::PdfMemDocument doc;
        doc.Load(path, password);
        return true;
    } catch (const PoDoFo::PdfError&) {
        return false;
    }
}

} // namespace nc
