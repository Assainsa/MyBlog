package com.lintao.blog.controller;

import com.lintao.blog.utils.QiniuUtils;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private QiniuUtils qiniuUtils;

    @Value("${qiniu.url}")
    private String qiniuUrl;

    /**
     * 上传用户发送的图片文件
     * @param file
     * @return
     */
    @PostMapping
    public Result upload(@RequestParam("image") MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID().toString() + "." + StringUtils.substringAfter(originalFilename, ".");
        boolean upload = qiniuUtils.upload(file, filename);
        if (upload){
            return Result.success(qiniuUrl+filename);
        }
        return Result.fail(ErrorCode.UPLOAD_FAILED.getCode(), ErrorCode.UPLOAD_FAILED.getMsg());
    }
}
