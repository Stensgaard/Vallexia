import DOMPurify from 'dompurify';

// Maximum input length to prevent DoS attacks (1MB)
const MAX_INPUT_LENGTH = 1024 * 1024;

// Flag to track if hook has been registered
let hookRegistered = false;

/**
 * Hook function to add rel="noopener noreferrer" to links with target="_blank".
 * This hook is registered once at module load to ensure all sanitized HTML
 * has proper security attributes on external links.
 * 
 * @param {Node} node - The DOM node being processed by DOMPurify
 */
function addRelAttributeHook(node) {
  // Process anchor tags only
  if (node.tagName !== 'A' || !node.hasAttribute('target')) {
    return;
  }

  const target = node.getAttribute('target');
  
  // If target is "_blank", ensure rel="noopener noreferrer" is present
  if (target === '_blank') {
    const existingRel = node.getAttribute('rel');
    
    if (!existingRel) {
      // No existing rel attribute, add both
      node.setAttribute('rel', 'noopener noreferrer');
    } else {
      // Parse existing rel values to avoid duplicates
      const relValues = existingRel
        .split(/\s+/)
        .filter((val) => val.length > 0)
        .map((val) => val.toLowerCase());
      
      const hasNoopener = relValues.includes('noopener');
      const hasNoreferrer = relValues.includes('noreferrer');
      
      // Only add missing values
      if (!hasNoopener || !hasNoreferrer) {
        if (!hasNoopener) {
          relValues.push('noopener');
        }
        if (!hasNoreferrer) {
          relValues.push('noreferrer');
        }
        node.setAttribute('rel', relValues.join(' '));
      }
    }
  }
}

// Register the hook once at module load
if (!hookRegistered) {
  DOMPurify.addHook('afterSanitizeAttributes', addRelAttributeHook);
  hookRegistered = true;
}

/**
 * Sanitizes HTML content to prevent XSS attacks while preserving safe formatting.
 * Allows common formatting tags like <b>, <i>, <a>, <p>, etc., but removes
 * dangerous tags like <script>, <iframe>, and event handlers.
 * @param {string} html - The HTML string to sanitize
 * @returns {string} - Sanitized HTML safe for rendering
 */
export function sanitizeHtml(html) {
  if (!html || typeof html !== 'string') {
    return '';
  }

  // Input length validation to prevent DoS attacks
  if (html.length > MAX_INPUT_LENGTH) {
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
    ADD_TAGS: [],
  };

  // Sanitize the HTML (hook will automatically process anchor tags)
  return DOMPurify.sanitize(html, config);
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
