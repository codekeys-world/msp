import {startPreview} from './preview-server.mjs';
import {chromium} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile, readdir, mkdir, writeFile} from 'node:fs/promises';

const server = await startPreview();
await mkdir('reports', {recursive: true});
const browser = await chromium.launch({channel: 'chrome', headless: true});
const results = [], failures = [];
const source = await readFile('src/app/app.routes.ts', 'utf8');
const paths = [...source.matchAll(/path\s*:\s*'([^']+)'/g)].map(m => m[1]).filter(p => !['dashboard', '**', 'index'].includes(p));
const publicPaths = ['/', '/index', '/authentification', '/reinitialiser-mot-de-passe', '/confidentialite', '/page-inexistante'];
const privatePaths = paths.filter(p => !publicPaths.includes('/' + p)).map(p => '/dashboard/' + p.replace(/:[^/]+/g, 'audit'));
const routePatterns = paths.filter(p => !publicPaths.includes('/' + p)).map(p => new RegExp('^/dashboard/' + p.replace(/:[^/]+/g, '[^/]+') + '/?$'));
const knownLink = pathname => publicPaths.includes(pathname) || pathname === '/dashboard'
  || routePatterns.some(pattern => pattern.test(pathname));
// Synthetic session and API fixtures only; no real account or backend data is used.
const roles = ['ROLE_SYSADMIN','ROLE_ADMINISTRATEUR','ROLE_SECRETAIRE_ADMINISTRATIF','ROLE_PRESIDENT',
  ...[...source.matchAll(/'([A-Z_]{4,})'/g)].map(m=>m[1])];
