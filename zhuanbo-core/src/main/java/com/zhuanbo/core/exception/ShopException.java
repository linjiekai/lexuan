package com.zhuanbo.core.exception;


import com.zhuanbo.core.util.ApplicationYmlUtil;

public class ShopException extends RuntimeException{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8341547119730680377L;
	private String code = "10520";
    private String msg;

    public ShopException() {
        super("");
    }

    public ShopException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ShopException(Integer code) {
        super(ApplicationYmlUtil.get(code));
        this.code = String.valueOf(code);
        this.msg=ApplicationYmlUtil.get(code);
    }

    public ShopException(Integer code, String msg) {
		super(msg);
		this.code = String.valueOf(code);
		this.msg = msg;
	}
    
    public ShopException(String code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}

    public ShopException(String code, String msg, Throwable e) {
		super(msg, e);
		this.code = code;
		this.msg = msg;
	}
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
