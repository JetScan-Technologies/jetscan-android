cmake_minimum_required(VERSION 3.23.0...3.28.0)
project(nativecore CXX C)

set(CMAKE_CXX_STANDARD          17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD            11)
set(CMAKE_C_STANDARD_REQUIRED   ON)

include(ExternalProject)

# ── Validate Android toolchain ────────────────────────────────────────────────
if(NOT DEFINED ANDROID_ABI)
    message(FATAL_ERROR "ANDROID_ABI is not set. Use the Android CMake toolchain.")
endif()
if(NOT DEFINED CMAKE_TOOLCHAIN_FILE)
    message(FATAL_ERROR "CMAKE_TOOLCHAIN_FILE is not set.")
endif()

# ── SIMD flags ────────────────────────────────────────────────────────────────
if(ANDROID_ABI STREQUAL "arm64-v8a")
    add_compile_options(-march=armv8-a+simd)
elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
    add_compile_options(-mfpu=neon -mfloat-abi=softfp)
elseif(ANDROID_ABI STREQUAL "x86_64")
    add_compile_options(-msse4.2)
elseif(ANDROID_ABI STREQUAL "x86")
    add_compile_options(-msse3)
endif()

# ── Directories ───────────────────────────────────────────────────────────────
set(TP  "${CMAKE_CURRENT_SOURCE_DIR}/third_party")
set(BIN "${CMAKE_BINARY_DIR}")

if(NOT IS_DIRECTORY "${TP}")
    message(FATAL_ERROR
            "third_party/ directory not found.\n"
            "Run: ./setup_third_party.sh")
endif()

# ── Helper: require third-party dir ──────────────────────────────────────────
function(require_tp _subdir _sentinel)
    if(NOT EXISTS "${TP}/${_subdir}/${_sentinel}")
        message(FATAL_ERROR
                "${_subdir} not found (expected ${TP}/${_subdir}/${_sentinel}).\n"
                "Run: ./setup_third_party.sh")
    endif()
endfunction()

# ── OpenSSL (Prefab AAR) ──────────────────────────────────────────────────────
find_package(openssl REQUIRED CONFIG)

foreach(_tgt openssl::ssl openssl::crypto)
    if(NOT TARGET ${_tgt})
        message(FATAL_ERROR "OpenSSL target '${_tgt}' not found after find_package.")
    endif()
endforeach()

get_target_property(OPENSSL_INC    openssl::ssl    INTERFACE_INCLUDE_DIRECTORIES)
get_target_property(OPENSSL_SSL    openssl::ssl    IMPORTED_LOCATION)
get_target_property(OPENSSL_CRYPTO openssl::crypto IMPORTED_LOCATION)

foreach(_var OPENSSL_INC OPENSSL_SSL OPENSSL_CRYPTO)
    if(NOT ${_var})
        message(FATAL_ERROR "Could not read ${_var} from OpenSSL Prefab targets.")
    endif()
endforeach()

message(STATUS "OpenSSL include : ${OPENSSL_INC}")
message(STATUS "OpenSSL ssl     : ${OPENSSL_SSL}")
message(STATUS "OpenSSL crypto  : ${OPENSSL_CRYPTO}")

# ── Header-only targets ───────────────────────────────────────────────────────
require_tp(eigen   "Eigen/Core")
require_tp(stb     "stb_image.h")
require_tp(nanosvg "src/nanosvg.h")

foreach(_hdr_lib eigen stb nanosvg)
    add_library(${_hdr_lib} INTERFACE)
endforeach()

target_include_directories(eigen   INTERFACE "${TP}/eigen")
target_include_directories(stb     INTERFACE "${TP}/stb")
target_include_directories(nanosvg INTERFACE "${TP}/nanosvg/src")

# ── libyuv ────────────────────────────────────────────────────────────────────
require_tp(libyuv "CMakeLists.txt")
add_subdirectory("${TP}/libyuv" third_party/libyuv)

