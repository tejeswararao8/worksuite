/**
 * Triggers a browser file download from a Blob.
 * Centralized here — no component should implement this inline.
 */
export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
}

/** Derives file extension from export format string. */
export function getFileExtension(format) {
  const map = { excel: 'xlsx', csv: 'csv', pdf: 'pdf' };
  return map[format] ?? format;
}
