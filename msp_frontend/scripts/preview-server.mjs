import {createServer} from 'node:http';
import {readFile} from 'node:fs/promises';
import {resolve, extname} from 'node:path';
import {gzipSync} from 'node:zlib';

/** Production files, gzip, and SPA fallback. No dependency on a running backend. */
export async function startPreview(port = 4173) {
  const root = resolve('dist/msp_frontend/browser');
  const types = {'.html':'text/html','.js':'text/javascript','.css':'text/css','.webp':'image/webp','.png':'image/png','.woff2':'font/woff2','.ico':'image/x-icon'};
  const server = createServer(async (req, res) => {
    const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
    if (pathname.startsWith('/api/')) { res.writeHead(503, {'Content-Type':'application/json'}); res.end('{"message":"Backend absent de cet audit local"}'); return; }
    let file = resolve(root, '.' + pathname);
    if (!file.startsWith(root + '/') && !file.startsWith(root + '\\') && file !== root) { res.writeHead(403); res.end(); return; }
    try {
      if (!extname(file)) file = resolve(root, 'index.html');
      let content = await readFile(file);
      const headers = {'Content-Type':types[extname(file)] ?? 'application/octet-stream'};
      if (/\.(html|js|css)$/.test(file)) { content = gzipSync(content); headers['Content-Encoding'] = 'gzip'; }
      res.writeHead(200,headers); res.end(content);
    } catch { res.writeHead(404); res.end(); }
  });
  await new Promise(done => server.listen(port,'127.0.0.1',done));
  return server;
}
