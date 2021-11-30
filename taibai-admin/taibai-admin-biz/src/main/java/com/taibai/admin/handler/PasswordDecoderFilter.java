/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package com.taibai.admin.handler;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date 2018/7/15 密码解密工具类(配合前端加密使用)
 */
@Slf4j
@Component
public class PasswordDecoderFilter {
//	private static final String KEY_ALGORITHM = "AES";

    /*
     * @Value("${security.encode.key:pigxpigxpigxpigx}") private String encodeKey;
     */

//	@SneakyThrows
//	public static String decryptAES(String data, String pass) {
//		AES aes = new AES(Mode.CBC,Padding.NoPadding,
//				new SecretKeySpec(pass.getBytes(), KEY_ALGORITHM),
//				new IvParameterSpec(pass.getBytes()));
//		byte[] result = aes.decrypt(Base64.decode(data.getBytes(StandardCharsets.UTF_8)));
//		return new String(result, StandardCharsets.UTF_8);
//	}

}
