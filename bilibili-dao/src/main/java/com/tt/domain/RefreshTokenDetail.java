package com.tt.domain;

import java.util.Date;

/**
 * ClassName: RefreshTokenDetail
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/6/17 16:18
 */
public class RefreshTokenDetail {
    private Long id;
    private Long userId;
    private String refreshToken;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
