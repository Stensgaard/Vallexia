/**
 * Utility functions for handling and formatting error messages from API responses.
 * 
 * @module errorUtils
 */

import { i18n } from '../i18n.js'

/**
 * Extracts a user-friendly error message from an API error response.
 * 
 * Handles various error formats:
 * - ErrorResponseDto with message and details (field-level validation errors)
 * - HTTP status code-based fallback messages
 * - Network and timeout errors
 * 
 * @param {Error} error - The error object from axios/API call
 * @returns {string} User-friendly error message
 */
export function getErrorMessage(error) {
  const t = i18n.global.t
  // Check if error has a response with data
  if (error.response?.data) {
    const errorData = error.response.data
    
    // If there are field-level validation errors, format them nicely
    if (errorData.details && Object.keys(errorData.details).length > 0) {
      const fieldErrors = Object.entries(errorData.details)
        .map(([field, message]) => {
          // Convert camelCase to readable format (e.g., "emailAddress" -> "Email Address")
          const readableField = field
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, str => str.toUpperCase())
            .trim()
          
          // Check if message already contains the field name (case-insensitive)
          const messageLower = message.toLowerCase()
          const fieldLower = readableField.toLowerCase()
          
          // If message already mentions the field, don't add prefix
          if (messageLower.includes(fieldLower)) {
            return message
          }
          
          return `${readableField}: ${message}`
        })
        .map(error => `• ${error}`)
        .join('\n')
      
      return fieldErrors || errorData.message || t('errors.validationFailed')
    }
    
    // Use the message from the error response
    if (errorData.message) {
      return errorData.message
    }
  }
  
  // Fallback messages based on status code
  if (error.response) {
    switch (error.response.status) {
      case 400:
        return t('errors.400')
      case 401:
        return t('errors.401')
      case 403:
        return t('errors.403')
      case 404:
        return t('errors.404')
      case 409:
        return t('errors.409')
      case 422:
        return t('errors.422')
      case 500:
        return t('errors.500')
      default:
        return t('errors.default')
    }
  }
  
  // Network or other errors
  if (error.message) {
    if (error.message.includes('timeout')) {
      return t('errors.timeout')
    }
    if (error.message.includes('Network Error')) {
      return t('errors.networkError')
    }
  }
  
  return t('errors.default')
}
