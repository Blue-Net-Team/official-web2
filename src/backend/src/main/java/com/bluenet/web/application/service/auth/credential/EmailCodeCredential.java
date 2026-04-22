package com.bluenet.web.application.service.auth.credential;

/**
 * 邮箱验证码登录凭证上下文。
 *
 * @param email
 *            邮箱地址。
 * @param verifyCode
 *            邮箱验证码。
 */
public record EmailCodeCredential(String email, String verifyCode) {
}
