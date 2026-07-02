package com.manzhushaka.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.manzhushaka.common.utils.StringUtils;

/**
 * 定时任务调度测试
 * 
 * @author manzhushaka
 */
@Component("scaffTask")
public class ManzhushakaScaffTask
{
    private static final Logger log = LoggerFactory.getLogger(ManzhushakaScaffTask.class);

    public void scaffMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        log.info(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }

    public void scaffParams(String params)
    {
        log.info("执行有参方法：{}", params);
    }

    public void scaffNoParams()
    {
        log.info("执行无参方法");
    }
}