# ── libwebp ───────────────────────────────────────────────────────────────────
require_tp(libwebp "CMakeLists.txt")
foreach(_opt ANIM_UTILS CWEBP DWEBP GIF2WEBP IMG2WEBP VWEBP WEBPINFO WEBPMUX EXTRAS)
    set(WEBP_BUILD_${_opt} OFF CACHE BOOL "" FORCE)
endforeach()
add_subdirectory("${TP}/libwebp" third_party/libwebp)

# ── tinyxml2 ──────────────────────────────────────────────────────────────────
require_tp(tinyxml2 "tinyxml2.cpp")
add_library(tinyxml2 STATIC "${TP}/tinyxml2/tinyxml2.cpp")
target_include_directories(tinyxml2 PUBLIC "${TP}/tinyxml2")
target_compile_options(tinyxml2 PRIVATE -w)

# ── libharu ───────────────────────────────────────────────────────────────────
require_tp(libharu "src/hpdf_doc.c")

set(HARU_CONFIG_DIR "${BIN}/libharu_config")
file(MAKE_DIRECTORY "${HARU_CONFIG_DIR}")

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/cmake/hpdf_config.h.in")
    configure_file(
            "${CMAKE_CURRENT_SOURCE_DIR}/cmake/hpdf_config.h.in"
            "${HARU_CONFIG_DIR}/hpdf_config.h"
            @ONLY
    )
else()
    file(WRITE "${HARU_CONFIG_DIR}/hpdf_config.h"
            "/* Auto-generated for Android */\n"
            "#ifndef _HPDF_CONFIG_H\n"
            "#define _HPDF_CONFIG_H\n"
            "#define LIBHPDF_HAVE_ZLIB\n"
            "#endif\n"
    )
endif()

file(GLOB HARU_SOURCES "${TP}/libharu/src/*.c")
list(FILTER HARU_SOURCES EXCLUDE REGEX ".*(hpdf_doc_png|hpdf_image_png)\\.c$")
if(NOT HARU_SOURCES)
    message(FATAL_ERROR "No libharu source files found in ${TP}/libharu/src/")
endif()

add_library(haru STATIC ${HARU_SOURCES})
target_include_directories(haru PUBLIC "${TP}/libharu/include" "${HARU_CONFIG_DIR}")
target_compile_definitions(haru PRIVATE LIBHPDF_HAVE_ZLIB)
target_compile_options(haru PRIVATE -w)
target_link_libraries(haru PRIVATE z)

# ── ExternalProject common args ───────────────────────────────────────────────
set(NATIVECORE_MIN_SDK "android-21")

set(EP_TOOLCHAIN
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DCMAKE_SYSTEM_NAME=Android
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${NATIVECORE_MIN_SDK}
        -DCMAKE_BUILD_TYPE=Release
        -DCMAKE_CXX_STANDARD=17
        -DCMAKE_C_STANDARD=11
        -DCMAKE_POLICY_VERSION_MINIMUM=3.5
        -DBUILD_SHARED_LIBS=OFF
        -DCMAKE_OSX_SYSROOT=
        -DCMAKE_OSX_DEPLOYMENT_TARGET=
)

# ── ep_add(<name> <src_dir> <byproduct_subpath> [extra cmake args...]) ────────
macro(ep_add _name _src _lib)
    set(_install "${BIN}/${_name}-install")
    set(_byproduct "${_install}/${_lib}")

    ExternalProject_Add(${_name}_build
            SOURCE_DIR  "${_src}"
            BINARY_DIR  "${BIN}/${_name}-build"
            INSTALL_DIR "${_install}"
            CMAKE_ARGS
            ${EP_TOOLCHAIN}
            -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR>
            ${ARGN}
            BUILD_BYPRODUCTS "${_byproduct}"
            LOG_CONFIGURE        ON
            LOG_BUILD            ON
            LOG_INSTALL          ON
            LOG_OUTPUT_ON_FAILURE ON
    )

    add_library(${_name}_imp STATIC IMPORTED GLOBAL)
    add_dependencies(${_name}_imp ${_name}_build)
    set_target_properties(${_name}_imp PROPERTIES
            IMPORTED_LOCATION "${_byproduct}")
