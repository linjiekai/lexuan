package com.zhuanbo.shop.api.scheduled;

import com.zhuanbo.core.qrcode.IQrCodeService;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.external.service.wx.service.impl.IWeixinThirdServiceImpl;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IUpgradeDetailsService;
import com.zhuanbo.service.service.IUserBuyInviteCodeService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MQMessageScheduledTest {

    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    @Qualifier("weixinThirdService")
    private IWeixinThirdServiceImpl iWeixinThirdService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService iOrderService;
    @Value("${server.port}")
    private String port;
    @Autowired
    private IQrCodeService iQrCodeService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    private IUserBuyInviteCodeService iUserBuyInviteCodeService;
    @Autowired
    private IUpgradeDetailsService iUpgradeDetailsService;

    @Before
    public void before(){

//        /SpringContextUtil.setApplicationContext(ctx);
    }

    @Test
    public void execute() {
        /*iMqMessageService.page(new Page<>(1, 100), new QueryWrapper<MqMessage>()
                .eq("status", MQMessageStatusEnum.STATUS_0.value()).last("and length(exchange) > 0 and length(routing_key) > 0 limit 100"));*/
        /*MqMessage mqMessage = new MqMessage();
        mqMessage.setId(1L);
        mqMessage.setTimes(1);
        MqMessage mm = new MqMessage();
        boolean update = iMqMessageService.update(mm, new UpdateWrapper<MqMessage>().set("times", mqMessage.getTimes() + 1).eq("id", mqMessage.getId()).eq("times", mqMessage.getTimes()));
        System.out.println(update);*/

        //rabbitTemplate.convertAndSend("mppartnerE", "zhuanbo.order.profit.dev", "{}");
    }

    @Test
    public void mpMobile(){
        /*String code = "023XQ5Kq0Cbb0m1M2kIq0JEOJq0XQ5KL";
        String e = "dPeiTVhNMBhtt6mNRhyFc2as+n/qASJ2AJVQfs5FFINiLu6y7+CleslE9STDrkQqWlQ30B9rF78lIQB1frszMwR45f86Ev8hE93hMIXR+auYVx86PC5f3JSvoPHue2RtyfjX5zJ1IDEp1xpRkL3IBuF8ZK2YzsIPc/BZF9Z4U3w1K1naAMiJB4eS1X31eqTVkG3pbfTNvrfFXpa6XbNc+g==";
        String iv = "bt9g5p3PmtueVcfgpoG8OQ==";

        String s2 = iWeixinThirdService.mpMobile(code, e, iv);
        System.out.println(s2);*/
        /*redisTemplate.delete("xfhl-zhuanbo-"+ConstantsEnum.REDIS_INVITE_CODE_SET.stringValue());
        redisTemplate.delete("xfhl-zhuanbo-"+ConstantsEnum.REDIS_INVITE_CODE_MAX.stringValue());*/

        /*Order order = new Order();
        order.setNickname("nnn");
        order.setId(4561L);
        iOrderService.updateById(order);*/

        System.out.println("===" + port);
    }

    @Test
    public void ddddsfs() {
        String make = iQrCodeService.make("我的天朝呀。。。", 300, 300);
        System.out.println(make);
    }


    @Test
    public void makeusertoken() {
        RedisUtil.set("11486", 11486L);
        //RedisUtil.set("12345678", 11555L);
        //redisTemplate.opsForValue().set(RedisUtil.KEY_PRE + "12345678", 11555L);
    }

    @Test
    public void makeusertoke2n() {

        RedisUtil.del("zhuanbo:ticket:wx3fb8ccb54d4a1417");
        /*String s = "{\"bankCode\":\"ALIPAY\",\"mercId\":\"888000000000004\",\"openId\":\"\",\"orderDate\":\"2020-04-27\",\"orderNo\":\"2020042700231490\",\"orderStatus\":\"S\",\"orderTime\":\"20:29:34\",\"payDate\":\"2020-04-27\",\"payNo\":\"202004270000010804\",\"payTime\":\"20:29:46\",\"price\":0.01,\"tradeType\":\"APP\",\"userId\":\"11244\"}";
        iUpgradeDetailsService.generateDetail(JSON.parseObject(s, PayNotifyParamsVO.class));*/
        /*BuyInviteCodeDTO buyInviteCodeDTO = new BuyInviteCodeDTO();
        buyInviteCodeDTO.setUserId(11664L);
        buyInviteCodeDTO.setBuyInviteCode("146124S5");
        iUserBuyInviteCodeService.checkCode(buyInviteCodeDTO);*/
    }
}
