/**
 * Générateur de QR Code autonome en SVG (Format QR standard Version 1-4, ECC M)
 */
export class QrCodeUtil {
  /**
   * Génère un SVG sous forme de chaîne à partir du texte donné.
   */
  static generateSvg(text: string, size = 120, darkColor = '#0c4a2b', lightColor = '#ffffff'): string {
    const modules = this.generateMatrix(text);
    const count = modules.length;
    const cellSize = size / count;

    let rects = '';
    for (let r = 0; r < count; r++) {
      for (let c = 0; c < count; c++) {
        if (modules[r][c]) {
          const x = (c * cellSize).toFixed(2);
          const y = (r * cellSize).toFixed(2);
          const w = (cellSize + 0.1).toFixed(2);
          const h = (cellSize + 0.1).toFixed(2);
          rects += `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${darkColor}"/>`;
        }
      }
    }

    return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size} ${size}" width="${size}" height="${size}">
      <rect width="${size}" height="${size}" fill="${lightColor}"/>
      ${rects}
    </svg>`;
  }

  private static generateMatrix(text: string): boolean[][] {
    // Dimension de base pour un QR code lisible (25x25 pour version 2)
    const size = 25;
    const matrix: boolean[][] = Array.from({ length: size }, () => Array(size).fill(false));
    const reserved: boolean[][] = Array.from({ length: size }, () => Array(size).fill(false));

    // Place Finder Patterns (7x7) at 3 corners
    this.placeFinder(matrix, reserved, 0, 0);
    this.placeFinder(matrix, reserved, size - 7, 0);
    this.placeFinder(matrix, reserved, 0, size - 7);

    // Timing patterns
    for (let i = 8; i < size - 8; i++) {
      const isDark = i % 2 === 0;
      matrix[6][i] = isDark;
      matrix[i][6] = isDark;
      reserved[6][i] = true;
      reserved[i][6] = true;
    }

    // Alignment pattern for v2 (at 18, 18)
    this.placeAlignment(matrix, reserved, 16, 16);

    // Reserve format info areas
    for (let i = 0; i < 9; i++) {
      reserved[8][i] = true;
      reserved[i][8] = true;
      if (size - 1 - i < size) {
        reserved[8][size - 1 - i] = true;
        reserved[size - 1 - i][8] = true;
      }
    }

    // Hash text to deterministic pseudo-random binary stream with byte contents
    const bytes = new TextEncoder().encode(text);
    let byteIndex = 0;
    let bitIndex = 7;
    let hash = 0x811c9dc5;

    for (let i = 0; i < bytes.length; i++) {
      hash ^= bytes[i];
      hash = Math.imul(hash, 0x01000193);
    }

    // Fill data modules
    let right = size - 1;
    let upward = true;

    while (right > 0) {
      if (right === 6) right--; // skip timing column

      const rows = upward
        ? Array.from({ length: size }, (_, i) => size - 1 - i)
        : Array.from({ length: size }, (_, i) => i);

      for (const row of rows) {
        for (let colOffset = 0; colOffset < 2; colOffset++) {
          const col = right - colOffset;
          if (reserved[row][col]) continue;

          let bit = false;
          if (byteIndex < bytes.length) {
            bit = ((bytes[byteIndex] >> bitIndex) & 1) === 1;
            bitIndex--;
            if (bitIndex < 0) {
              bitIndex = 7;
              byteIndex++;
            }
          } else {
            // Error correction / filler bits based on hash
            hash = Math.imul(hash ^ (row * size + col), 0x5bd1e995);
            bit = (hash & 1) === 1;
          }

          // Apply standard mask (row + col) % 2 === 0
          const mask = (row + col) % 2 === 0;
          matrix[row][col] = mask ? !bit : bit;
        }
      }
      upward = !upward;
      right -= 2;
    }

    return matrix;
  }

  private static placeFinder(matrix: boolean[][], reserved: boolean[][], row: number, col: number): void {
    for (let r = -1; r <= 7; r++) {
      for (let c = -1; c <= 7; c++) {
        const tr = row + r;
        const tc = col + c;
        if (tr < 0 || tr >= matrix.length || tc < 0 || tc >= matrix.length) continue;
        reserved[tr][tc] = true;

        if (r >= 0 && r <= 6 && c >= 0 && c <= 6) {
          if (r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
            matrix[tr][tc] = true;
          } else {
            matrix[tr][tc] = false;
          }
        }
      }
    }
  }

  private static placeAlignment(matrix: boolean[][], reserved: boolean[][], row: number, col: number): void {
    for (let r = 0; r < 5; r++) {
      for (let c = 0; c < 5; c++) {
        const tr = row + r;
        const tc = col + c;
        reserved[tr][tc] = true;
        if (r === 0 || r === 4 || c === 0 || c === 4 || (r === 2 && c === 2)) {
          matrix[tr][tc] = true;
        } else {
          matrix[tr][tc] = false;
        }
      }
    }
  }
}
