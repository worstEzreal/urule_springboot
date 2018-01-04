package com.example.demo;

import com.bstek.urule.Utils;
import com.bstek.urule.runtime.KnowledgePackage;
import com.bstek.urule.runtime.KnowledgeSession;
import com.bstek.urule.runtime.KnowledgeSessionFactory;
import com.bstek.urule.runtime.service.KnowledgeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc
 *
 * @author zengxzh@yonyou.com
 * @version V1.0.0
 * @date 2018/1/4
 */
@RestController
public class RuleController {
    @RequestMapping("rule")
    public String rule(@RequestParam String data) throws IOException {
        //创建一个KnowledgeSession对象
        KnowledgeService knowledgeService = (KnowledgeService) Utils.getApplicationContext().getBean(KnowledgeService.BEAN_ID);
        KnowledgePackage knowledgePackage = knowledgeService.getKnowledge("aaa/bag");
        KnowledgeSession session = KnowledgeSessionFactory.newKnowledgeSession(knowledgePackage);

        Integer integer = Integer.valueOf(data);
        Map<String, Object> param = new HashMap();
        param.put("var", integer);
        session.fireRules(param);

        Integer result = (Integer) session.getParameter("var");
        return String.valueOf(result);
    }
}
