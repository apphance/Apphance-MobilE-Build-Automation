package com.apphance.ameba.documentation

import groovy.xml.MarkupBuilder

class AmebaDocumentationHelper {
    private static MarkupBuilder mb = new MarkupBuilder()

    static final String CODE_BLOCK_HEADER = '<div class="sites-codeblock sites-codesnippet-block"><div>\n'
    static final String CODE_BLOCK_FOOTER = '</div></div>\n'
    static final String BLOCK_LINE_START_GREEN = '<div style="font-family:courier new,monospace"><code><font color="#6aa84f">'
    static final String BLOCK_LINE_START_ORANGE = '<div style="font-family:courier new,monospace"><code><font color="#e69138">'
    static final String BLOCK_LINE_END = '</font></code></div>\n'

    private static String escapeAndNbspText(String text) {
        if (text.length() == 0) {
            return '</div><br><div>'
        } else {
            return mb.escapeXmlValue(text, false).replaceAll('^ *', { match -> '&nbsp;' * match.length() })
        }
    }

    private static String surroundBlockLineOrange(String text) {
        return BLOCK_LINE_START_ORANGE + escapeAndNbspText(text) + BLOCK_LINE_END
    }

    private static String surroundBlockLineGreen(String text) {
        return BLOCK_LINE_START_GREEN + escapeAndNbspText(text) + BLOCK_LINE_END
    }

    private static String surroundBlockLine(String text) {
        if (text.startsWith('#') || text.startsWith('//')) {
            return surroundBlockLineOrange(text)
        } else {
            return surroundBlockLineGreen(text)
        }
    }

    static String getBlockTextGreen(String text) {
        return getBlockTextGreen(text.split('\n'))
    }

    static String getBlockTextGreen(List<String> textList) {
        StringBuilder sb = new StringBuilder()
        sb << CODE_BLOCK_FOOTER
        textList.each { sb << surroundBlockLineGreen(it) }
        sb << CODE_BLOCK_FOOTER
        return sb.toString()
    }

    static String getBlockTextWithComments(String text) {
        return getBlockTextWithComments(text.split('\n'))
    }

    static String getBlockTextWithComments(String[] text) {
        return getBlockTextWithComments(text as List)
    }

    static String getBlockTextWithComments(List<String> textList) {
        StringBuilder sb = new StringBuilder()
        sb << CODE_BLOCK_HEADER
        textList.each {
            sb << surroundBlockLine(it)
        }
        sb << CODE_BLOCK_FOOTER
        return sb.toString()
    }

    static String getHtmlTextFromDescription(String text) {
        return getHtmlTextFromDescription(text.split('\n'))
    }

    static String getHtmlTextFromDescription(String[] text) {
        return getHtmlTextFromDescription(text as List)
    }

    static String getHtmlTextFromDescription(List<String> textList) {
        StringBuilder sb = new StringBuilder()
        boolean inCode = false
        sb << '<div>'
        textList.each {
            if (it.startsWith('<code>')) {
                sb << '</div>'
                sb << CODE_BLOCK_HEADER
                inCode = true
            } else if (it.startsWith('</code>')) {
                sb << CODE_BLOCK_FOOTER
                sb << '<div>'
                inCode = false
            } else {
                if (inCode) {
                    sb << surroundBlockLine(it)
                } else {
                    if (it.startsWith("<")) {
                        sb << it
                    } else {
                        sb << escapeAndNbspText(it)
                    }
                    sb << ' '
                }
            }
        }
        sb << '</div>'
        return sb.toString()
    }
}
