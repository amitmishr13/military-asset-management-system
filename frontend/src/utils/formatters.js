/**
 * India Localization & Military Logistics Presentation Utilities
 */

/**
 * Formats a numeric value into INR currency format (e.g. ₹1,50,000)
 * @param {number|string} amount 
 * @returns {string} Formatted INR currency string
 */
export const formatINR = (amount) => {
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return '₹0';
  }
  const numericVal = Number(amount);
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  }).format(numericVal);
};

/**
 * Formats ISO or Date string to Indian standard DD/MM/YYYY
 * @param {string|Date} dateInput 
 * @returns {string} Formatted DD/MM/YYYY string
 */
export const formatDateIN = (dateInput) => {
  if (!dateInput) return '-';
  try {
    const d = new Date(dateInput);
    if (isNaN(d.getTime())) return String(dateInput);
    return new Intl.DateTimeFormat('en-IN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(d);
  } catch (err) {
    return String(dateInput);
  }
};

/**
 * Calculates stock operational health badge based on available stock
 * @param {number} availableStock 
 * @returns {{ status: 'HEALTHY'|'LOW'|'CRITICAL', badgeClass: string, label: string }}
 */
export const getInventoryHealthStatus = (availableStock) => {
  const stock = Number(availableStock) || 0;
  if (stock <= 0) {
    return {
      status: 'CRITICAL',
      badgeClass: 'badge-critical',
      label: 'CRITICAL'
    };
  } else if (stock <= 5) {
    return {
      status: 'LOW',
      badgeClass: 'badge-low',
      label: 'LOW STOCK'
    };
  }
  return {
    status: 'HEALTHY',
    badgeClass: 'badge-healthy',
    label: 'HEALTHY'
  };
};

/**
 * Formats raw numbers with locale thousand separators
 * @param {number|string} num 
 * @returns {string}
 */
export const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(Number(num))) return '0';
  return new Intl.NumberFormat('en-IN').format(Number(num));
};
