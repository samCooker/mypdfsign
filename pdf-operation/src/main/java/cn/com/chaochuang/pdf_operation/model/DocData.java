package cn.com.chaochuang.pdf_operation.model;

import java.util.List;

/**
 * Created by Shicx on 2020/8/12.
 */
public class DocData {

    private DocAttachData pdfFile;
    private DocAttachData docFile;
    private List<DocAttachData> attachList;
    private List<DocAttachData> pdfFileList;

    public DocAttachData getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(DocAttachData pdfFile) {
        this.pdfFile = pdfFile;
    }

    public DocAttachData getDocFile() {
        return docFile;
    }

    public void setDocFile(DocAttachData docFile) {
        this.docFile = docFile;
    }

    public List<DocAttachData> getAttachList() {
        return attachList;
    }

    public void setAttachList(List<DocAttachData> attachList) {
        this.attachList = attachList;
    }

    public List<DocAttachData> getPdfFileList() {
        return pdfFileList;
    }

    public void setPdfFileList(List<DocAttachData> pdfFileList) {
        this.pdfFileList = pdfFileList;
    }
}
