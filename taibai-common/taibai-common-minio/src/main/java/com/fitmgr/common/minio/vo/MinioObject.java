
package com.fitmgr.common.minio.vo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.minio.ObjectStat;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 存储对象的元数据
 *
 * @author Fitmgr
 */
@Data
@AllArgsConstructor
public class MinioObject {
    private String bucketName;
    private String name;
    private Date createdTime;
    private Long length;
    private String etag;
    private String contentType;
    private Map<String, List<String>> httpHeaders;

    public Date getCreatedTime() {
        Date temp = createdTime;
        return temp;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = (Date) createdTime.clone();
    }

    public MinioObject(ObjectStat os) {
        this.bucketName = os.bucketName();
        this.name = os.name();
        this.createdTime = os.createdTime();
        this.length = os.length();
        this.etag = os.etag();
        this.contentType = os.contentType();
        this.httpHeaders = os.httpHeaders();
    }

}
