package cn.codeprobe.blog.utils.send;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 邮件发送工具类
 */

@Slf4j
@Component
public class SendMailUtil {

    //默认编码
    public static final String DEFAULT_ENCODING = "UTF-8";

    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String FROM;
    @Value("${spring.mail.nickname}")
    private String NICKNAME;


    /************************************ 根据具体情况扩展接口 ******************************/


    public boolean sendVerifyCode(String code, String toAddr) {
        String notice = "账户身份信息验证";
        String content = "您正在注册蔚蓝博客账户，验证码为：" + code + "，在10分钟内有效，若非您本人操作，请忽略本消息！";
        String subject = "用户注册";
        return sendTemplateMail(notice, content, toAddr, subject);
    }

    public boolean sendCommentNotice(String authorUsername, String articleTitle, String toAddr) {
        String notice = "又有新的评论啦！";
        String content = authorUsername + ",您的文章 " + "<span style=\"color: skyblue; font-style: inherit; font-size: 17px;\">"
                + articleTitle + "</span>" + " 又有了一条新的评论，赶快前往查看吧";
        String subject = "评论通知";
        return sendTemplateMail(notice, content, toAddr, subject);
    }


    /****************************************************************************/


    /**
     * 发送简单文本邮件，
     * 唯一收件人
     *
     * @param subject 主题
     * @param text    内容文本
     * @param toOne   收件人
     * @return 发送结果
     */
    public boolean sendText(String subject, String text, String toOne) {
        String[] to = {toOne};
        return sendSimplyMessage(subject, text, to, null);
    }

    public boolean sendText(String subject, String text, String[] toMore) {
        return sendSimplyMessage(subject, text, toMore, null);
    }

    /**
     * 发送Html邮件，
     * 唯一收件人
     *
     * @param subject
     * @param html
     * @param toOne
     * @return
     */
    public boolean sendHtml(String subject, String html, String toOne) {
        String[] to = {toOne};
        return sendHtmlMessage(subject, html, to, null);
    }

    public boolean sendHtml(String subject, String html, String[] toMore) {
        return sendHtmlMessage(subject, html, toMore, null);
    }


    /**
     * 发送一份附件
     * 唯一收件人
     *
     * @param subject
     * @param text
     * @param toOne
     * @param attachmentPath
     * @return
     */
    public boolean sendAttach(String subject, String text, String toOne, String attachmentPath) {
        String[] tos = {toOne};
        String[] attachmentPaths = {attachmentPath};
        return sendSimplyMessage(subject, text, tos, attachmentPaths);
    }

    public boolean sendAttach(String subject, String text, String[] toMore, String attachmentPath) {
        String[] attachmentPaths = {attachmentPath};
        return sendSimplyMessage(subject, text, toMore, attachmentPaths);
    }

    /**
     * 发送多份附件
     * 唯一收件人
     *
     * @param subject
     * @param text
     * @param toOne
     * @param attachmentPaths
     * @return
     */
    public boolean sendAttaches(String subject, String text, String toOne, String[] attachmentPaths) {
        String[] tos = {toOne};
        return sendSimplyMessage(subject, text, tos, attachmentPaths);
    }

    public boolean sendAttaches(String subject, String text, String[] toMore, String[] attachmentPaths) {
        return sendSimplyMessage(subject, text, toMore, attachmentPaths);
    }

