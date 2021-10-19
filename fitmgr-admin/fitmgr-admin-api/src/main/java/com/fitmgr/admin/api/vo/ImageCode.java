
package com.fitmgr.admin.api.vo;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @author Fitmgr
 * @date 2017-12-18
 */
@Data
public class ImageCode {
    private String code;

    private LocalDateTime expireTime;

    private BufferedImage image;

    public ImageCode(BufferedImage image, String sRand, int defaultImageExpire) {
        this.image = image;
        this.code = sRand;
        this.expireTime = LocalDateTime.now().plusSeconds(defaultImageExpire);
    }
}