endmacro()

# ── FreeType 2.13.2 ───────────────────────────────────────────────────────────
require_tp(freetype "include/freetype/freetype.h")
ep_add(freetype "${TP}/freetype" lib/libfreetype.a
        -DFT_DISABLE_ZLIB=ON
        -DFT_DISABLE_BZIP2=ON
        -DFT_DISABLE_PNG=ON
        -DFT_DISABLE_HARFBUZZ=ON
        -DFT_DISABLE_BROTLI=ON
)
set(FREETYPE_INC "${BIN}/freetype-install/include/freetype2")
set(FREETYPE_LIB "${BIN}/freetype-install/lib/libfreetype.a")

# ── libxml2 2.12.6 ────────────────────────────────────────────────────────────
require_tp(libxml2 "include/libxml/parser.h")
ep_add(libxml2 "${TP}/libxml2" lib/libxml2.a
        -DLIBXML2_WITH_PYTHON=OFF
        -DLIBXML2_WITH_LZMA=OFF
        -DLIBXML2_WITH_ICONV=OFF
        -DLIBXML2_WITH_ICU=OFF
        -DLIBXML2_WITH_PROGRAMS=OFF
        -DLIBXML2_WITH_TESTS=OFF
        -DLIBXML2_WITH_ZLIB=ON
        "-DCMAKE_C_FLAGS=-Wno-implicit-function-declaration"
)
set(LIBXML2_INC "${BIN}/libxml2-install/include/libxml2")
set(LIBXML2_LIB "${BIN}/libxml2-install/lib/libxml2.a")

# ── libjpeg-turbo 3.0.3 ───────────────────────────────────────────────────────
require_tp(libjpeg-turbo "jpeglib.h")
ep_add(libjpeg "${TP}/libjpeg-turbo" lib/libjpeg.a
        -DENABLE_SHARED=OFF
        -DENABLE_STATIC=ON
        -DWITH_TURBOJPEG=OFF
)
set(LIBJPEG_INC "${BIN}/libjpeg-install/include")
set(LIBJPEG_LIB "${BIN}/libjpeg-install/lib/libjpeg.a")

# ── libpng 1.6.43 ─────────────────────────────────────────────────────────────
require_tp(libpng "png.h")
ep_add(libpng "${TP}/libpng" lib/libpng16.a
        -DPNG_SHARED=OFF
        -DPNG_STATIC=ON
        -DPNG_TESTS=OFF
        -DPNG_EXECUTABLES=OFF
)
set(LIBPNG_INC "${BIN}/libpng-install/include")
set(LIBPNG_LIB "${BIN}/libpng-install/lib/libpng16.a")

# ── PoDoFo 1.0.3 ──────────────────────────────────────────────────────────────
require_tp(podofo "CMakeLists.txt")

set(_podofo_install "${BIN}/podofo-install")
set(_podofo_libs
        "${_podofo_install}/lib/libpodofo.a"
        "${_podofo_install}/lib/libpodofo_private.a"
        "${_podofo_install}/lib/libpodofo_3rdparty.a"
)

