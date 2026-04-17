package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserLoginVO  implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "用户ID", example = "5")
    private Long id;

    @Schema(description = "用户名/账号", example = "dseam8g")
    private String username;

    @Schema(description = "角色", example = "STUDENT")
    private String role;

    @Schema(description = "昵称", example = "小明", nullable = true)
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.png", nullable = true)
    private String avatar;
}
