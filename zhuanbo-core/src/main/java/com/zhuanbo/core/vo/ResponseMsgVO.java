package com.zhuanbo.core.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseMsgVO {

	private List<NotifyPushMQVO> listNotifyPushMQVO = new ArrayList<NotifyPushMQVO>();
	
	public void addNotifyPushMQVO(NotifyPushMQVO vo) {
		listNotifyPushMQVO.add(vo);
	}
}
