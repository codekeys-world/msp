import lighthouse from 'lighthouse';
import {launch} from 'chrome-launcher';
import {mkdir, writeFile} from 'node:fs/promises';
import {startPreview} from './preview-server.mjs';

const server = await startPreview(4174);
await mkdir('reports',{recursive:true});
const chrome = await launch({chromePath:process.env.CHROME_PATH ?? 'C:/Program Files/Google/Chrome/Application/chrome.exe',chromeFlags:['--headless','--disable-gpu','--no-sandbox']});
try {
  const results=[];
  for (const [name,path] of [['accueil','/'],['connexion','/authentification'],['confidentialite','/confidentialite']]) {
    const {lhr,report}=await lighthouse('http://127.0.0.1:4174'+path,{port:chrome.port,output:'html',logLevel:'error',onlyCategories:['performance','accessibility','seo','best-practices']});
    await writeFile(`reports/lighthouse-${name}.html`,report);
    results.push({page:path,scores:Object.fromEntries(Object.entries(lhr.categories).map(([key,c])=>[key,Math.round(c.score*100)])),
      fcp:lhr.audits['first-contentful-paint'].displayValue,lcp:lhr.audits['largest-contentful-paint'].displayValue,
      tbt:lhr.audits['total-blocking-time'].displayValue,cls:lhr.audits['cumulative-layout-shift'].displayValue,
      failed:Object.values(lhr.audits).filter(a=>a.score!==null && a.score<1).map(a=>({id:a.id,title:a.title,displayValue:a.displayValue}))});
  }
  await writeFile('reports/performance.json',JSON.stringify({note:'Chrome, profil mobile Lighthouse, build production et gzip, backend absent. Mesures locales, pas de mesure de latence serveur de production.',results},null,2));
  console.log(JSON.stringify(results,null,2));
} finally { await chrome.kill(); server.close(); }
