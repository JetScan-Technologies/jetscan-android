package io.github.dracula101.jetscan.data.document.models

enum class IllegalFilenameChar(val value: Char) {

    POUND('#'),
    PERCENT('%'),
    AMPERSAND('&'),
    LEFT_CURLY_BRACKET('{'),
    RIGHT_CURLY_BRACKET('}'),
    BACK_SLASH('\\'),
    LEFT_ANGLE_BRACKET('<'),
    RIGHT_ANGLE_BRACKET('>'),
    ASTERISK('*'),
    QUESTION_MARK('?'),
    FORWARD_SLASH('/'),
    DOLLAR_SIGN('$'),
    EXCLAMATION_POINT('!'),
    SINGLE_QUOTES('\''),
    DOUBLE_QUOTES('"'),
    COLON(':'),
    AT_SIGN('@'),
    PLUS_SIGN('+'),
    BACKTICK('`'),
    PIPE('|'),
    EQUAL_SIGN('=');

    companion object {
        fun isIllegalChar(string: String): Boolean {
            entries.forEach {
                if (string.contains(it.value)) {
                    return true
                }
            }
            return false
        }

        fun removeIllegalChar(string: String): String {
            var result = string
            entries.forEach {
                result = result.replace(it.value.toString(), "")
            }
            return result
        }


    }

}

