export function downloadCsv(filename: string, rows: (string | number)[][]): void {
  const content = '\uFEFF' + rows
    .map(row => row.map(value => `"${String(value ?? '').replaceAll('"', '""')}"`).join(';'))
    .join('\r\n');
  const blob = new Blob([content], {type: 'text/csv;charset=utf-8'});
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
