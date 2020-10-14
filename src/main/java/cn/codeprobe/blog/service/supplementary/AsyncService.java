package cn.codeprobe.blog.service.supplementary;

import cn.codeprobe.blog.utils.send.SendMailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    @Autowired
    private SendMailUtil sendMailUtil;

    @Async
    public void sendEmailVerifyCode(String verifyCode, String emailAddr) throws Exception {
        sendMailUtil.sendVerifyCode(verifyCode, emailAddr);
    }

    @Async
    public void sendCommentNotice(String authorUsername, String articleTitle, String emailAddr) throws Exception {
        sendMailUtil.sendCommentNotice(authorUsername, articleTitle, emailAddr);
    }
}
