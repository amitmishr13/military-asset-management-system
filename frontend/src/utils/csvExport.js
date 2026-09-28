/**
 * Client-Side CSV Export Utility for Operational Reports
 * Respects current user data access scope without making unsafe backend calls.
 */

/**
 * Converts an array of objects to a CSV string and triggers browser file download.
 * @param {Array<Object>} data List of row data objects
 * @param {Array<{ key: string, label: string }>} columns Mapping of object keys to CSV column headers
 * @param {string} filename Output CSV filename (without extension)
 */
export const exportToCSV = (data, columns, filename = 'export') => {
  if (!data || !data.length || !columns || !columns.length) {
    alert('No data available to export.');
    return;
  }

  // Header line
  const headers = columns.map(col => `"${col.label.replace(/"/g, '""')}"`).join(',');

  // Row lines
  const rows = data.map(row => {
    return columns.map(col => {
      let val = row[col.key];
      if (val === null || val === undefined) {
        val = '';
      } else if (typeof val === 'object') {
        val = val.name || val.code || JSON.stringify(val);
      } else {
        val = String(val);
      }
      return `"${val.replace(/"/g, '""')}"`;
    }).join(',');
  });

  const csvContent = [headers, ...rows].join('\r\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};
