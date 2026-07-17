package com.manzhushaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动程序
 * 
 * @author manzhushaka
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ManzhushakaScaffApplication
{
    private static final Logger log = LoggerFactory.getLogger(ManzhushakaScaffApplication.class);

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        LogPathInitializer.initialize(ManzhushakaScaffApplication.class, args);
        SpringApplication.run(ManzhushakaScaffApplication.class, args);
        log.info("(♥◠‿◠)ﾉﾞ  manzhushaka-scaff 启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
