/* Captures locales de l'application réelle, avec API simulée et données fictives.
 * Prérequis : npm run build -- --configuration development dans msp_frontend.
 * Exécution depuis la racine : node documentation/capture-manuels.cjs
 */
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const {spawn} = require('node:child_process');
const root = path.resolve(__dirname, '..');
const output = path.join(__dirname, 'captures');
const dist = path.join(root, 'msp_frontend/dist/msp_frontend/browser');
const sql = fs.readFileSync(path.join(root, 'msp_backend/src/main/resources/db/migration/V29__seed_roles_and_authorisations.sql'), 'utf8');
const permissions = [...new Set([...sql.matchAll(/\('([A-Z_]+)'\)/g)].map(m => m[1]))].filter(p => !['ADMINISTRATEUR','SYSADMIN','ADHERENT','PRESIDENT','TRESORIER','COMMISSION_CONTROLE','SECRETAIRE_ADMINISTRATIF','SECRETAIRE_ORGANISATION','SECRETAIRE_AFFAIRE_SOCIALE'].includes(p));
const roles = ['ADMINISTRATEUR','SYSADMIN','SECRETAIRE_ADMINISTRATIF','PRESIDENT','TRESORIER','COMMISSION_CONTROLE','SECRETAIRE_ORGANISATION','SECRETAIRE_AFFAIRE_SOCIALE','ADHERENT'];
const user = {slug:'demo', nom:'EXEMPLE', prenom:'Aminata', email:'demo@example.test', matricule:'DEMO-001', numeroInscription:'DEMO-001', telephonePrincipal:'00000000', dateNaissance:'1990-01-01', lieuNaissance:'Bamako', genre:'FEMME', pere:'Parent exemple', mere:'Parent exemple', adresse:'Adresse de démonstration', role:'ADMINISTRATEUR', enabled:true, authorisations:permissions, conjoints:[], descendants:[]};
const page = content => ({content, number:0,size:10,totalElements:content.length,totalPages:content.length?1:0,first:true,last:true});
const stats = {total:24,hommes:14,femmes:10,terminees:8,enCours:3,aVenir:2,traites:10,nonTraites:2,traitesPourcentage:80,arrives:6,departs:6,traitees:6,enAttente:2,totalActions:48,actionsAujourdhui:4,totalUtilisateursDistincts:6,totalSucces:47,totalEchecs:1};
function mock(url) {
  const p = new URL(url).pathname;
  if (p.includes('/manuels/liste')) return [{role:currentRole,titre:'Manuel '+currentRole.replaceAll('_',' '),fichier:'manuel.pdf'}];
  if (/statistiques|statistique/.test(p)) return stats;
  if (p.endsWith('/me')) return {...user,role:currentRole};
  if (p.endsWith('/frais-de-gestion/options')) return [{slug:'budget-demo',annee:2026,montantTotal:500000}];
  if (/options|roles-disponibles/.test(p)) return [];
  if (/situation/.test(p)) return [{mois:11,libelleMois:'Novembre',libelle:p.includes('depenses')?'Fournitures — exemple':'Versement',statut:'VALIDEE',nombre:1,montantTotal:25000}];
  if (p.endsWith('/depenses')) return page([{slug:'depense-demo',libelle:'Fournitures — exemple',dateDepense:'2026-11-02',montant:25000,statut:'EN_ATTENTE',fraisDeGestionAnnee:2026,observation:'Dossier fictif pour illustrer la validation.'}]);
  if (p.endsWith('/journal-actions')) return page([{slug:'action-demo',nom:'EXEMPLE',prenom:'Aminata',nomComplet:'Aminata EXEMPLE',role:'TRESORIER',matricule:'DEMO-001',adresseIp:'127.0.0.1',client:'Navigateur de démonstration',action:'CREATION',module:'DEPENSE',details:'Création du dossier fictif Fournitures — exemple.',statut:'SUCCES',dateAction:'2026-11-02T10:30:00'}]);
  if (/adherents|utilisateurs/.test(p)) return page([user]);
  return page([]);
}
let currentRole='ADMINISTRATEUR';
const server=http.createServer((req,res)=>{
  let p=decodeURIComponent(req.url.split('?')[0]);
  if (p==='/') p='/index.html';
  let file=path.resolve(dist,'.'+p);
  if (!file.startsWith(dist+path.sep) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) file=path.join(dist,'index.html');
  const mime={'.html':'text/html','.js':'text/javascript','.css':'text/css','.png':'image/png','.woff2':'font/woff2','.svg':'image/svg+xml'};
  res.setHeader('Content-Type',mime[path.extname(file)]||'application/octet-stream');
  fs.createReadStream(file).pipe(res);
});
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
async function main(){
  fs.mkdirSync(output,{recursive:true});
  await new Promise(r=>server.listen(4317,'127.0.0.1',r));
  const chrome=process.env.MSP_CHROME || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
  const profile=path.join(root,'.manual-browser');
  const browser=spawn(chrome,['--headless=new','--disable-gpu','--no-first-run','--remote-debugging-port=9337','--remote-allow-origins=*','--user-data-dir='+profile,'about:blank'],{windowsHide:true,stdio:'ignore'});
  let ws;
  try {
    let targets;
    for(let i=0;i<40;i++){try{targets=await (await fetch('http://127.0.0.1:9337/json')).json();break;}catch{await sleep(250);}}
    ws=new WebSocket(targets.find(t=>t.type==='page').webSocketDebuggerUrl);
    await new Promise(r=>ws.addEventListener('open',r,{once:true}));
    let id=0; const pending=new Map(); const errors=[];
    const send=(method,params={})=>new Promise((resolve,reject)=>{const n=++id;pending.set(n,{resolve,reject});ws.send(JSON.stringify({id:n,method,params}));});
    ws.addEventListener('message',async e=>{
      const m=JSON.parse(e.data);
      if(m.id){const p=pending.get(m.id);pending.delete(m.id);if(m.error)p.reject(m.error);else p.resolve(m.result);}
      if(m.method==='Runtime.exceptionThrown') errors.push(m.params.exceptionDetails.text+': '+(m.params.exceptionDetails.exception?.description||''));
      if(m.method==='Fetch.requestPaused'){
        const result=mock(m.params.request.url);
        await send('Fetch.fulfillRequest',{requestId:m.params.requestId,responseCode:200,responseHeaders:[{name:'Content-Type',value:'application/json'},{name:'Access-Control-Allow-Origin',value:'*'},{name:'Access-Control-Allow-Headers',value:'*'}],body:Buffer.from(JSON.stringify({statusCode:200,message:'Démonstration',donnees:{resultat:result}})).toString('base64')});
      }
    });
    await send('Page.enable'); await send('Runtime.enable');
    await send('Fetch.enable',{patterns:[{urlPattern:'http://localhost:8088/*'}]});
    await send('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});
    const requested = process.argv.slice(2);
    const manifest = requested.length && fs.existsSync(path.join(output,'manifest.json')) ? JSON.parse(fs.readFileSync(path.join(output,'manifest.json'),'utf8')).filter(m=>!requested.includes(m.file.replace('.png',''))) : [];
    async function capture(name,route,role='ADMINISTRATEUR',action){
      if(requested.length && !requested.includes(name)) return;
      currentRole=role;
      const grants=['ADMINISTRATEUR','SYSADMIN','SECRETAIRE_ADMINISTRATIF'].includes(role)?permissions:[...sql.matchAll(/\('([A-Z_]+)', '([A-Z_]+)'\)/g)].filter(m=>m[1]===role).map(m=>m[2]);
      const payload={nom:'Aminata EXEMPLE',email:'demo@example.test',sub:'DEMO-001',iat:Math.floor(Date.now()/1000),exp:Math.floor(Date.now()/1000)+86400,authorities:['ROLE_'+role,...grants]};
      const token=Buffer.from('{}').toString('base64url')+'.'+Buffer.from(JSON.stringify(payload)).toString('base64url')+'.demo';
      const init=await send('Page.addScriptToEvaluateOnNewDocument',{source:`localStorage.setItem('access_token',${JSON.stringify(token)});`});
      errors.length=0;
      await send('Page.navigate',{url:'http://127.0.0.1:4317/'+route});
      await sleep(1700);
      await send('Runtime.evaluate',{expression:`(async()=>{await document.fonts.ready;document.querySelectorAll('*').forEach(e=>{e.style.animation='none';e.style.transition='none';});const note=document.createElement('div');note.textContent='DOCUMENTATION MSP • Données fictives de démonstration';note.style.cssText='position:fixed;bottom:0;left:0;right:0;z-index:99999;background:#0F7D4B;color:white;padding:8px 20px;font:14px Arial;text-align:center';document.body.appendChild(note);})()`,awaitPromise:true});
      const body=await send('Runtime.evaluate',{expression:'document.body.innerText'});
      if(errors.length) throw new Error(name+': '+errors.join('\n'));
      if(!body.result.value.includes(route==='authentification'?'MSP':'Tableau de bord')) throw new Error('Écran non rendu : '+name+' '+body.result.value.slice(0,500));
      if(action){
        const result=await send('Runtime.evaluate',{expression:action});
        if(result.exceptionDetails) throw new Error('Action de capture impossible : '+name);
        await sleep(350);
      }
      const img=await send('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});
      fs.writeFileSync(path.join(output,name+'.png'),Buffer.from(img.data,'base64'));
      manifest.push({file:name+'.png',route,role,viewport:'1440x1000',data:'fictives'});
      await send('Page.removeScriptToEvaluateOnNewDocument',{identifier:init.identifier});
      console.log(name);
    }
    await capture('connexion','authentification');
    for(const role of roles) await capture('accueil-'+role.toLowerCase(),'dashboard/home',role);
    const screens=[['profil','profil'],['adherents','adherents'],['nouvel-adherent','adherent/create'],['cartes','cartes-membres'],['utilisateurs','utilisateurs'],['encaissement','nouvel-encaissement'],['depense','depenses/nouvelle'],['depenses','depenses'],['frais-gestion','frais-de-gestion/nouveau'],['prestation','nouvelle-prestation'],['cas-social','nouveau-cas-social'],['plainte','nouvelle-plainte'],['courrier','nouveau-courrier'],['convocation','nouvelle-convocation'],['pv','nouveau-pv'],['mission','nouveau-ordre-missions'],['materiel','nouveau-materiel'],['archives','archives'],['controle','journal-actions'],['adjoints','adjoints'],['situation-encaissements','situation-encaissements'],['situation-depenses','situation-depenses']];
    for(const [name,route] of screens) await capture(name,'dashboard/'+route);
    for(const [name,route] of [['courrier-liste','courrier'],['plainte-liste','plaintes'],['convocation-liste','convocations'],['pv-liste','proces-verbaux']]) await capture(name,'dashboard/'+route);
    await capture('decision','dashboard/depenses','PRESIDENT',`document.querySelector('[aria-label="Rejeter la dépense"]').click()`);
    await capture('securite','dashboard/profil','ADHERENT',`document.querySelector('#ancienMotDePasse').scrollIntoView({block:'center'})`);
    fs.writeFileSync(path.join(output,'manifest.json'),JSON.stringify(manifest,null,2));
  }finally{ws?.close();browser.kill();server.close();}
}
main().catch(e=>{console.error(e);process.exitCode=1;server.close();});
