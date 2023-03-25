package com.shatyuka.zhiliao.hooks;

import android.annotation.SuppressLint;
import android.widget.Toast;
import com.shatyuka.zhiliao.Helper;
import java.io.File;
import java.io.FilenameFilter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;

public class Cleaner implements IHook {

    @Override
    public String getName() {
        return "临时文件清理";
    }

    static final Pattern regex_cache = Helper.compileRegex("(^\\d{4}-\\d{2}-\\d{2}$)|(^-?\\d+$)|(^[0-9a-fA-F]{32}$)");

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    static class Filter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            if (regex_cache == null) {
                return false;
            }
            return regex_cache.matcher(name).find();
        }
    }

    public static long getFileSize(final File file) {
        if (file.isFile()) {
            return file.length();
        }
        final File[] children = file.listFiles();
        long size = 0;
        if (children != null) {
            for (final File child : children) {
                size += getFileSize(child);
            }
        }
        return size;
    }

    public static long getFileSize(final File[] files) {
        if (files == null) {
            return 0;
        }
        long size = 0;
        for (File file : files) {
            size += getFileSize(file);
        }
        return size;
    }

    public static File[] getCacheFiles() {
        return Helper.context.getFilesDir().listFiles(new Filter());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static long doClean() {
        File[] files = getCacheFiles();
        long size = 0;
        for (File file: files) {
            try {
                long fileSize = getFileSize(file);
                if (file.isDirectory()) {
                    Helper.deleteDirectory(file);
                } else {
                    file.delete();
                }
                size += fileSize;
            } catch (Exception ignored) {
            }
        }
        return size;
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_autoclean", false)) {
            String size = Cleaner.humanReadableByteCount(Cleaner.doClean());
            if (!Helper.prefs.getBoolean("switch_silenceclean", false)) {
                Toast.makeText(Helper.context, "共清理 " + size + " 临时文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void hook() throws Throwable {
    }
}
