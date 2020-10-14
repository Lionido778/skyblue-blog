package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.ImageDao;
import cn.codeprobe.blog.pojo.Image;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.ImageService;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.utils.common.QrCodeUtil;
import cn.codeprobe.blog.utils.common.RedisUtil;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import cn.codeprobe.blog.utils.common.StringUtil;
import cn.codeprobe.blog.constatnts.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Transactional
@Service
public class ImageServiceImpl extends BaseService implements ImageService {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private RedisUtil redisUtil;

    @Value("${blog.image.store-path}")
    public String imagePath;

    @Value("${blog.image.max-size}")
    public long maxSize;


    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    /**
     * 上传的路径：可以配置，在配置文件里配置
     * 上传的内容，命名-->可以用ID,-->每天一个文件夹保存
     * 限制文件大小，通过配置文件配置
     * 保存记录到数据库里
     * ID｜存储路径｜url｜原名称｜用户ID｜状态｜创建日期｜更新日期
     *
     * @param file
     * @return
     */
    @Override
    public ResponseResult uploadImage(MultipartFile file) {
        //判断是否有文件
        if (file == null) {
            return ResponseResult.FAILED("图片不可以为空");
        }
        //判断文件类型，只支持png，jpg，gif 格式图片上传
        String contentType = file.getContentType();
        if (StringUtil.isEmpty(contentType)) {
            return ResponseResult.FAILED("图片格式错误");
        }
        //获取相关数据，文件类型，文件名称
        String originalFilename = file.getOriginalFilename();
        String type = null;
        log.info("originalFilename ==> " + originalFilename);
        if (Constants.ImageType.TYPE_PNG_WITH_PREFIX.equals(contentType) &&
                originalFilename.endsWith(Constants.ImageType.TYPE_PNG)) {
            type = Constants.ImageType.TYPE_PNG;
        } else if (Constants.ImageType.TYPE_GIF_WITH_PREFIX.equals(contentType) &&
                originalFilename.endsWith(Constants.ImageType.TYPE_GIF)) {
            type = Constants.ImageType.TYPE_GIF;
        } else if (Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType) &&
                originalFilename.endsWith(Constants.ImageType.TYPE_JPG)) {
            type = Constants.ImageType.TYPE_JPG;
        }
        if (type == null) {
            return ResponseResult.FAILED("不支持此图片类型");
        }
        //限制文件大小
        if (file.getSize() > maxSize) {
            return ResponseResult.FAILED("图片上传最大仅支持" + maxSize / 1024 / 1024 + "MB");
        }
        log.info("maxSize ==> " + maxSize + "realSize ==> " + file.getSize());
        //创建图片的保存目录  规则：配置目录/日期/类型/ID.类型
        long currentTimeMillis = System.currentTimeMillis();
        String currentDay = simpleDateFormat.format(currentTimeMillis);
        log.info("current day ==> " + currentDay);
        String dayPath = imagePath + File.separator + currentDay;
        File dayPathFile = new File(dayPath);
        //判断日期文件夹是否存在
        if (dayPathFile != null) {
            dayPathFile.mkdirs();
        }
        //id
        String targetName = String.valueOf(snowflakeIdWorker.nextId());
        //url
        String url = currentTimeMillis + "_" + targetName + "." + type;
        //targetPath
        String targetPath = dayPath + File.separator + type + File.separator + targetName + "." + type;
        File targetFile = new File(targetPath);
        //判断类型文件加是否存在
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        log.info("targetFile ==> " + targetFile);
        //保存文件
        try {
            file.transferTo(targetFile);
            //保存记录到数据库 （originalFilename 上传完之后会消失，所以需要保存上记录）
            Image image = new Image();
            image.setContentType(contentType);
            image.setId(targetName);
            image.setName(originalFilename);
            image.setUrl(url);
            image.setPath(targetPath);
            image.setState("1");
            image.setCreateTime(new Date());
            image.setUpdateTime(new Date());
            //检查用户登录状态获取userId
            User user = userService.checkLoginStatus();
            image.setUserId(user.getId());
            imageDao.save(image);
            //返回结果(url)：包含这个图片的名称和访问路径（时间戳）（目的是为了可以查询到）
            Map<String, Object> result = new HashMap<>();
            result.put("path", url);
            result.put("name", originalFilename);
            return ResponseResult.SUCCESS("图片上传成功").setData(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.FAILED("图片上传失败，请稍后重试");
        }
    }

    @Override
    public void

    viewImage(String imageId, HttpServletResponse response) throws IOException {
        //解析 imageID
        String[] paths = imageId.split("_");
        String dayPath = simpleDateFormat.format(Long.parseLong(paths[0]));
        log.info("viewImage dayPath ==>  " + dayPath);
        String imageName = paths[1];
        log.info("viewImage imageName ==>  " + imageName);
        //获取类型
        String imageType = paths[1].substring(imageName.length() - 3);
        //targetPath
        String targetPath = imagePath + File.separator + dayPath + File.separator + imageType + File.separator + imageName;
        log.info("viewImage targetPath ==>  " + targetPath);
        File file = new File(targetPath);
        OutputStream writer = null;
        InputStream fos = null;
        try {
            response.setContentType("image/png");
            writer = response.getOutputStream();
            //读取
            fos = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len;
            while ((len = fos.read(buff)) != -1) {
                writer.write(buff, 0, len);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public ResponseResult listImages(int page, int size) {
        //检查数据
        page = checkPage(page);
        size = checkSize(size);
        User user = userService.checkLoginStatus();
        //创建条件
        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION.DESC, "createTime");

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        //条件查询 根据 userId 和 state
        Page<Image> all = imageDao.findAll(new Specification<Image>() {
            @Override
            public Predicate toPredicate(Root<Image> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                //根据用户ID AND 状态=:"1"查询
                Predicate p1 = cb.equal(root.get("state").as(String.class), "1");
                Predicate p2 = cb.equal(root.get("userId").as(String.class), user.getId());
                return cb.and(p1, p2);
            }
        }, pageable);
        //返回结果
        return ResponseResult.SUCCESS("图片列表获取成功").setData(all);
    }

    @Override
    public ResponseResult deleteImageByUpdateState(String imageId) {
        int result = imageDao.deleteImageByUpdateState(imageId);
        return result > 0 ? ResponseResult.SUCCESS("图片删除成功") : ResponseResult.FAILED("图片删除失败");
    }

    /**
     * 获取二维码
     *
     * @param code
     * @param response
     */
    @Override
    public void getQrCode(String code, HttpServletResponse response) {
        String loginStatus = (String) redisUtil.get(Constants.User.KEY_QR_CODE + code);
        if (StringUtil.isEmpty(loginStatus)) {
            return;
        }
        String content = "/download/app" + code;
        byte[] result = QrCodeUtil.encodeQRCode(content);
        response.setContentType(QrCodeUtil.RESPONSE_CONTENT_TYPE);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(result);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
