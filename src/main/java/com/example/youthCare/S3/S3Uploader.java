package com.example.youthCare.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    // 파일 1건 업로드
    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3.getUrl(bucket, fileName).toString(); // 공개 URL 반환
    }

    // 업로드된 파일 URL로 삭제
    public void deleteByUrl(String url) {
        String key = extractKeyFromUrl(url);
        if (key != null && amazonS3.doesObjectExist(bucket, key)) {
            amazonS3.deleteObject(bucket, key);
        }
    }

    // 폴더(prefix) 내 객체 일괄 삭제
    public void deleteFolder(String prefix) {
        // prefix 예: post-images/HOUSING/4/
        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix);

        ListObjectsV2Result result;
        do {
            result = amazonS3.listObjectsV2(req);

            // 키 목록 수집
            List<DeleteObjectsRequest.KeyVersion> keys = result.getObjectSummaries()
                    .stream()
                    .map(S3ObjectSummary::getKey)
                    .map(DeleteObjectsRequest.KeyVersion::new)
                    .collect(java.util.stream.Collectors.toList());

            if (!keys.isEmpty()) {
                try {
                    DeleteObjectsRequest dor = new DeleteObjectsRequest(bucket)
                            .withKeys(keys)        // List<KeyVersion> 사용
                            .withQuiet(true);      // 개별 성공 로그 suppress
                    amazonS3.deleteObjects(dor);
                } catch (MultiObjectDeleteException e) {
                    // 일부 실패가 있어도 나머지는 삭제됨 → 실패 키 로깅
                    for (MultiObjectDeleteException.DeleteError err : e.getErrors()) {
                        System.err.println("S3 delete failed: " + err.getKey() + " - " + err.getCode() + " / " + err.getMessage());
                    }
                    // 필요시 재시도 로직 추가 가능
                }
            }

            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
    }

    // URL에서 S3 key 추출
    private String extractKeyFromUrl(String url) {
        // 네이버 오브젝트 스토리지(엔드포인트 기반) 형태 가정
        // https://kr.object.ncloudstorage.com/{bucket}/<key>
        try {
            String hostPart = "://" ;
            int idx = url.indexOf(hostPart);
            if (idx < 0) return null;
            String afterProto = url.substring(idx + hostPart.length()); // kr.object.../{bucket}/key...
            int firstSlash = afterProto.indexOf('/');
            if (firstSlash < 0) return null;
            String afterHost = afterProto.substring(firstSlash + 1); // {bucket}/key...
            int secondSlash = afterHost.indexOf('/');
            if (secondSlash < 0) return null;
            return afterHost.substring(secondSlash + 1); // key...
        } catch (Exception e) {
            return null;
        }
    }
}
