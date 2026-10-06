package com.cookpilot.university.core.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern INLINE_MARKDOWN = Pattern.compile(
            "\\*\\*([^*]+)\\*\\*|\\*([^*\\s][^*]*?)\\*|'([^'\\s][^']*?)'"
    );

    private TextUtil() {
    }

    @NonNull
    public static CharSequence boldMarkdown(@NonNull String text) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        String[] lines = text.split("\\n", -1);

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            String trimmed = line.trim();

            if (trimmed.startsWith("### ")) {
                appendInline(
                        result,
                        trimmed.substring(4),
                        true,
                        true
                );
            } else {
                appendInline(result, line, false, false);
            }

            if (index < lines.length - 1) {
                result.append('\n');
            }
        }

        return result;
    }

    private static void appendInline(
            @NonNull SpannableStringBuilder target,
            @NonNull String text,
            boolean forceBold,
            boolean forceItalic
    ) {
        Matcher matcher = INLINE_MARKDOWN.matcher(text);
        int cursor = 0;

        while (matcher.find()) {
            if (matcher.start() > cursor) {
                appendStyled(
                        target,
                        text.substring(cursor, matcher.start()),
                        forceBold,
                        forceItalic
                );
            }

            String boldText = matcher.group(1);
            String italicText = matcher.group(2);
            String quotedItalicText = matcher.group(3);

            if (boldText != null) {
                appendStyled(
                        target,
                        boldText,
                        true,
                        forceItalic
                );
            } else if (italicText != null) {
                appendStyled(
                        target,
                        italicText,
                        forceBold,
                        true
                );
            } else if (quotedItalicText != null) {
                appendStyled(
                        target,
                        quotedItalicText,
                        forceBold,
                        true
                );
            }

            cursor = matcher.end();
        }

        if (cursor < text.length()) {
            appendStyled(
                    target,
                    text.substring(cursor),
                    forceBold,
                    forceItalic
            );
        }
    }

    private static void appendStyled(
            @NonNull SpannableStringBuilder target,
            @NonNull String text,
            boolean bold,
            boolean italic
    ) {
        int start = target.length();
        target.append(text);
        int end = target.length();

        if (start == end) {
            return;
        }

        int style = Typeface.NORMAL;
        if (bold && italic) {
            style = Typeface.BOLD_ITALIC;
        } else if (bold) {
            style = Typeface.BOLD;
        } else if (italic) {
            style = Typeface.ITALIC;
        }

        if (style != Typeface.NORMAL) {
            target.setSpan(
                    new StyleSpan(style),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
}
