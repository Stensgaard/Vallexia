/**
 * Utility functions for handling and formatting error messages from API responses.
 * 
 * @module errorUtils
 */

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
      
      return fieldErrors || errorData.message || 'Validation failed'
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
        return 'Invalid input. Please check your data and try again.'
      case 401:
        return 'Your session has expired. Please log in again.'
      case 403:
        return 'You do not have permission to perform this action.'
      case 404:
        return 'The requested resource was not found.'
      case 409:
        return 'This email is already in use. Please use a different email address.'
      case 422:
        return 'The data you provided is invalid. Please check and try again.'
      case 500:
        return 'A server error occurred. Please try again later.'
      default:
        return 'An unexpected error occurred. Please try again.'
    }
  }
  
  // Network or other errors
  if (error.message) {
    if (error.message.includes('timeout')) {
      return 'The request took too long. Please try again.'
    }
    if (error.message.includes('Network Error')) {
      return 'Network error. Please check your connection and try again.'
    }
  }
  
  return 'An unexpected error occurred. Please try again.'
}
