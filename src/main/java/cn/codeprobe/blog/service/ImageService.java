package cn.codeprobe.blog.service;

import cn.codeprobe.blog.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ImageService {
    ResponseResult uploadImage(MultipartFile file);

    void viewImage(String imageId, HttpServletResponse response) throws IOException;

    ResponseResult listImages(int page, int size);

    ResponseResult deleteImageByUpdateState(String imageId);

    void getQrCode(String code, HttpServletResponse response);

}
