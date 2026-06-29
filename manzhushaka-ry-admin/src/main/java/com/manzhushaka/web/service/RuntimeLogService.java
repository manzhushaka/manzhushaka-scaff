package com.manzhushaka.web.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.web.dto.monitor.RuntimeLogQuery;
import com.manzhushaka.web.vo.monitor.RuntimeLogFileVO;
import com.manzhushaka.web.vo.monitor.RuntimeLogLineVO;
import org.springframework.stereotype.Service;

/**
 * 运行日志在线查看服务。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Service
public class RuntimeLogService {

    private static final Set<String> ALLOWED_FILE_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("sys-info.log", "sys-warn.log", "sys-error.log", "sys-user.log")));

    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "^(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) \\[[^]]+] (TRACE|DEBUG|INFO|WARN|ERROR) .*$");

    private static final int DEFAULT_LINE_COUNT = 500;

    private static final int MAX_LINE_COUNT = 5000;

    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Path logDirectory;

    public RuntimeLogService() {
        this(resolveDefaultLogDirectory());
    }

    public RuntimeLogService(Path logDirectory) {
        this.logDirectory = logDirectory.toAbsolutePath().normalize();
    }

    /**
     * 查询运行日志文件列表。
     *
     * @return 日志文件列表
     */
    public List<RuntimeLogFileVO> listFiles() {
        List<RuntimeLogFileVO> files = new ArrayList<>();
        for (String fileName : ALLOWED_FILE_NAMES) {
            Path path = resolveLogPath(fileName);
            RuntimeLogFileVO file = new RuntimeLogFileVO();
            file.setFileName(fileName);
            if (Files.exists(path)) {
                try {
                    file.setFileSize(Files.size(path));
                    file.setUpdateTime(UPDATE_TIME_FORMATTER.format(Files.getLastModifiedTime(path).toInstant()));
                } catch (IOException ex) {
                    file.setFileSize(0L);
                }
            } else {
                file.setFileSize(0L);
                file.setUpdateTime(UPDATE_TIME_FORMATTER.format(Instant.EPOCH));
            }
            files.add(file);
        }
        return files;
    }

    /**
     * 查询运行日志行。
     *
     * @param query 查询条件
     * @return 日志行
     */
    public List<RuntimeLogLineVO> list(RuntimeLogQuery query) {
        RuntimeLogQuery safeQuery = query == null ? new RuntimeLogQuery() : query;
        Path path = resolveLogPath(safeQuery.getFileName());
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        List<IndexedLine> tailLines = readTailLines(path, normalizeLineCount(safeQuery.getLineCount()));
        return filterAndMerge(tailLines, safeQuery);
    }

    /**
     * 解析日志文件路径。
     *
     * @param fileName 文件名
     * @return 日志文件路径
     */
    public Path resolveLogPath(String fileName) {
        if (!ALLOWED_FILE_NAMES.contains(fileName)) {
            throw new ServiceException("日志文件不允许访问");
        }
        Path path = logDirectory.resolve(fileName).toAbsolutePath().normalize();
        if (!path.startsWith(logDirectory)) {
            throw new ServiceException("日志文件不允许访问");
        }
        return path;
    }

    /**
     * 解析默认日志目录。
     *
     * @return 默认日志目录
     */
    private static Path resolveDefaultLogDirectory() {
        String configuredLogPath = System.getProperty("LOG_PATH");
        if (StringUtils.isEmpty(configuredLogPath)) {
            configuredLogPath = System.getenv("LOG_PATH");
        }
        if (StringUtils.isEmpty(configuredLogPath)) {
            configuredLogPath = "logs";
        }
        return Paths.get(configuredLogPath);
    }

    /**
     * 读取文件尾部指定行数。
     *
     * @param path 文件路径
     * @param lineCount 行数
     * @return 行列表
     */
    private List<IndexedLine> readTailLines(Path path, int lineCount) {
        ArrayDeque<IndexedLine> lines = new ArrayDeque<>(lineCount);
        long lineNumber = 0L;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                lineNumber++;
                if (lines.size() == lineCount) {
                    lines.removeFirst();
                }
                lines.addLast(new IndexedLine(lineNumber, line));
            }
        } catch (IOException ex) {
            throw new ServiceException("读取运行日志失败");
        }
        return new ArrayList<>(lines);
    }

    /**
     * 过滤并合并日志行。
     *
     * @param lines 原始行
     * @param query 查询条件
     * @return 展示行
     */
    private List<RuntimeLogLineVO> filterAndMerge(List<IndexedLine> lines, RuntimeLogQuery query) {
        List<RuntimeLogLineVO> records = new ArrayList<>();
        RuntimeLogLineVO current = null;
        for (IndexedLine indexedLine : lines) {
            Matcher matcher = LOG_LINE_PATTERN.matcher(indexedLine.content());
            if (matcher.matches()) {
                current = toLogLine(indexedLine, matcher);
                records.add(current);
            } else if (current != null) {
                String stackTrace = current.getStackTraceBlock();
                current.setStackTraceBlock((stackTrace == null ? "" : stackTrace + "\n") + indexedLine.content());
            }
        }

        List<RuntimeLogLineVO> filtered = new ArrayList<>();
        for (RuntimeLogLineVO record : records) {
            if (matches(record, query)) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    /**
     * 转换为日志行展示对象。
     *
     * @param indexedLine 行
     * @param matcher 匹配器
     * @return 日志行展示对象
     */
    private RuntimeLogLineVO toLogLine(IndexedLine indexedLine, Matcher matcher) {
        RuntimeLogLineVO line = new RuntimeLogLineVO();
        line.setLineNumber(indexedLine.lineNumber());
        line.setTime(matcher.group(1));
        line.setLevel(matcher.group(2));
        line.setContent(indexedLine.content());
        return line;
    }

    /**
     * 判断是否匹配查询条件。
     *
     * @param record 日志行
     * @param query 查询条件
     * @return true 表示匹配
     */
    private boolean matches(RuntimeLogLineVO record, RuntimeLogQuery query) {
        if (StringUtils.isNotEmpty(query.getLevel())
                && !query.getLevel().equalsIgnoreCase(record.getLevel())) {
            return false;
        }
        if (StringUtils.isEmpty(query.getKeyword())) {
            return true;
        }
        String keyword = query.getKeyword().toLowerCase(Locale.ROOT);
        String content = StringUtils.defaultString(record.getContent()).toLowerCase(Locale.ROOT);
        String stackTraceBlock = StringUtils.defaultString(record.getStackTraceBlock()).toLowerCase(Locale.ROOT);
        return content.contains(keyword) || stackTraceBlock.contains(keyword);
    }

    /**
     * 标准化行数。
     *
     * @param lineCount 行数
     * @return 安全行数
     */
    private int normalizeLineCount(Integer lineCount) {
        if (lineCount == null || lineCount < 1) {
            return DEFAULT_LINE_COUNT;
        }
        return Math.min(lineCount, MAX_LINE_COUNT);
    }

    /**
     * 带行号的日志行。
     *
     * @param lineNumber 行号
     * @param content 内容
     */
    private record IndexedLine(Long lineNumber, String content) {
    }
}
