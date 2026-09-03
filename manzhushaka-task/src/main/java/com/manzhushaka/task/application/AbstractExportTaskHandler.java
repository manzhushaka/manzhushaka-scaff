package com.manzhushaka.task.application;

import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * 流式游标导出任务模板。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public abstract class AbstractExportTaskHandler<R> implements TaskHandler
{
    private static final int BATCH_SIZE = 500;
    private static final int ROW_WINDOW_SIZE = 100;

    /** 使用流式工作簿分批写出数据。 */
    @Override
    public final void execute(TaskContext<?> context) throws Exception
    {
        long total = countRows(context);
        long processed = 0L;
        String cursor = null;
        context.report(total, 0L, 0L, 0L);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_WINDOW_SIZE);
                OutputStream outputStream = Files.newOutputStream(context.filePath()))
        {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet(sheetName());
            writeHeader(sheet.createRow(0));
            while (true)
            {
                checkCancelled(context);
                List<R> rows = fetchBatch(context, cursor, BATCH_SIZE);
                if (rows == null || rows.isEmpty())
                {
                    break;
                }
                for (R row : rows)
                {
                    checkCancelled(context);
                    writeRow(sheet.createRow(Math.toIntExact(processed + 1L)), row);
                    processed++;
                }
                cursor = nextCursor(rows.get(rows.size() - 1));
                context.report(total, processed, processed, 0L);
            }
            workbook.write(outputStream);
        }
    }

    protected abstract long countRows(TaskContext<?> context);
    protected abstract String sheetName();
    protected abstract void writeHeader(Row row);
    protected abstract List<R> fetchBatch(TaskContext<?> context, String cursor, int batchSize);
    protected abstract void writeRow(Row row, R value);
    protected abstract String nextCursor(R lastRow);

    /** 检查取消状态。 */
    protected final void checkCancelled(TaskContext<?> context)
    {
        if (context.isCancellationRequested())
        {
            throw new TaskCancelledException();
        }
    }
}
