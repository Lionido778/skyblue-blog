package cn.codeprobe.blog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.SpringVersion;

@Slf4j
@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BlogApplication.class)
                .main(SpringVersion.class)  // 加载 Spring 版本
                .bannerMode(Banner.Mode.CONSOLE)  // 控制台打印
                .run(args);

        System.out.println("(♥◠‿◠)ﾉﾞ  蔚蓝启动成功   ლ(´ڡ`ლ)ﾞ \n" +
                "\n" +
                "___.   .__                 \n" +
                "\\_ |__ |  |  __ __   ____  \n" +
                " | __ \\|  | |  |  \\_/ __ \\ \n" +
                " | \\_\\ \\  |_|  |  /\\  ___/ \n" +
                " |___  /____/____/  \\___  >\n" +
                "     \\/                 \\/ \n");
    }
}
