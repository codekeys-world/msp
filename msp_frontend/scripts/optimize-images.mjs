import sharp from 'sharp';
import {stat, writeFile} from 'node:fs/promises';

const results = [];
for (const [name, width] of [['logo_msp', 256], ['logo_msp_vf', 512]]) {
  const source = `public/${name}.png`, target = `public/${name}.webp`;
  await sharp(source).resize({width, withoutEnlargement: true}).webp({quality: 85, effort: 6}).toFile(target);
  results.push({source, target, before: (await stat(source)).size, after: (await stat(target)).size});
}
await writeFile('scripts/image-sizes.json', JSON.stringify(results, null, 2) + '\n');
console.log(results);
