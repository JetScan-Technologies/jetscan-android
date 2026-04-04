#pragma once
#include <string>

namespace nc {

// Encrypt a PDF with AES-128, user+owner passwords, print+copy permissions.
// Uses PoDoFo. Returns true on success.
bool encryptPdf(const std::string& inputPath, const std::string& outputPath,
                const std::string& userPassword, const std::string& ownerPassword);

// Decrypt (remove password protection) from a PDF.
// Requires supplying the current password.
bool decryptPdf(const std::string& inputPath, const std::string& outputPath,
                const std::string& password);

// Check if a PDF has password protection.
bool pdfHasPassword(const std::string& path);

// Check if the given password opens the PDF.
bool pdfCheckPassword(const std::string& path, const std::string& password);

} // namespace nc
