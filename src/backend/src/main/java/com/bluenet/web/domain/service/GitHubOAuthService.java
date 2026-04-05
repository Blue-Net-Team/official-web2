package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.GitHubUserInfo;

/**
 * GitHub OAuth 领域服务接口
 */
public interface GitHubOAuthService {

    /**
     * 生成 GitHub 授权 URL
     *
     * @param state
     *            随机 state 参数（防 CSRF）
     * @param redirectUri
     *            回调地址
     * @return 授权页面 URL
     */
    String buildAuthorizeUrl(String state, String redirectUri);

    /**
     * 使用 authorization code 换取 access token
     *
     * @param code
     *            GitHub 返回的授权码
     * @param redirectUri
     *            回调地址（必须与授权时一致）
     * @return access token
     */
    String exchangeCodeForToken(String code, String redirectUri);

    /**
     * 使用 access token 获取 GitHub 用户信息
     *
     * @param accessToken
     *            GitHub access token
     * @return GitHub 用户信息
     */
    GitHubUserInfo getUserInfo(String accessToken);
}
