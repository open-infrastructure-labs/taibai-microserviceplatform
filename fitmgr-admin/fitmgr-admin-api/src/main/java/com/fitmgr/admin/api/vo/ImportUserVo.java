package com.fitmgr.admin.api.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 用户录入信息类
 *
 * @author Fitmgr
 * @date: 2021年3月18日 下午8:24:12
 */
public class ImportUserVo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6133409020282900083L;

    private String bucket;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 显示名称（历史记录）
     */
    private String name;

    /**
     * 录入总数
     */
    private int total;

    /**
     * 进度
     */
    private int progress;

    /**
     * 成功数
     */
    private int success;

    /**
     * 失败数
     */
    private int fail;

    /**
     * 失败用户下载链接
     */
    private String failLink;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态（0-处理中，1-完成，2-异常）
     */
    private int status;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public String getFailLink() {
        return failLink;
    }

    public void setFailLink(String failLink) {
        this.failLink = failLink;
    }

    public Date getStartTime() {
        Date temp = startTime;
        return temp;
    }

    public void setStartTime(Date startTime) {
        this.startTime = (Date) startTime.clone();
    }

    public Date getEndTime() {
        Date temp = endTime;
        return temp;
    }

    public void setEndTime(Date endTime) {
        this.endTime = (Date) endTime.clone();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ImportUserVo [bucket=" + bucket + ", fileName=" + fileName + ", name=" + name + ", total=" + total
                + ", progress=" + progress + ", success=" + success + ", fail=" + fail + ", failLink=" + failLink
                + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status + "]";
    }

}
