package com.minio.MinIo;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class MinioAdapter {
    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.name}")
    String defaultBucketName;

    @Value("${minio.default.folder}")
    String defaultBaseFolder;

    public List<Bucket> getAllBucket() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void uploadFile(String name, byte[] content) {
        File file = new File("/tmp" + name);
        file.canWrite();
        file.canRead();
        try {
            FileOutputStream iofs = new FileOutputStream(file);
            iofs.write(content);
            minioClient.putObject(defaultBucketName, defaultBaseFolder + name, file.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getFile(String key) {
        try {
            InputStream obj = minioClient.getObject(defaultBucketName, defaultBaseFolder + "/" + key);
            byte[] content = IOUtils.toByteArray(obj);
            obj.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAll() throws Exception{
        Iterable<Result<Item>> results = minioClient.listObjects(defaultBucketName);
        for (Result<Item> result : results) {
            minioClient.removeObject(defaultBucketName, result.get().objectName());
        }
        if (minioClient.bucketExists(defaultBucketName)) {
            minioClient.removeBucket(defaultBucketName);
        }
    }

    @PostConstruct
    public void init() {

    }
}
