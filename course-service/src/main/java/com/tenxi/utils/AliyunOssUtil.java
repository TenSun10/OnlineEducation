package com.tenxi.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import com.tenxi.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.tenxi.enums.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AliyunOssUtil {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.folder}")
    private String baseFolder;

    private OSS ossClient;

    // 允许的文件类型
    private static final String[] ALLOWED_VIDEO_TYPES = {"mp4", "avi", "mov", "wmv", "flv", "mpeg"};
    private static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    private static final long MAX_VIDEO_SIZE = 1024 * 1024 * 1024L; // 1GB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10MB

    @PostConstruct
    public void init() {
        try {
            // 创建客户端配置
            ClientBuilderConfiguration config = new ClientBuilderConfiguration();
            config.setMaxConnections(200);
            config.setSocketTimeout(10000);
            config.setConnectionTimeout(10000);
            config.setIdleConnectionTime(10000);

            // 创建CredentialsProvider
            CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

            // 创建OSSClient实例
            ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider, config);

            // 检查Bucket是否存在，不存在则创建
            if (!ossClient.doesBucketExist(bucketName)) {
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
                log.info("创建Bucket: {}", bucketName);
            }

            log.info("OSS客户端初始化成功");
        } catch (Exception e) {
            log.error("OSS客户端初始化失败", e);
            throw new RuntimeException("OSS初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    /**
     * 上传视频文件
     */
    public String uploadVideo(MultipartFile file, Long courseId) {
        validateVideoFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName("videos", courseId, fileExtension, "video");

        return uploadFile(file, fileName);
    }

    /**
     * 上传封面图片
     */
    public String uploadCoverImage(MultipartFile file, Long courseId) {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName("covers", courseId, fileExtension, "cover");

        return uploadFile(file, fileName);
    }

    /**
     * 通用文件上传方法
     */
    public String uploadFile(MultipartFile file, String folder, String businessType) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName(folder, null, fileExtension, businessType);

        return uploadFile(file, fileName);
    }

    /**
     * 上传文件到指定路径
     */
    private String uploadFile(MultipartFile file, String fileName) {
        try (InputStream inputStream = file.getInputStream()) {
            // 创建上传对象的元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(getContentType(fileName));
            //设置公共读
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);

            // 上传文件
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("文件上传成功: {}, ETag: {}", fileName, result.getETag());

            // 生成访问URL
            return generateUrl(fileName);

        } catch (IOException e) {
            log.error("文件上传失败: {}", fileName, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 分片上传大文件（适用于大视频文件）
     */
    public String multipartUpload(MultipartFile file, String folder, String businessType) {
        validateVideoFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName(folder, null, fileExtension, businessType);

        try {
            // 初始化分片上传
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, fileName);
            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
            String uploadId = result.getUploadId();

            // 计算分片数量
            long fileSize = file.getSize();
            final long partSize = 5 * 1024 * 1024L; // 5MB per part
            int partCount = (int) (fileSize / partSize);
            if (fileSize % partSize != 0) {
                partCount++;
            }

            // 上传分片
            List<PartETag> partETags = new ArrayList<>();
            try (InputStream inputStream = file.getInputStream()) {
                for (int i = 0; i < partCount; i++) {
                    long startPos = i * partSize;
                    long curPartSize = (i + 1 == partCount) ? (fileSize - startPos) : partSize;

                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(bucketName);
                    uploadPartRequest.setKey(fileName);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setInputStream(inputStream);
                    uploadPartRequest.setPartSize(curPartSize);
                    uploadPartRequest.setPartNumber(i + 1);

                    UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                    partETags.add(uploadPartResult.getPartETag());

                    log.info("分片上传进度: {}/{}", i + 1, partCount);
                }
            }

            // 完成分片上传
            CompleteMultipartUploadRequest completeRequest =
                    new CompleteMultipartUploadRequest(bucketName, fileName, uploadId, partETags);
            ossClient.completeMultipartUpload(completeRequest);

            log.info("分片上传完成: {}", fileName);
            return generateUrl(fileName);

        } catch (Exception e) {
            log.error("分片上传失败: {}", fileName, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName != null) {
                ossClient.deleteObject(bucketName, fileName);
                log.info("文件删除成功: {}", fileName);
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileUrl, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 批量删除文件
     */
    public void batchDeleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();
        for (String fileUrl : fileUrls) {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName != null) {
                fileNames.add(fileName);
            }
        }

        if (!fileNames.isEmpty()) {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(fileNames)
                    .withQuiet(true); // 安静模式，不返回删除结果

            try {
                ossClient.deleteObjects(deleteRequest);
                log.info("批量删除文件成功，数量: {}", fileNames.size());
            } catch (Exception e) {
                log.error("批量删除文件失败", e);
            }
        }
    }

    /**
     * 生成带签名的URL（适用于私有文件）
     */
    public String generatePresignedUrl(String fileUrl, Date expiration) {
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName == null) {
            return null;
        }

        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName);
            request.setExpiration(expiration);
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("生成签名URL失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean doesFileExist(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName == null) {
            return false;
        }

        try {
            return ossClient.doesObjectExist(bucketName, fileName);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public ObjectMetadata getFileMetadata(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName == null) {
            return null;
        }

        try {
            return ossClient.getObjectMetadata(bucketName, fileName);
        } catch (Exception e) {
            log.error("获取文件元数据失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 重命名文件（实际上是复制到新位置并删除原文件）
     */
    public String renameFile(String originalUrl, String folder, Long businessId, String fileType) {
        String originalFileName = extractFileNameFromUrl(originalUrl);
        if (originalFileName == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 生成新的文件名
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = generateFileName(folder, businessId, fileExtension, fileType);

        try {
            // 复制文件到新位置
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, originalFileName,
                    bucketName, newFileName);
            ossClient.copyObject(copyObjectRequest);

            // 删除原文件
            ossClient.deleteObject(bucketName, originalFileName);

            log.info("文件重命名成功: {} -> {}", originalFileName, newFileName);
            return generateUrl(newFileName);

        } catch (Exception e) {
            log.error("文件重命名失败: {} -> {}", originalFileName, newFileName, e);
            throw new BusinessException(ErrorCode.FILE_RENAME_FAILED);
        }
    }

    /**
     * 直接上传文件并包含业务ID（一次性完成）
     */
    public String uploadFileWithBusinessId(MultipartFile file, String folder, Long businessId, String fileType) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName(folder, businessId, fileExtension, fileType);

        return uploadFile(file, fileName);
    }

    // ========== 私有方法 ==========

    private String generateFileName(String folder, Long businessId, String fileExtension, String fileType) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String timestamp = String.valueOf(System.currentTimeMillis());

        StringBuilder fileName = new StringBuilder();
        fileName.append(baseFolder).append(folder).append("/");

        if (businessId != null) {
            fileName.append(businessId).append("/");
        }

        fileName.append(fileType).append("_").append(timestamp).append("_").append(uuid);

        if (fileExtension != null && !fileExtension.isEmpty()) {
            fileName.append(".").append(fileExtension);
        }

        return fileName.toString();
    }

    private String generateUrl(String fileName) {
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }

    private String extractFileNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            log.warn("从URL提取文件名失败: {}", fileUrl);
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String getContentType(String filename) {
        String extension = getFileExtension(filename);
        switch (extension) {
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            default: return "application/octet-stream";
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    private void validateVideoFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        boolean allowed = false;
        for (String allowedType : ALLOWED_VIDEO_TYPES) {
            if (allowedType.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        boolean allowed = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }
}
