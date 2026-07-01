package com.manzhushaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author manzhushaka
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ManzhushakaScaffApplication
{
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
        System.out.println("(♥◠‿◠)ﾉﾞ  manzhushaka-scaff 启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
