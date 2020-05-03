package com.zhuanbo.core.storage;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.RedisUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Date;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Yogeek
 * @date 2018/7/16 16:10
 * @decrpt 阿里云对象存储服务
 */
@Data
@Slf4j
public class AliyunStorage implements Storage {


    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String privateBucketName;
    private String url;
    private Long expires;
    private String stsDomain;
    private String roleArn;
    private String roleSessionName;
    private String privateEndpoint;

    private String getBaseUrl() {
        return url;// https://" + bucketName + "." +  endpoint + "/" ;
    }

    /**
     * 阿里云OSS对象存储简单上传实现
     */
    @Override
    public void store(MultipartFile file, String keyName, boolean isPrivate) {
        long l = System.currentTimeMillis();
        log.info("开始上传文件,keyName：{},是否私有：{}", keyName, isPrivate);
        OSS oss = null;
        try {
            // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20M以下的文件使用该接口
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());
            long partSize = 1 * 1024 * 1024L;
            // 对象键（Key）是对象在存储桶中的唯一标识。
            PutObjectResult putObjectResult = null;
            if (isPrivate) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(privateBucketName, keyName, file.getInputStream(), objectMetadata);
                oss = new OSSClientBuilder().build(privateEndpoint, accessKeyId, accessKeySecret);
                putObjectResult = oss.putObject(putObjectRequest);
                log.info("store:etag:{}, requestId:{}", putObjectResult.getETag(), putObjectResult.getRequestId());
            } else {
                commonUpload(file, keyName, objectMetadata);
              /*  if (file.getSize() > partSize) {
                    multipartUpload(file, keyName, objectMetadata);
                } else {

                }*/
            }
            log.info("上传文件结束,keyName：{},耗时；{}", keyName, System.currentTimeMillis() - l);
        } catch (Exception ex) {
            log.error("上传OSS失败:{}", ex);
            throw new ShopException("图片上传失败");
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }

    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public Path load(String keyName) {
        return null;
    }

    @Override
    public Resource loadAsResource(String keyName) {
        try {
            URL url = new URL(getBaseUrl() + keyName);
            Resource resource = new UrlResource(url);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public void delete(String keyName) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, keyName);
        } catch (Exception e) {
            log.error("删除 oss 资源失败，keyName：{}，bucketName：{}", keyName, bucketName);
        } finally {
            ossClient.shutdown();
        }

    }

    @Override
    public String generateUrl(String keyName) {
        log.error("获取OSS路：{}", url);
        return getBaseUrl() + keyName;
    }

    @Override
    public void storeBySteam(String keyName, ByteArrayInputStream byteArrayInputStream) {
        OSS oss = null;
        try {
            oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            oss.putObject(bucketName, keyName, byteArrayInputStream);
        } catch (Exception ex) {
            log.error("上传OSS失败|流|:{}", ex);
            throw new ShopException("图片上传失败");
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }
    }

    @Override
    public String baseUrl() {
        return getBaseUrl();
    }

    public String getStsToken(String url) throws Exception {
        try {
            DefaultProfile.addEndpoint("", "", "Sts", stsDomain);
            // 构造default profile（参数留空，无需添加region ID）
            IClientProfile profile = DefaultProfile.getProfile("", accessKeyId, accessKeySecret);
            // 用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.GET);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(expires); // 设置凭证有效时间
            final AssumeRoleResponse response = client.getAcsResponse(request);
            log.info("AssumeRoleResponse:{}", JSON.toJSONString(response));
            String securityToken = response.getCredentials().getSecurityToken();
            String accessKeyId1 = response.getCredentials().getAccessKeyId();
            String accessKeySecret1 = response.getCredentials().getAccessKeySecret();
            //存缓存
            Map<String, String> map = new HashMap<>();
            map.put("token", securityToken);
            map.put("url", url);
            map.put("privateAccessKeyId", accessKeyId1);
            map.put("privateAccessKeySecret", accessKeySecret1);
            String token = CharUtil.getRandomString(32);
            RedisUtil.set(token, map, expires);
            return token;
        } catch (ClientException e) {
            log.error("获取STS资源临时token失败：{}", e);
            throw new ShopException(e.getMessage());
        }
    }

    public Map<String,Object> getOssResouces(String securityToken, String url, boolean isPrivate) throws Exception {
        OSS oss = null;
        OSSObject object = null;
        try {
            if (isPrivate) {
                Map<String, String> map = (Map) RedisUtil.get(securityToken);
                String token = map.get("token");
                String url1 = map.get("url");
                String privateAccessKeyId = map.get("privateAccessKeyId");
                String privateAccessKeySecret = map.get("privateAccessKeySecret");
                oss = new OSSClientBuilder().build(privateEndpoint, privateAccessKeyId, privateAccessKeySecret, token);
                object = oss.getObject(privateBucketName, new URL(url1).getPath().replaceFirst("/", ""));
            } else {
                oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                object = oss.getObject(bucketName, url);
            }
            Map<String,Object> result = new HashMap<>();
            result.put("OSSObject",object);
            result.put("oss",oss);
            return result;
        } catch (OSSException e) {
            log.error("获取oss图片失败 securityToken：{}，url：{}，error：{}", securityToken, url, e);
            throw new ShopException(e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取oss图片失败 securityToken：{}，url：{}，error：{}", securityToken, url, e);
            throw e;
        }
    }

    //普通上传
    public void commonUpload(MultipartFile file, String keyName, ObjectMetadata objectMetadata) throws Exception {
        OSS oss = null;
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file.getInputStream(), objectMetadata);
            PutObjectResult putObjectResult = null;
            oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            putObjectResult = oss.putObject(putObjectRequest);

            log.info("store:etag:{}, requestId:{}", putObjectResult.getETag(), putObjectResult.getRequestId());
        } catch (Exception e) {
            log.error("普通上传失败，{}", e);
            log.error("普通上传失败，keyName：{}，bucketName：{}", keyName, bucketName);
            throw new ShopException("上传失败");
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }

    }

    //分片上传
    public void multipartUpload(MultipartFile file, String keyName, ObjectMetadata objectMetadata) throws Exception {
        OSS ossClient = null;
        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, keyName);
            request.setObjectMetadata(objectMetadata);
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
            String uploadId = upresult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags = new ArrayList<PartETag>();
            // 计算文件有多少个分片。
            final long partSize = 1 * 1024 * 1024L;   // 1MB
            //   final File sampleFile = new File("<localFile>");
            long fileLength = file.getSize();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;

                InputStream instream = file.getInputStream();
                // 跳过已经上传的分片。
                instream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(keyName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }
            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);

            // 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);

            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            log.info("store:etag:{}, requestId:{}", completeMultipartUploadResult.getETag(), completeMultipartUploadResult.getRequestId());

        } catch (Exception e) {
            log.error("分片上传失败，keyName：{}，bucketName：{}", keyName, bucketName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    public Object stsPostPolicy(Map<String, Object> map) throws Exception {
        String dir = (String) map.get("ossPath");
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", url);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("fileName", (String) map.get("fileName"));
            //回调内容先不要了
            /*JSONObject jasonCallback = new JSONObject();
            jasonCallback.put("callbackUrl", callbackUrl);
            jasonCallback.put("callbackBody", "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
            String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
            respMap.put("callback", base64CallbackBody);*/

            return respMap;
        } catch (Exception e) {
            log.error("获取获取sts上传文件临时权限失败：{}", e);
            return null;
        } finally {
            ossClient.shutdown();
            client.shutdown();
        }
    }

    /**
     * @Description(描述): app获取临时上传权限
     * @auther: Jack Lin
     * @param :[map]
     * @return :java.lang.Object
     * @date: 2019/11/22 16:17
     */
    public Object stsUploadPolicyForApp(String path) throws Exception {
        String policy = "{\"Statement\":[{\"Action\":[\"oss:PutObject\",\"oss:ListParts\",\"oss:AbortMultipartUpload\"],\"Effect\":\"Allow\",\"Resource\":[\"acs:oss:*:*:static-zhuanbo*\"]}],\"Version\":\"1\"}";
        try {
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion("2015-04-01");
            request.setMethod(MethodType.POST);
            request.setProtocol(ProtocolType.HTTPS);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);
            request.setDurationSeconds(expires);
            AssumeRoleResponse stsResponse =  client.getAcsResponse(request);

            Map<String, String> respMap = new LinkedHashMap();
            respMap.put("AccessKeyId", stsResponse.getCredentials().getAccessKeyId());
            respMap.put("AccessKeySecret", stsResponse.getCredentials().getAccessKeySecret());
            respMap.put("SecurityToken", stsResponse.getCredentials().getSecurityToken());
            respMap.put("Expiration", stsResponse.getCredentials().getExpiration());
            respMap.put("path", path);

            return respMap;
        } catch (Exception e) {
            log.error("app获取临时上传权限 失败：{}",e);
            return null;
        }
    }
}
