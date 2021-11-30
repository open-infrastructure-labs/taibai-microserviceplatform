
package com.fitmgr.common.transaction;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fitmgr
 * @date 2017/6/26.
 * @since 4.1.0
 */

@Configuration
@ComponentScan({ "com.codingapi.tx", "com.fitmgr.common.transaction" })
public class TransactionConfiguration {

}
