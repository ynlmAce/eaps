/**
 * 判断字符串是否为外部链接
 * @param {string} path - 要检查的路径
 * @returns {boolean} - 是否为外部链接
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 验证用户名是否合法
 * @param {string} str - 要验证的用户名
 * @returns {boolean} - 是否合法
 */
export function validUsername(str) {
  const validPattern = /^[a-zA-Z0-9_-]{4,16}$/
  return validPattern.test(str)
}

/**
 * 验证URL是否合法
 * @param {string} url - 要验证的URL
 * @returns {boolean} - 是否合法
 */
export function validURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * 验证小写字母
 * @param {string} str - 要验证的字符串
 * @returns {boolean} - 是否全为小写字母
 */
export function validLowerCase(str) {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * 验证大写字母
 * @param {string} str - 要验证的字符串
 * @returns {boolean} - 是否全为大写字母
 */
export function validUpperCase(str) {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * 验证字母
 * @param {string} str - 要验证的字符串
 * @returns {boolean} - 是否全为字母
 */
export function validAlphabets(str) {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

/**
 * 验证邮箱
 * @param {string} email - 要验证的邮箱
 * @returns {boolean} - 是否为合法邮箱
 */
export function validEmail(email) {
  const reg = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

/**
 * 验证手机号
 * @param {string} phone - 要验证的手机号
 * @returns {boolean} - 是否为合法手机号
 */
export function validPhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证是否是数字
 * @param {string} str - 要验证的字符串
 * @returns {boolean} - 是否全为数字
 */
export function isNumber(str) {
  const reg = /^\d+$/
  return reg.test(str)
}

/**
 * 校验密码强度
 * @param {string} password - 要验证的密码
 * @returns {number} - 密码强度等级 (0-4)
 */
export function checkPasswordStrength(password) {
  let strength = 0
  
  // 长度检查
  if (password.length >= 8) strength += 1
  
  // 包含数字
  if (/\d/.test(password)) strength += 1
  
  // 包含小写字母
  if (/[a-z]/.test(password)) strength += 1
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength += 1
  
  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength += 1
  
  return strength
} 