for (const f of ['src/app/dashboard/sidebar/sidebar.html','src/app/dashboard/pages/home/home.ts','src/app/dashboard/sidebar/sidebar.ts']) {
  roles.push(...[...(await readFile(f,'utf8')).matchAll(/'([A-Z_]{4,})'/g)].map(m=>m[1]));
}
for (const file of (await readdir('src/app', {recursive:true})).filter(f => f.endsWith('.ts') && !f.endsWith('.spec.ts'))) {
  roles.push(...[...(await readFile('src/app/' + file,'utf8')).matchAll(/'([A-Z_]{4,})'/g)].map(m=>m[1]));
}
const token = Buffer.from('{}').toString('base64url') + '.' + Buffer.from(JSON.stringify({sub:'audit',nom:'Compte de démonstration',email:'audit@example.test',exp:Math.floor(Date.now()/1000)+3600,authorities:[...new Set(roles)]})).toString('base64url') + '.audit';
const member = {slug:'audit',nom:'TRAORÉ',prenom:'Aminata',email:'audit@example.test',matricule:'MSP-AUDIT',telephonePrincipal:'+223 76123456',genre:'FEMME',dateNaissance:'1990-01-01',lieuNaissance:'Bamako',pere:'Ousmane TRAORÉ',mere:'Fatou DIALLO',adresse:'Quartier du Fleuve, Bamako',numeroInscription:'AUDIT',conjoints:[],descendants:[],roles:[],authorisations:[],authorities:[],profileImage:null};
try {
  for (const width of (process.env.AUDIT_WIDTHS ?? '360,768,1440').split(',').map(Number)) {
    const context = await browser.newContext({viewport: {width, height:900}});
    await context.addInitScript(({token}) => { localStorage.setItem('access_token', token); localStorage.setItem('msp-cookie-consent-v1',JSON.stringify({optional:false,updatedAt:Date.now()})); }, {token});
    await context.route('**/api/v1/**', async route => {
      const p = new URL(route.request().url()).pathname;
      let result = {content:[],number:0,size:8,totalPages:0,totalElements:0,first:true,last:true,total:0,hommes:0,femmes:0};
      if (/situation|roles|autorisations|authorisations|manuels/.test(p)) result=[];
      else if (/profil|adherents\/(audit|me)$/.test(p)) result=member;
      else if (/\/adherents$/.test(p)) result={...result,content:[member],totalElements:1,totalPages:1};
      else if (/\/audit$/.test(p)) result={...member,libelle:'Démonstration',objet:'Audit',dateDepart:'2026-11-16',dateRetour:'2026-11-16',datePv:'2026-11-16',dateConvocation:'2026-11-16',dateCourrier:'2026-11-16',dateEncaissement:'2026-11-16',dateAcquisition:'2026-11-16',montant:100,nomFichier:null};
      await route.fulfill({json:{donnees:{resultat:result},status:'OK',statusCode:200,message:''}});
    });
    const page = await context.newPage();
    for (const path of [...publicPaths, ...privatePaths].filter(p => !process.env.AUDIT_PATH || p.includes(process.env.AUDIT_PATH))) {
      const errors = [];
      const listener = error => errors.push(error.message);
      page.on('pageerror',listener);
      await page.goto('http://127.0.0.1:4173'+path);
      await page.waitForTimeout(120);
      const state = await page.evaluate(() => ({
        url:location.pathname, title:document.title, description:document.querySelector('meta[name="description"]')?.content,
        overflow:document.documentElement.scrollWidth>innerWidth+1,
        overflowElements:[...document.querySelectorAll('body *')].filter(e=> {const r=e.getBoundingClientRect();return r.width>0 && (r.right>innerWidth+2 || r.left< -2) && !e.closest('.sidebar,.table-responsive');}).slice(0,8).map(e=>e.className),
        images:[...document.images].filter(i=>!i.hasAttribute('alt') || !i.complete || !i.naturalWidth).map(i=>i.getAttribute('src')),
        links:[...document.querySelectorAll('a[href]')].map(a=>({href:a.href,raw:a.getAttribute('href')}))
          .filter(a=>a.raw!=='#' && a.href.startsWith(location.origin+'/') && !new URL(a.href).pathname.startsWith('/api/')),
        timing: {domContentLoaded: performance.getEntriesByType('navigation')[0]?.domContentLoadedEventEnd,
          fcp: performance.getEntriesByName('first-contentful-paint')[0]?.startTime},
      }));
      let violations=[];
      if(width===360) violations=(await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa']).analyze()).violations.map(v=>({id:v.id,impact:v.impact,nodes:v.nodes.map(n=>({target:n.target,summary:n.failureSummary}))}));
      const brokenLinks=state.links.filter(link=>!knownLink(new URL(link.href).pathname));
      results.push({path,width,...state,errors,violations,brokenLinks});
      if(state.url!==path || state.overflow || errors.length || !state.description || state.images.length || violations.length || brokenLinks.length) failures.push({path,width,url:state.url,overflow:state.overflow,errors,violations:violations.map(v=>v.id),brokenLinks});
      page.off('pageerror',listener);
    }
    await page.goto('http://127.0.0.1:4173/');
    await page.screenshot({path:`reports/home-${width}.png`,fullPage:true});
    await page.goto('http://127.0.0.1:4173/dashboard/home');
    await page.screenshot({path:`reports/dashboard-${width}.png`,fullPage:true});
    if(width===360){
      await page.getByRole('button',{name:'Ouvrir ou fermer le menu'}).click();
      if(await page.getByRole('button',{name:'Ouvrir ou fermer le menu'}).getAttribute('aria-expanded')!=='true') failures.push({test:'Menu mobile'});
      await page.keyboard.press('Escape');
    }
    await context.close();
  }
  const consent = await browser.newPage({viewport:{width:360,height:800}});
  await consent.goto('http://127.0.0.1:4173/confidentialite');
  await consent.getByRole('button',{name:'Continuer sans accepter'}).click();
  await consent.reload();
  if(await consent.locator('.cookie-banner').count()) failures.push({test:'Mémorisation du refus'});
  await consent.getByRole('button',{name:'Modifier mes préférences de cookies'}).click();
  await consent.getByRole('button',{name:'Tout accepter',exact:true}).click();
  await writeFile('reports/ui-audit.json',JSON.stringify({note:'Données API simulées ; aucun test de charge backend.',results,failures},null,2));
  console.log(JSON.stringify({pages:results.length,failures},null,2));
  if (failures.length) process.exitCode = 1;
} finally { await browser.close(); server.close(); }
