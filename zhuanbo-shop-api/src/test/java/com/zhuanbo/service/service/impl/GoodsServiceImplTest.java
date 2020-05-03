package com.zhuanbo.service.service.impl;

import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PushActionEnum;
import com.zhuanbo.core.dto.GoodsDTO;
import com.zhuanbo.core.dto.WithdrDicDTO;
import com.zhuanbo.core.util.ContentUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IPayDictionaryService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import com.zhuanbo.shop.api.ShopApiApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApiApplication.class)
@Slf4j
public class GoodsServiceImplTest {

    @Resource
    private IGoodsService iGoodsService;
    @Resource
    private IPayDictionaryService iPayDictionaryService;

    @Test
    public void findGoodsDTOByGoodsId() throws Exception {
        System.out.println("11213");
        GoodsDTO goodsDTOByGoodsId = iGoodsService.findGoodsDTOByGoodsId(1);
        System.out.println(goodsDTOByGoodsId);
    }

    @Test
    public void dicTest(){
        WithdrDicDTO withdrDic = iPayDictionaryService.getWithdrDic();

        System.out.println(withdrDic);
    }


    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Test
    public void tttestPush() throws Exception{
        // 推送
        NotifyPushMQVO notifyPushMQVO = new NotifyPushMQVO();
        notifyPushMQVO.setTitle("系统通知");
        notifyPushMQVO.setContent("你邀请的好友 " + ContentUtil.starName("张三") +" 注册成功");
        notifyPushMQVO.setExtra(MapUtil.of("type", 3, "link", ""));
        notifyPushMQVO.setMsgFlag(1);
        notifyPushMQVO.setUserId(1L);
        notifyPushMQVO.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        notifyPushMQVO.setNickname("张三");
        notifyPushMQVO.setAction(PushActionEnum.REGISTER.value());

        for(int i = 0; i < 10; i++){
            iRabbitMQSenderService.send(RabbitMQSenderImpl.PUSH_NOTIFY, notifyPushMQVO);
        }
        Thread.sleep(1000000000);
    }
}