ExternalProject_Add(podofo_build
        SOURCE_DIR  "${TP}/podofo"
        BINARY_DIR  "${BIN}/podofo-build"
        INSTALL_DIR "${_podofo_install}"
        DEPENDS
        freetype_build
        libxml2_build
        libjpeg_build
        libpng_build
        INSTALL_COMMAND
        ${CMAKE_COMMAND} --build <BINARY_DIR> --target install
        COMMAND ${CMAKE_COMMAND} -E make_directory
        "${_podofo_install}/include/podofo"
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        "${BIN}/podofo-build/src/podofo/podofo/auxiliary/podofo_config.h"
        "${_podofo_install}/include/podofo/podofo_config.h"
        CMAKE_ARGS
        ${EP_TOOLCHAIN}
        -DCMAKE_INSTALL_PREFIX=<INSTALL_DIR>
        -DPODOFO_BUILD_LIB_ONLY=ON
        -DPODOFO_BUILD_STATIC=ON
        -DPODOFO_BUILD_TESTS=OFF
        -DPODOFO_BUILD_EXAMPLES=OFF
        -DOPENSSL_INCLUDE_DIR=${OPENSSL_INC}
        -DOPENSSL_SSL_LIBRARY=${OPENSSL_SSL}
        -DOPENSSL_CRYPTO_LIBRARY=${OPENSSL_CRYPTO}
        -DFREETYPE_INCLUDE_DIRS=${FREETYPE_INC}
        -DFREETYPE_LIBRARY=${FREETYPE_LIB}
        -DLIBXML2_INCLUDE_DIR=${LIBXML2_INC}
        -DLIBXML2_LIBRARY=${LIBXML2_LIB}
        -DJPEG_INCLUDE_DIR=${LIBJPEG_INC}
        -DJPEG_LIBRARY=${LIBJPEG_LIB}
        -DPNG_PNG_INCLUDE_DIR=${LIBPNG_INC}
        -DPNG_LIBRARY=${LIBPNG_LIB}
        BUILD_BYPRODUCTS ${_podofo_libs}
        LOG_CONFIGURE        ON
        LOG_BUILD            ON
        LOG_INSTALL          ON
        LOG_OUTPUT_ON_FAILURE ON
)

foreach(_lib podofo podofo_private podofo_3rdparty)
    add_library(${_lib}_imp STATIC IMPORTED GLOBAL)
    set_target_properties(${_lib}_imp PROPERTIES
            IMPORTED_LOCATION "${_podofo_install}/lib/lib${_lib}.a")
    add_dependencies(${_lib}_imp podofo_build)
endforeach()

# ── jetscan shared library ────────────────────────────────────────────────────
file(GLOB_RECURSE NATIVECORE_SOURCES
        image/core/*.cpp        image/codec/*.cpp
        image/color/*.cpp       image/filter/*.cpp
        image/morphology/*.cpp  image/threshold/*.cpp
        image/normalize/*.cpp   image/pixel_ops/*.cpp
        image/channel_ops/*.cpp image/edge/*.cpp
        image/hough/*.cpp       image/geometry/*.cpp
        image/document/*.cpp    image/effect/*.cpp
        image/extra/*.cpp
        pdf/generation/*.cpp    pdf/crypto/*.cpp
        pdf/manipulation/*.cpp  pdf/metadata/*.cpp
        jni/*.cpp
)

if(NOT NATIVECORE_SOURCES)
    message(FATAL_ERROR "No source files found for jetscan — check source directory layout.")
endif()

add_library(jetscan SHARED ${NATIVECORE_SOURCES})

target_compile_definitions(jetscan PRIVATE
        PODOFO_STATIC
        _CRT_SECURE_NO_WARNINGS
)

target_include_directories(jetscan PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}"
        "${TP}/libyuv/include"
        "${TP}/libharu/include"
        "${_podofo_install}/include"
        "${OPENSSL_INC}"
        "${FREETYPE_INC}"
        "${LIBXML2_INC}"
        "${LIBJPEG_INC}"
        "${LIBPNG_INC}"
)

add_dependencies(jetscan
        podofo_imp podofo_private_imp podofo_3rdparty_imp
        freetype_imp libxml2_imp libjpeg_imp libpng_imp
)

target_link_libraries(jetscan PRIVATE
        eigen stb nanosvg
        yuv webp webpdecoder tinyxml2 haru
        podofo_imp podofo_private_imp podofo_3rdparty_imp
        freetype_imp libxml2_imp libjpeg_imp libpng_imp
        openssl::ssl openssl::crypto
        jnigraphics android log z
)