    /**
     * 发送简单信息，以及是否携带附件
     *
     * @param subject
     * @param text
     * @param to
     * @param attachments null==>不携带附件
     * @return
     */
    public boolean sendSimplyMessage(String subject, String text, String[] to, String[] attachments) {
        if (SendMailUtil.isEmpty(subject) || SendMailUtil.isEmpty(text) || to == null || to.length == 0) {
            log.error("邮件无法发送，缺少必要的参数！");
        }
        //发送附件，需要处理附件时，需要使用二进制信息，使用 MimeMessage 类来进行处理
        if (attachments != null && attachments.length > 0) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, DEFAULT_ENCODING);
                //配置基本信息
                handleBasicInfo(subject, text, to, helper, false);
                //处理附件
                handleAttachment(subject, attachments, helper);
                //发送
                mailSender.send(mimeMessage);
                log.info("发送邮件成功: 主题->{}", subject);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                log.error("发送邮件失败: 主题->{}", subject);
                return false;
            }
        }
        //发送普通文本
        SimpleMailMessage simpleMsg = new SimpleMailMessage();
        handleBasicInfo(subject, text, to, simpleMsg);
        try {
            mailSender.send(simpleMsg);
            System.out.println("发送简单文本邮件成功");
            return true;
        } catch (Exception e) {
            System.out.println("发送简单文本邮件失败!");
            e.printStackTrace();
            return false;
        }
    }


    public boolean sendHtmlMessage(String subject, String html, String[] to, String[] attachments) {
        if (SendMailUtil.isEmpty(subject) || SendMailUtil.isEmpty(html) || to == null || to.length == 0) {
            log.error("邮件无法发送，缺少必要的参数！");
        }
        //发送附件
        if (attachments != null && attachments.length > 0) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, DEFAULT_ENCODING);
                //配置Html基本信息
                handleBasicInfo(subject, html, to, helper, true);
                //处理附件
                handleAttachment(subject, attachments, helper);
                //发送
                mailSender.send(mimeMessage);
                log.info("发送邮件成功: 主题->{}", subject);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                log.error("发送邮件失败: 主题->{}", subject);
                return false;
            }
        }
        //发送html
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        //配置Html基本信息
        try {
            handleBasicInfo(subject, html, to, helper, true);
            mailSender.send(mimeMessage);
            log.info("html邮件发送成功");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("发送邮件出错->{}", subject);
            return false;
        }
    }


    public boolean sendTemplateMail(String notice, String content, String to, String subject) {
        //创建message
        MimeMessage message = mailSender.createMimeMessage();
        //添加图片
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, DEFAULT_ENCODING);
            helper.setFrom(NICKNAME + '<' + FROM + '>');
            helper.setSubject(subject);
            helper.setText(builder(notice, content), true);
            helper.setTo(to);
            String alarmIconName = "success-alarm.png";
            ClassPathResource img = new ClassPathResource("static/" + alarmIconName);
            if (Objects.nonNull(img)) {
                helper.addInline("icon-alarm", img);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        //发送邮件
        try {
            mailSender.send(message);
            log.info("html邮件发送成功");
            return true;
        } catch (MailException e) {
            e.printStackTrace();
            log.error("发送邮件出错->{}", subject);
            return false;
        }
    }

    private String builder(String message, String mainContent) {
        //加载邮件html模板
        String fileName = "register.html";
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("templates/" + fileName);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = fileReader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            log.error("读取文件失败，fileName:{}", fileName, e);
        } finally {
            try {
                inputStream.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //标头
        String notice = message;
        //主要内容
        String contentText = mainContent;
        //绿色
        String emailHeadColor = "#10fa81";
        //日期
        String date = DateFormatUtils.format(new Date(), "yyyy/MM/dd HH:mm:ss");
        //填充html模板中的五个参数
        String htmlText = MessageFormat.format(buffer.toString(), emailHeadColor, notice, contentText, date);
        return htmlText;
    }


    private void handleAttachment(String subject, String[] attachments, MimeMessageHelper helper) {
        FileSystemResource resource;
        String fileName;
        //循环处理邮件的附件
        for (String attachmentFilePath : attachments) {
            //获取该路径所对应的文件资源对象
            resource = new FileSystemResource(new File(attachmentFilePath));
            //判断该资源是否存在，当不存在时仅仅会打印一条警告日志，不会中断处理程序。
            // 也就是说在附件出现异常的情况下，邮件是可以正常发送的，所以请确定你发送的邮件附件在本机存在
            if (!resource.exists()) {
                log.warn("邮件->{} 的附件->{} 不存在！", subject, attachmentFilePath);
                //开启下一个资源的处理
                continue;
            }
            //获取资源的名称
            fileName = resource.getFilename();
            try {
                //添加附件
                helper.addAttachment(fileName, resource);
            } catch (MessagingException e) {
                e.printStackTrace();
                log.error("邮件->{} 添加附件->{} 出现异常->{}", subject, attachmentFilePath, e.getMessage());
            }
        }
    }

    /**
     * SimpleMailMessage 基本信息配置
     *
     * @param subject
     * @param text
     * @param to
     * @param simpleMailMessage
     */
    private void handleBasicInfo(String subject, String text, String[] to, SimpleMailMessage simpleMailMessage) {
        try {
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(text);
            simpleMailMessage.setFrom(NICKNAME + '<' + FROM + '>');
            simpleMailMessage.setTo(to);
        } catch (Exception e) {
            log.error("邮件基本信息出错-->{}", subject);
        }
    }

    /**
     * MimeMessageHelper 基本信息配置
     *
     * @param subject
     * @param content
     * @param to
     * @param helper
     * @param isHtml
     * @throws MessagingException
     */
    private void handleBasicInfo(String subject, String content, String[] to, MimeMessageHelper helper, boolean isHtml) throws MessagingException {
        try {
            helper.setFrom(NICKNAME + '<' + FROM + '>');
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            helper.setTo(to);
        } catch (MessagingException e) {
            log.error("邮件基本信息出错-->{}", subject);
        }
    }

    /**
     * 字符串判空
     *
     * @param cs 字符串
     * @return
     */
    private static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
