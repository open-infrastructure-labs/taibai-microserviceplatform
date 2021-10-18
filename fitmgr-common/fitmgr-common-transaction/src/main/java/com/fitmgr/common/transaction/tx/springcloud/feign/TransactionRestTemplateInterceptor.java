
package com.fitmgr.common.transaction.tx.springcloud.feign;

import com.codingapi.tx.aop.bean.TxTransactionLocal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Fitmgr
 * @date 2017/6/26.
 * @since 4.1.0
 */
@Slf4j
public class TransactionRestTemplateInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        TxTransactionLocal txTransactionLocal = TxTransactionLocal.current();
        String groupId = txTransactionLocal == null ? null : txTransactionLocal.getGroupId();

        log.info("LCN-SpringCloud TxGroup info -> groupId:" + groupId);

        if (txTransactionLocal != null) {
            requestTemplate.header("tx-group", groupId);
        }
    }

}
