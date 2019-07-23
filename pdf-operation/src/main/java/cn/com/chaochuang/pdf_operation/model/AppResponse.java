package cn.com.chaochuang.pdf_operation.model;


/**
 * @author hzr 2016年5月27日
 *
 */
public class AppResponse {

    public static final String RES_SUCCESS="1";
    public static final String RES_FAILURE="0";

    /**
     * 返回数据请求操作代码（代码：1表示成功，0或其它值表示失败）
     */
    private String success;

    /**
     * 错误信息或者成功信息描述，成功时候可以为空，如果失败请写明失败原因
     */
    private String message;

    /**
     * 返回业务数据对象
     */
    private Object data;

    /**
     * 是否对data加密
     */
    private boolean      encodeFlag;

    public AppResponse() {
    }

    public AppResponse(Object data) {
        super();
        this.success   = "1";
        this.message   = "";
        this.data    = data;
    }

    public AppResponse(String success, String message, Object data) {
        super();
        this.success = success;
        this.message = message;
        this.data  = data;
    }

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

    public boolean isEncodeFlag() {
        return encodeFlag;
    }

    public void setEncodeFlag(boolean encodeFlag) {
        this.encodeFlag = encodeFlag;
    }
}
