import request from '@/utils/request';

/**
 * 用户名+密码登录
 * @param {Object} data - { username, password, role }
 */
export function loginpwd(data) {
  return request({
    url: '/login/loginpwd',
    method: 'post',
    data,
  });
}

/**
 * 手机号+验证码登录
 * @param {Object} data - { phone, catchCode, role }
 */
export function loginphone(data) {
  return request({
    url: '/login/loginphone',
    method: 'post',
    data,
  });
}

/**
 * 发送短信验证码
 * @param {Object} data - { phone: 手机号 }
 * 后端生成验证码存入 Redis，并通过短信发送给用户
 */
export function sendCodeApi(data) {
  return request({
    url: `/login/code`,
    method: 'post',
    data
  });
}

/**
 * 用户注册接口
 * @param {Object} data - { username, phone, password,  role }
 */
export function registerApi(data) {
  return request({
    url: '/login/register',
    method: 'post',
    data
  });
}

/**
 * 忘记密码 — 发送验证码
 * @param {Object} data - { username, phone }
 */
export function forgotPwdCodeApi(data) {
  return request({
    url: '/login/forgot-pwd/code',
    method: 'post',
    data,
  });
}

/**
 * 忘记密码 — 验证验证码
 * @param {Object} data - { username, phone, code }
 */
export function forgotPwdVerifyApi(data) {
  return request({
    url: '/login/forgot-pwd/verify',
    method: 'post',
    data,
  });
}

/**
 * 忘记密码 — 重置密码
 * @param {Object} data - { username, newPassword, confirmPassword, code }
 */
export function forgotPwdResetApi(data) {
  return request({
    url: '/login/forgot-pwd/reset',
    method: 'post',
    data,
  });
}
