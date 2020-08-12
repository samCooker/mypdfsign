package cn.com.chaochuang.pdf_operation.model;

import java.util.Date;

/**
 * Created by Shicx on 2020/8/12.
 */
public class DocFlowData {

    /**
     * 环节
     */
    private String nodeId;
    private String nodeName;
    /**
     * 办理候选人 （多个用逗号分开）
     */
    private String candidates;
    /**
     * 办理人
     */
    private Long dealerId;
    private String dealerName;
    /**
     * 办理部门
     */
    private Long dealDeptId;
    private String dealDeptName;
    /**
     * 送达时间
     */
    private Date arrivalTime;
    /**
     * 处理时间
     */
    private Date dealTime;
    /**
     * 办理意见
     */
    private String opinion;
    /**
     * 签批意见
     */
    private String signContent;
    /**
     * 实例状态
     */
    private String status;
    /**
     * 已读标识
     */
    private String readFlag;
    private String dealType;
    private String endCond;
    private String flowDir;
    /**
     * 截止期限
     */
    private Date expiryDate;
    /**
     * 备注
     */
    private String remark;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getCandidates() {
        return candidates;
    }

    public void setCandidates(String candidates) {
        this.candidates = candidates;
    }

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public Long getDealDeptId() {
        return dealDeptId;
    }

    public void setDealDeptId(Long dealDeptId) {
        this.dealDeptId = dealDeptId;
    }

    public String getDealDeptName() {
        return dealDeptName;
    }

    public void setDealDeptName(String dealDeptName) {
        this.dealDeptName = dealDeptName;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Date getDealTime() {
        return dealTime;
    }

    public void setDealTime(Date dealTime) {
        this.dealTime = dealTime;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getSignContent() {
        return signContent;
    }

    public void setSignContent(String signContent) {
        this.signContent = signContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(String readFlag) {
        this.readFlag = readFlag;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public String getEndCond() {
        return endCond;
    }

    public void setEndCond(String endCond) {
        this.endCond = endCond;
    }

    public String getFlowDir() {
        return flowDir;
    }

    public void setFlowDir(String flowDir) {
        this.flowDir = flowDir;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
