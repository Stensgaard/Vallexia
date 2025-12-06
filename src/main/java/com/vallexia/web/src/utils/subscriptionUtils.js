/**
 * Utility functions for subscription and feature access management.
 * 
 * This module provides helper functions to check subscription status
 * and determine feature availability based on subscription tiers.
 * 
 * @module subscriptionUtils
 */

import { getSubscriptionStatuses, createEnumFromList } from '@/utils/localeConfig'

const subscriptionStatuses = getSubscriptionStatuses()
const subscriptionEnum = createEnumFromList(subscriptionStatuses)

const SUBSCRIPTION_CODES = {
  FREE: subscriptionEnum.FREE || 'FREE',
  PREMIUM: subscriptionEnum.PREMIUM || 'PREMIUM',
  FAMILY: subscriptionEnum.FAMILY || 'FAMILY',
  CANCELLED: subscriptionEnum.CANCELLED || 'CANCELLED',
  EXPIRED: subscriptionEnum.EXPIRED || 'EXPIRED'
}

/**
 * Subscription tiers in order of access level
 */
export const SUBSCRIPTION_TIERS = {
  [SUBSCRIPTION_CODES.FREE]: 0,
  [SUBSCRIPTION_CODES.PREMIUM]: 1,
  [SUBSCRIPTION_CODES.FAMILY]: 2
}

/**
 * Check if user has access to a specific subscription tier.
 * 
 * @param {string} userSubscriptionStatus - User's current subscription status
 * @param {string} requiredTier - Required subscription tier (FREE, PREMIUM, FAMILY)
 * @returns {boolean} True if user has access to the required tier
 */
export function hasSubscriptionAccess(userSubscriptionStatus, requiredTier) {
  if (!userSubscriptionStatus) {
    return requiredTier === SUBSCRIPTION_CODES.FREE
  }

  const userTier = SUBSCRIPTION_TIERS[userSubscriptionStatus] || 0
  const requiredTierLevel = SUBSCRIPTION_TIERS[requiredTier] || 0

  return userTier >= requiredTierLevel
}

/**
 * Check if user can access family features (per-person nutrition goals).
 * 
 * @param {string} subscriptionStatus - User's subscription status
 * @returns {boolean} True if user has FAMILY subscription
 */
export function canAccessFamilyFeatures(subscriptionStatus) {
  return hasSubscriptionAccess(subscriptionStatus, SUBSCRIPTION_CODES.FAMILY)
}

/**
 * Check if user can access premium features.
 * 
 * @param {string} subscriptionStatus - User's subscription status
 * @returns {boolean} True if user has PREMIUM or FAMILY subscription
 */
export function canAccessPremiumFeatures(subscriptionStatus) {
  return hasSubscriptionAccess(subscriptionStatus, SUBSCRIPTION_CODES.PREMIUM)
}

/**
 * Check if user should see upgrade prompts for family features.
 * 
 * Shows banner to FREE and PREMIUM users to promote FAMILY subscription tier.
 * 
 * @param {Object} user - User object with householdSize and subscriptionStatus
 * @returns {boolean} True if user has FREE or PREMIUM subscription (not FAMILY)
 */
export function shouldShowFamilyUpgrade(user) {
  if (!user) {
    return false
  }

  // Show banner to FREE and PREMIUM users (promote FAMILY tier)
  return (user.subscriptionStatus === SUBSCRIPTION_CODES.FREE || 
          user.subscriptionStatus === SUBSCRIPTION_CODES.PREMIUM) &&
         user.subscriptionStatus !== SUBSCRIPTION_CODES.CANCELLED &&
         user.subscriptionStatus !== SUBSCRIPTION_CODES.EXPIRED
}

/**
 * Get subscription tier display name.
 * 
 * @param {string} subscriptionStatus - Subscription status
 * @returns {string} Display name for the subscription tier
 */
export function getSubscriptionDisplayName(subscriptionStatus) {
  const displayNames = subscriptionStatuses.reduce((acc, item) => {
    acc[item.code] = item.name
    return acc
  }, {})

  if (!subscriptionStatus) {
    return displayNames[SUBSCRIPTION_CODES.FREE] || 'Free'
  }

  return displayNames[subscriptionStatus] || subscriptionStatus
}
