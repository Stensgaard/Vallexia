import DOMPurify from 'dompurify';

/**
 * Sanitizes HTML content to prevent XSS attacks while preserving safe formatting.
 * Allows common formatting tags like <b>, <i>, <a>, <p>, etc., but removes
 * dangerous tags like <script>, <iframe>, and event handlers.
 * 
 * @param {string} html - The HTML string to sanitize
 * @returns {string} - Sanitized HTML safe for rendering
 */
export function sanitizeHtml(html) {
  if (!html || typeof html !== 'string') {
    return '';
  }

  // Configure DOMPurify to allow safe formatting tags
  const config = {
    ALLOWED_TAGS: [
      'b', 'strong', 'i', 'em', 'u', 'p', 'br', 'a', 'ul', 'ol', 'li',
      'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'span', 'div'
    ],
    ALLOWED_ATTR: ['href', 'target', 'rel', 'class'],
    ALLOW_DATA_ATTR: false,
    // Ensure links open safely
    ADD_ATTR: ['target'],
    ADD_TAGS: [],
  };

  // Sanitize the HTML
  const sanitized = DOMPurify.sanitize(html, config);

  // Post-process to ensure external links have rel="noopener noreferrer"
  return sanitized.replace(
    /<a\s+([^>]*href=["'][^"']*["'][^>]*)>/gi,
    (match, attrs) => {
      // Check if target="_blank" is present
      if (attrs.includes('target="_blank"') || attrs.includes("target='_blank'")) {
        // Check if rel is already present
        if (!attrs.includes('rel=')) {
          return `<a ${attrs} rel="noopener noreferrer">`;
        }
      }
      return match;
    }
  );
}

/**
 * Sanitizes HTML for recipe descriptions.
 * More permissive than general sanitization to allow recipe formatting.
 * 
 * @param {string} html - The HTML string to sanitize
 * @returns {string} - Sanitized HTML safe for rendering
 */
export function sanitizeRecipeDescription(html) {
  return sanitizeHtml(html);
}

/**
 * Sanitizes HTML for recipe instructions.
 * Allows basic formatting but ensures safety.
 * 
 * @param {string} html - The HTML string to sanitize
 * @returns {string} - Sanitized HTML safe for rendering
 */
export function sanitizeRecipeInstructions(html) {
  return sanitizeHtml(html);
}
