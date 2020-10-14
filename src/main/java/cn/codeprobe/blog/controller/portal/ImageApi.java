package cn.codeprobe.blog.controller.portal;

import cn.codeprobe.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/portal/image")
public class ImageApi {

    @Autowired
    private ImageService imageService;

    @GetMapping("/{imageId}")
    public void getImage(@PathVariable("imageId") String imageId, HttpServletResponse response) throws IOException {
        imageService.viewImage(imageId, response);
    }

    @GetMapping("/qr-code/{code}")
    public void getQrCode(@PathVariable("code") String code, HttpServletResponse response) {
        imageService.getQrCode(code,response);
    }
}
