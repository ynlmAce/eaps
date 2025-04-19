/**
 * 格式化时间为相对时间（如：刚刚、5分钟前、1小时前、昨天等）
 * @param {Date|string|number} date 要格式化的日期
 * @returns {string} 格式化后的相对时间字符串
 */
export function formatTimeAgo(date) {
  if (!date) return ''
  
  const now = new Date()
  const dateObj = new Date(date)
  const diffMs = now - dateObj
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)
  
  if (diffSec < 60) {
    return '刚刚'
  } else if (diffMin < 60) {
    return `${diffMin}分钟前`
  } else if (diffHour < 24) {
    return `${diffHour}小时前`
  } else if (diffDay === 1) {
    return '昨天'
  } else if (diffDay === 2) {
    return '前天'
  } else if (diffDay < 7) {
    return `${diffDay}天前`
  } else {
    return formatDate(dateObj)
  }
}

/**
 * 格式化日期为"YYYY-MM-DD"
 * @param {Date|string|number} date 要格式化的日期
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date) {
  if (!date) return ''
  
  const dateObj = new Date(date)
  const year = dateObj.getFullYear()
  const month = (dateObj.getMonth() + 1).toString().padStart(2, '0')
  const day = dateObj.getDate().toString().padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间为"YYYY-MM-DD HH:MM:SS"
 * @param {Date|string|number} date 要格式化的日期
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date) {
  if (!date) return ''
  
  const dateObj = new Date(date)
  const year = dateObj.getFullYear()
  const month = (dateObj.getMonth() + 1).toString().padStart(2, '0')
  const day = dateObj.getDate().toString().padStart(2, '0')
  const hours = dateObj.getHours().toString().padStart(2, '0')
  const minutes = dateObj.getMinutes().toString().padStart(2, '0')
  const seconds = dateObj.getSeconds().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化数字，添加千分位分隔符
 * @param {number} num 要格式化的数字
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num) {
  if (num === undefined || num === null) return ''
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
} 